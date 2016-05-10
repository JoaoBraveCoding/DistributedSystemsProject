package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import pt.upa.transporter.ws.handler.HeaderHandler;






@WebService(
		endpointInterface="pt.upa.transporter.ws.TransporterPortType",
		wsdlLocation="transporter.1_0.wsdl",
		name="TransporterWebService",
		portName="TransporterPort",
		targetNamespace="http://ws.transporter.upa.pt/",
		serviceName="TransporterService"
		)
@HandlerChain(file = "/handler-chain.xml")
public class TransporterPort implements TransporterPortType {

	private int    identifierCounter;
	private String name;
	private Random rn    = new Random();
	private Timer  timer = new Timer();
	private List<JobView> jobs = new ArrayList<JobView>();
	private Map<String, String> locations = new HashMap<String, String>();
	private Map<String, String> placesNotOperable = new HashMap<String, String>();
	
	@Resource 
	private WebServiceContext webServiceContext;

	public class ChangeState extends TimerTask {
		private JobView      jw;
		private JobStateView js;

		public ChangeState(JobView jw, JobStateView js){
			this.jw = jw;
			this.js = js;
		}

		@Override
		public void run() {
			jw.setJobState(js);
			JobStateView jsFuture = null;

			if(js.equals(JobStateView.HEADING)){
				jsFuture = JobStateView.ONGOING;
				TimerTask tt = new ChangeState(jw, jsFuture);
				timer.schedule(tt, rn.nextInt(4001) + 1000);
			}

			if(js.equals(JobStateView.ONGOING)){
				jsFuture = JobStateView.COMPLETED;
				TimerTask tt = new ChangeState(jw, jsFuture);
				timer.schedule(tt, rn.nextInt(4001) + 1000);
			}

			else{
				//js = COMPLETED nothing to do
			}
		}
	}

	public TransporterPort(String name) {
		this.name = name;
		
		identifierCounter = -1;
		locations.put("Lisboa", "Center");
		locations.put("Leiria", "Center");
		locations.put("Santarém", "Center");
		locations.put("Castelo Branco", "Center");
		locations.put("Coimbra", "Center");
		locations.put("Aveiro", "Center");
		locations.put("Viseu", "Center");
		locations.put("Guarda", "Center");

		if(name.matches("UpaTransporter[1-9]*[02468]$")){
			locations.put("Porto", "North");
			locations.put("Braga", "North");
			locations.put("Viana Do Castelo", "North");
			locations.put("Vila Real", "North");
			locations.put("Bragança", "North");

			placesNotOperable.put("Setúbal", "South");
			placesNotOperable.put("Évora", "South");
			placesNotOperable.put("Portalegre", "South");
			placesNotOperable.put("Beja", "South");
			placesNotOperable.put("Faro", "South");
		}
		else{
			locations.put("Setúbal", "South");
			locations.put("Évora", "South");
			locations.put("Portalegre", "South");
			locations.put("Beja", "South");
			locations.put("Faro", "South");

			placesNotOperable.put("Porto", "North");
			placesNotOperable.put("Braga", "North");
			placesNotOperable.put("Viana Do Castelo", "North");
			placesNotOperable.put("Vila Real", "North");
			placesNotOperable.put("Bragança", "North");
		}

	}


	@Override
	public String ping(String name) {
	  putTransporterNameInContext();
		System.out.println("Received Ping from " + name);
		return "Pong " + name + "!";
	}
	
	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
	  putTransporterNameInContext();
	  System.out.println(origin + " destination: " + destination + " price: " + price);
	  
		JobView budgetJob;
		System.out.println(price);
		//check Price
		if(price < 0){
			BadPriceFault badPrice = new BadPriceFault();
			badPrice.setPrice(price);
			throw new BadPriceFault_Exception("Requested a Job with price a negative price", badPrice);
		}

		//check Origin Location
		if(origin == null ||origin.equals("") || (!(placesNotOperable.containsKey(origin)) && !(locations.containsKey(origin))) ){
			BadLocationFault badLocation = new BadLocationFault();
			badLocation.setLocation(origin);
			throw new BadLocationFault_Exception("Requested a Job with a origin location unknown", badLocation);
		}

		//check Destination Location
		if(destination == null || destination.equals("") || (!(placesNotOperable.containsKey(destination)) && !(locations.containsKey(destination))) ){
			BadLocationFault badLocation = new BadLocationFault();
			badLocation.setLocation(destination);
			throw new BadLocationFault_Exception("Requested a Job with a destination location unknown", badLocation);
		}

		//doesn't operate in region 
		if (placesNotOperable.containsKey(origin) || placesNotOperable.containsKey(destination)) {
		  
		  System.out.println("I do not work in this conditions.");
			return null;
		}

		if(price > 100) {
			return null;
		}

		budgetJob = createNewJob(origin, destination);

		if(price <= 10) {
			budgetJob.setJobPrice(rn.nextInt(price) + 1);
			jobs.add(budgetJob);
			System.out.println("Proposing: "+ budgetJob.getJobPrice());
			return budgetJob;
		}

		else if(price > 10 && price <= 100) {
			budgetJob.setJobPrice(calcPrice(price));
			jobs.add(budgetJob);
			System.out.println("Proposing: "+ budgetJob.getJobPrice());
			return budgetJob;
		}

		//TODO see if it shouldn't throw an Exception
		return null;
	}

	private int calcPrice(int price) {
		if(price % 2 == 0) {
			if(name.matches("UpaTransporter[1-9]*[02468]$")) {
				//transporter and price even
				return rn.nextInt(price);
			}
			else{
				//price even but transporter odd
				return rn.nextInt(price) + price + 1;
			}
		}

		else {
			if((name.matches("UpaTransporter[1-9]*[02468]$"))) {
				//transporter even but price odd
				return rn.nextInt(price) + price + 1;
			}
			else {
				//transporter and price odd
				return rn.nextInt(price);
			}
		}
	}

	private JobView createNewJob(String origin, String destination) {
		JobView budgetJob = new JobView();
		//TODO Don't know if this is right
		budgetJob.setCompanyName(name);
		budgetJob.setJobIdentifier(getIdentifer());
		budgetJob.setJobOrigin(origin);
		budgetJob.setJobDestination(destination);
		budgetJob.setJobState(JobStateView.PROPOSED);
		return budgetJob;
	}

	private String getIdentifer(){
		identifierCounter++;
		return name + "." + identifierCounter;
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
	  putTransporterNameInContext();
		if(id == null || id.equals("")){ 
			BadJobFault faultInfo = new BadJobFault();
			throw new BadJobFault_Exception("Empty or null job id", faultInfo);
		}
		String[] parts = id.split("\\.");
		int i = Integer.parseInt(parts[parts.length - 1]);

		if(i > jobs.size()){
			BadJobFault faultInfo = new BadJobFault();
			throw new BadJobFault_Exception("Job id Received from Broker unknown", faultInfo);
		}

		JobView job = jobs.get(i);
		if (job.getJobState()!=JobStateView.PROPOSED){
			BadJobFault faultInfo = new BadJobFault();
			throw new BadJobFault_Exception("Duplicated decide job.", faultInfo);
		}

		if(accept){
			job.setJobState(JobStateView.ACCEPTED);
			TimerTask tt = new ChangeState(jobs.get(i), JobStateView.HEADING);
			timer.schedule(tt, rn.nextInt(4001) + 1000);
		}

		else{
			job.setJobState(JobStateView.REJECTED);
		}
		return jobs.get(i);
	}


	@Override
	public JobView jobStatus(String id) {
	  putTransporterNameInContext();
	  if(id==null){
	    return null;
	  }
	  
		String[] parts = id.split("\\.");

		if(!id.matches("UpaTransporter[1-9]*.[0-9]*$")) {
			return null;
		}

		int i = Integer.parseInt(parts[parts.length - 1]);

		//TODO see if this is safe or is needed to use other object
		return jobs.get(i);

	}

	@Override
	public List<JobView> listJobs() {
		//TODO see if this is safe or if we need to return something else
	  putTransporterNameInContext();
		return jobs;
	}

	@Override
	public void clearJobs() {
		jobs = new ArrayList<JobView>();
		identifierCounter = -1;
	}

	public int getIdentifier(){
		return identifierCounter;
	}
	
	public void putTransporterNameInContext(){
	  // retrieve message context
    if(webServiceContext != null) {
      MessageContext messageContext = webServiceContext.getMessageContext();
      // *** #6 ***
      // get token from message context
      String propertyValue = (String) messageContext.get(HeaderHandler.CONTEXT_PROPERTY);
      System.out.printf("%s got token '%s' from response context%n", "transporter", propertyValue);
  
      // *** #7 ***
      // put token in message context
      System.out.printf("%s put token '%s' on request context%n", "transporter", this.name);
      messageContext.put(HeaderHandler.CONTEXT_PROPERTY, this.name);
    }
	}

	public void stopTimer(){
		timer.cancel();
	}
}