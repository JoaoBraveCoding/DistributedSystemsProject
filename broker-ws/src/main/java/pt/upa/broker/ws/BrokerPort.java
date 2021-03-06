package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
    )
@HandlerChain(file = "/handler-chain.xml")
public class BrokerPort implements BrokerPortType {

  private List<TransporterPortType> transporters = new ArrayList<TransporterPortType>();
  private List<TransportView>    	transports   = new ArrayList<TransportView>();
  private Map<String, String> places = new HashMap<String, String>();
  private BrokerPortType secondaryBroker = null;
  private int identifierCounter = 0;
  Timer timer = new Timer();
  Timer takeovertimer = new Timer();
  MyTakeoverTimer primBrokerDied;
  boolean primaryBroker = false;

  public BrokerPort() {
    super();
    // adding known places 
    places.put("Porto", "North");
    places.put("Braga", "North");
    places.put("Viana do Castelo", "North");
    places.put("Vila Real", "North");
    places.put("Bragança", "North");

    places.put("Lisboa", "Center");
    places.put("Leiria", "Center");
    places.put("Santarém", "Center");
    places.put("Castelo Branco", "Center");
    places.put("Coimbra", "Center");
    places.put("Aveiro", "Center");
    places.put("Viseu", "Center");
    places.put("Guarda", "Center");

    places.put("Setúbal", "South");
    places.put("Évora", "South");
    places.put("Portalegre", "South");
    places.put("Beja", "South");
    places.put("Faro", "South");
    
  }
  
  public class MyTimerTask extends TimerTask {

    @Override
    public void run(){
      if(secondaryBroker!=null){
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        secondaryBroker.imAlive("["+date+"]"+" El Capitan is still alive. I shall wait for my turn.");
      }
    }
  }
  
  public class MyTakeoverTimer extends TimerTask {

    @Override
    public void run(){
      //takeover
      System.out.println("\nI BE THE CAPTAIN NOW!! YARRRR");
      primaryBroker = true;
    }
  }

  @Override
  public String ping(String name) {
    String returnValue;
    int counter = 0;
    System.out.println("\nReceived ping from " + name);

    //Pinging transporters 
    System.out.println("Ping - Slapping transporters");
    for(TransporterPortType port : transporters){
      
      returnValue = port.ping("Broker");
      if (returnValue.equals("Pong Broker!")){
        counter++;
      }
    }
    System.out.println("Slap " + name + "! " + "(" + counter + "/" + transporters.size() + ")" + " transporters online/transporters");
    return "Pong " + name + "!";
  }

  @Override
  public String requestTransport(String origin, String destination, int price) throws 
  UnknownLocationFault_Exception, InvalidPriceFault_Exception, UnavailableTransportFault_Exception, 
  UnavailableTransportPriceFault_Exception {
    System.out.println("\nLooking for pirate ship to go from... "+origin+" to "+destination+". Bounty:"+price);

    JobView bestJob = null;
    TransporterPortType bestTransporter = null;
    boolean one_job_not_null = false;
    Map<JobView,TransporterPortType> jobs_transporters = new HashMap<JobView,TransporterPortType>();
    // check if origin is known
    placeExists(origin);
    // check if destination is known
    placeExists(destination);
    // check if price is positive
    priceIsValid(price);

    TransportView transport = createTransport(origin, destination);

    for(TransporterPortType transporter: transporters){
      JobView requested_job;
      try {
        requested_job = transporter.requestJob(origin, destination, price);

        if(requested_job==null){
          continue;
        }
        
        jobs_transporters.put(requested_job, transporter);

        one_job_not_null = true;
        if(bestJob==null && requested_job.getJobPrice() < price ){
          bestJob = requested_job;
          bestTransporter = transporter;
        }

        if(bestJob!=null && requested_job.getJobPrice() <= bestJob.getJobPrice()){
          bestJob = requested_job;
          bestTransporter = transporter;
        }
      } catch (BadLocationFault_Exception | BadPriceFault_Exception e) {
        System.out.println(e.getMessage());
      }
    }

    if(bestJob==null && !one_job_not_null){
      transport.setState(TransportStateView.FAILED);
      if(secondaryBroker!=null){
        secondaryBroker.updateBackup(transport);
      }
      UnavailableTransportFault fault = new UnavailableTransportFault();
      throw new UnavailableTransportFault_Exception("No transporter for the job", fault);
    }
    else if(bestJob==null && one_job_not_null){
      transport.setState(TransportStateView.FAILED);
      if(secondaryBroker!=null){
        secondaryBroker.updateBackup(transport);
      }
      UnavailableTransportPriceFault fault = new UnavailableTransportPriceFault();
      throw new UnavailableTransportPriceFault_Exception("Unavailable price", fault);
    }

    updateTransport(transport, bestTransporter, bestJob);
    
    try {
      bestTransporter.decideJob(bestJob.getJobIdentifier(), true);
    } catch (BadJobFault_Exception e) {
      System.out.println(e.getMessage());
    }
    transport.setState(TransportStateView.BOOKED);

    for(JobView job: jobs_transporters.keySet()){
      if(job.getJobIdentifier()!=bestJob.getJobIdentifier()){
        try {
          jobs_transporters.get(job).decideJob(job.getJobIdentifier(), false);
        } catch (BadJobFault_Exception e) {
          System.out.println(e.getMessage());
        }
      }
    }
    
    if(secondaryBroker!=null){
      secondaryBroker.updateBackup(transport);
    }
    return transport.getId();
  }

  private TransportView updateTransport(TransportView transport, TransporterPortType transporter, JobView job) {
    transport.setId(transport.getId()+"_"+getTransporterPortTypeName(transporter)+"_"+job.getJobIdentifier());
    transport.setPrice(job.getJobPrice());
    transport.setTransporterCompany(getTransporterPortTypeName(transporter));
    transport.setState(TransportStateView.BUDGETED);

    return transport;
  }

  private TransportView createTransport(String origin, String destination) {
    TransportView transport = new TransportView();

    transport.setId(getIdentifier());
    transport.setOrigin(origin);
    transport.setDestination(destination);
    transport.setPrice(-1);
    transport.setTransporterCompany(null);
    transport.setState(TransportStateView.REQUESTED);

    transports.add(transport);
    return transport;
  }


  // Check if place exists
  private void placeExists(String place) throws UnknownLocationFault_Exception{
    if(places.containsKey(place)) return;

    UnknownLocationFault fault = new UnknownLocationFault();
    fault.setLocation(place);
    throw new UnknownLocationFault_Exception("Location not found. ", fault);
  }

  // Check if price is valid
  private void priceIsValid(int price) throws InvalidPriceFault_Exception{
    if (price < 0) {
      InvalidPriceFault fault = new InvalidPriceFault();
      fault.setPrice(price);
      throw new InvalidPriceFault_Exception("Price invalid. ", fault);
    }
    return;
  }

  private String getIdentifier(){
    identifierCounter++;
    return "" + identifierCounter;
  }

  private String getTransporterPortTypeName(TransporterPortType transporter){
    for (Integer i = 0; i < transporters.size(); i++){
      if(transporter == transporters.get(i)){
        return i.toString();
      }
    }
    return "";
  }

  @Override
  public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
    TransporterPortType corresponding_company = null;

    for(TransportView transport: transports){

      if(transport.getId().equals(id)) {
        corresponding_company = this.getTransporter(transport);

        String identifier = getJobIdentifier(transport);
        JobView job_status = corresponding_company.jobStatus(identifier);

        if(job_status.getJobState() == JobStateView.HEADING) transport.setState(TransportStateView.HEADING);
        if(job_status.getJobState() == JobStateView.ONGOING) transport.setState(TransportStateView.ONGOING);
        if(job_status.getJobState() == JobStateView.COMPLETED) transport.setState(TransportStateView.COMPLETED);

        return transport;
      }
    }

    UnknownTransportFault fault = new UnknownTransportFault();
    fault.setId(id);
    throw new UnknownTransportFault_Exception("Transport not found. ", fault);
  }

  public TransporterPortType getTransporter(TransportView transport){
    String transporter_id = transport.id.split("_")[1];
    for(TransporterPortType transporter: transporters){
      if(getTransporterPortTypeName(transporter).equals(transporter_id)){
        return transporter;
      }
    }
    return null;
  }
  
  public String getJobIdentifier(TransportView transport){
    return transport.getId().split("_")[2];
  }

  @Override
  public List<TransportView> listTransports() {
    return transports;
  }

  @Override
  public void clearTransports() {
    if(secondaryBroker!=null){
      secondaryBroker.clearTransports(); 
    }
    identifierCounter = 0;
    for(TransporterPortType transporter: transporters){
      transporter.clearJobs();
    }
    transports = new ArrayList<TransportView>();
  }

  public List<TransportView> getTransports() {
    return transports;
  }

  public void addTransporter(String name, UDDINaming uddiNaming) throws JAXRException{
    String endpointAddress = name;
    if (endpointAddress == null) {
      System.out.println("Not found!");
      return;
    }
    TransporterService service = new TransporterService();
    TransporterPortType port = service.getTransporterPort();

    BindingProvider bindingProvider = (BindingProvider) port;
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    System.out.println("\nAdding transporter: " + port.toString());
    transporters.add(port);
  }
  
  public void addSecondaryBroker(String secondaryBrokerAddress) {
    if (secondaryBrokerAddress == null) {
      System.out.println("Not found!");
      return;
    }
    BrokerService service = new BrokerService();
    BrokerPortType port = service.getBrokerPort();

    BindingProvider bindingProvider = (BindingProvider) port;
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    requestContext.put(ENDPOINT_ADDRESS_PROPERTY, secondaryBrokerAddress);
    
    secondaryBroker = port;
  }

  @Override
  public void updateBackup(TransportView tv){
    System.out.println("updating transport with id: "+tv.getId());
    if(!primaryBroker){
      for(TransportView tmp: transports){
        if(tmp.getId().equals(tv.getId())){
          copyTransportView(tmp,tv);
          return;
        }
      }
      transports.add(tv);
    }
  }
  
  @Override
  public void imAlive(String arg){
    System.out.println(arg);
    if(!primaryBroker){
      //reset timer to take over
      if(primBrokerDied!=null){
        primBrokerDied.cancel();
      }
      primBrokerDied = new MyTakeoverTimer();
      takeovertimer.schedule(primBrokerDied, 2000);
    }
  }
  
  private void copyTransportView(TransportView old, TransportView young){
    old.setOrigin(young.getOrigin());
    old.setDestination(young.getDestination());
    old.setPrice(young.getPrice());
    old.setTransporterCompany(young.getTransporterCompany());
    old.setState(young.getState());
  }

  public void setBrokerType(boolean primary) {
    primaryBroker = primary;
    if(primaryBroker){
      MyTimerTask sendLifeProof = new MyTimerTask();
      timer.schedule(sendLifeProof, 1000, 1000);
    }
  }
  
  public void stopTimers(){
    timer.cancel();
    takeovertimer.cancel();
  }

  public boolean isPrimary() {
    return primaryBroker;
  }

}