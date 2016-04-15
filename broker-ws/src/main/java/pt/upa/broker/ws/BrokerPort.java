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
  private Map<TransportView, JobView> transports_jobs = new HashMap<TransportView, JobView>();
  private Map<TransportView, TransporterPortType> transports_transporters = new HashMap<TransportView, TransporterPortType>();
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
    System.out.println("Pong " + name + "! " + "(" + counter + "/" + transporters.size() + ")" + " transporters online/transporters");
    return "Pong " + name + "!";
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
	  // best price will never be higher than proposed max
	  int bestPrice = price;
	  JobView bestJob = null;
	  TransporterPortType bestTransporter = null;
	  
	  // for not chosen jobs
	  Map<JobView, TransporterPortType> badJobs = new HashMap<JobView, TransporterPortType>();
	  Map<JobView, TransportView> badTransports = new HashMap<JobView, TransportView>();
	  boolean higher_price = false;
	  
	  for(TransporterPortType transporter: transporters){
		  try {
			  // create transport for each transporter
			  transportView = createTransport(origin, destination, price, bestTransporter);
			  // request a job
			  JobView job = transporter.requestJob(origin, destination, price);
			  // add to arrays the new information job-transport
			  transports_jobs.put(transportView, job); // class attribute
			  transports_transporters.put(transportView, transporter);
			  // function locals
			  badJobs.put(job, transporter);
			  badTransports.put(job, transportView);
			  System.out.println("Proposed price: " + job.getJobPrice());
			  
			  // set transport state to BUDGETED
			  transportView.setState(TransportStateView.BUDGETED);
			  if (job == null) continue;
			  // check if price is better that it was before
			  if (job.getJobPrice() <= bestPrice){
				  bestJob = job;
				  System.out.println(bestJob.getCompanyName() + bestJob.getJobPrice());
				  bestTransporter = transporter;
				  bestPrice = bestJob.getJobPrice();
			  } else { higher_price = true; } // needed to throw UnavailableTransportPrice
		  } catch (BadLocationFault_Exception e) { 
			  System.out.println(e.getMessage());
		  } catch (BadPriceFault_Exception e) {
			  System.out.println(e.getMessage());
		  }
	  }
	  // no job to fulfill client's needs
	  UnavailableTransportFault fault1 = new UnavailableTransportFault();
	  if (bestJob == null) throw new UnavailableTransportFault_Exception("No transporter for the job", fault1);
	  
	  // the price was always higher than the maximum allowed 
	  UnavailableTransportPriceFault fault2 = new UnavailableTransportPriceFault();
	  System.out.println("higher price:" + higher_price + "bestJob:" + bestJob);
	  if (higher_price && bestJob == null) throw new UnavailableTransportPriceFault_Exception("Uknavailable Price.", fault2);
	  
	  // remove bestjob from badjobs
	  badJobs.remove(bestJob);

	  // update non chosen jobs status to failed
	  for(JobView j : badJobs.keySet()){
		  badTransports.get(j).setState(TransportStateView.FAILED);
	  }
	  
	  
	  try {
		  // tell transporter to accept bestJob offer
		  JobView stateJob = bestTransporter.decideJob(bestJob.getJobIdentifier(), true);
		 

		  badTransports.get(bestJob).setState(TransportStateView.BOOKED);

		 
		 for(JobView j: badJobs.keySet()){
			 stateJob = badJobs.get(j).decideJob(j.getJobIdentifier(), false);
			 badTransports.get(j).setState(TransportStateView.FAILED);;

		 }
	  } catch (BadJobFault_Exception e) {
	  }
	  System.out.println("RETRIEVING ID " + transportView.getId());
	  return transportView.getId();
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
	  JobView corresponding_job = null;
	  TransporterPortType corresponding_company = null;
	System.out.println("GETTING TRANSPORT" + id);
	System.out.println("TRANSPORTS IDS: ");
	  
	for(TransportView transport: transports){
		// get right transport
		
		if(transport.getId().equals(id)) {
			corresponding_job = this.transports_jobs.get(transport);
			corresponding_company = this.transports_transporters.get(transport);
			
			System.out.println("TESTE VIEW TRANSPORT: "+ corresponding_job.getCompanyName() + " " +  corresponding_company.toString() );
			
			String identifier = corresponding_job.getJobIdentifier();
			System.out.println(identifier);
			JobView job_status = corresponding_company.jobStatus(identifier);
			System.out.println(job_status.getCompanyName());
			System.out.println(job_status.getJobState());
			corresponding_job.setJobState(job_status.getJobState());
		
		
			if(corresponding_job.getJobState() == JobStateView.HEADING) transport.setState(TransportStateView.HEADING);
			if(corresponding_job.getJobState() == JobStateView.ONGOING) transport.setState(TransportStateView.ONGOING);
			if(corresponding_job.getJobState() == JobStateView.COMPLETED) transport.setState(TransportStateView.COMPLETED);
			
			return transport;
		}
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

    transporters.add(port);
  }
  
}