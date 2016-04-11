package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class BrokerPort implements BrokerPortType {

  private List<TransporterPortType> transporters = new ArrayList<TransporterPortType>();
  private List<TransportView>    	transports   = new ArrayList<TransportView>();
  private Map<String, String> places = new HashMap<String, String>();
  private int identifierCounter = 0;
  
  public BrokerPort() {
	  super();
	  // adding known places 
	  places.put("Porto", "North");
	  places.put("Braga", "North");
	  places.put("Viana do Castelo", "North");
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
	  places.put("Faro", "South");
  }
  

  @Override
  public String ping(String name) {
    String returnValue;
    int counter = 0;
    System.out.println("Received ping from " + name);
    
    //Pinging transporters 
    System.out.println("Pingging transporters");
    for(TransporterPortType port : transporters){
      returnValue = port.ping("Broker");
      if (returnValue.equals("Pong Broker!")){
        counter++;
      }
    }
    return "Pong " + name + "! " + "(" + counter + "/" + transporters.size() + ")" + " transporters online/transporters";
  }

  @Override
  public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception,
  UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {

	  // check if origin is known
	  placeExists(origin);
	  // check if destination is known
	  placeExists(destination);
	  // check if price is positive
	  priceIsValid(price);

	  // find best job
	  TransportView transportView = null;
	  int bestPrice = price;
	  JobView bestJob = null;
	  TransporterPortType bestTransporter = null;
	  Map<JobView, TransporterPortType> badJobs = new HashMap<JobView, TransporterPortType>();
	  Map<JobView, TransportView> badTransports = new HashMap<JobView, TransportView>();
	  
	  for(TransporterPortType transporter: transporters){
		  try {
			  // create transport for each transporter
			  transportView = createTransport(origin, destination, price, bestTransporter);
			  JobView job = transporter.requestJob(origin, destination, price);
			  badJobs.put(job, transporter);
			  badTransports.put(job, transportView);
			  
			  transportView.setState(TransportStateView.BUDGETED);
			  if (job == null) continue; // TODO should I tell that it is rejected?
			  if (job.getJobPrice() <= bestPrice){
				  bestJob = job;
				  bestTransporter = transporter;
				  bestPrice = bestJob.getJobPrice();
			  }
		  } catch (BadLocationFault_Exception e) {
			  // TODO do what?
		  } catch (BadPriceFault_Exception e) {
			  // TODO do what?
			  //InvalidPriceFault fault = new InvalidPriceFault();
			  //fault.setPrice(price);
			  //throw new InvalidPriceFault_Exception("Invalid Price Exception. ", fault);
		  }
	  }
	  // TODO which exception?
	  if (bestJob == null) throw new UnavailableTransportFault_Exception(destination, null);
	  badJobs.remove(bestJob);
	  
	  try {
		 JobView stateJob = bestTransporter.decideJob(bestJob.getJobIdentifier(), true);
		 if(stateJob.getJobState() == JobStateView.ACCEPTED){
			 badTransports.get(bestJob).setState(TransportStateView.BOOKED);;
		 }
		 
		 for(JobView j: badJobs.keySet()){
			 stateJob = badJobs.get(j).decideJob(j.getJobIdentifier(), false);
			 if(stateJob.getJobState() == JobStateView.REJECTED){
				 badTransports.get(bestJob).setState(TransportStateView.FAILED);;
			 }
		 }
	  } catch (BadJobFault_Exception e) {
		// TODO do what?
	  }
	  return null;
  }
  
  private TransportView createTransport(String origin, String destination, int price, TransporterPortType transporter) {
	TransportView transport = new TransportView();
	
	transport.setId(getIdentifier());
	transport.setOrigin(origin);
	transport.setDestination(destination);
	transport.setPrice(price);
	transport.setTransporterCompany(getTransporterPortTypeName(transporter));
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
	for(TransportView transport: transports){
		if(transport.getId() == id) return transport;
	}
	UnknownTransportFault fault = new UnknownTransportFault();
	fault.setId(id);
    throw new UnknownTransportFault_Exception("Transport not found. ", fault);
  }

  @Override
  public List<TransportView> listTransports() {
    return transports;
  }

  @Override
  public void clearTransports() {
	for(TransporterPortType transporter: transporters){
		transporter.clearJobs();
	}
    transports = new ArrayList<TransportView>();
  }
   
  public List<TransporterPortType> getTransporters(){
    return this.transporters;
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

    transporters.add(port);
  }
  
}
