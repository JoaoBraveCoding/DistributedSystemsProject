package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jws.WebService;

@WebService(
    endpointInterface="pt.upa.transporter.ws.TransporterPortType",
    wsdlLocation="transporter.1_0.wsdl",
    name="TransporterWebService",
    portName="TransporterPort",
    targetNamespace="http://ws.transporter.upa.pt/",
    serviceName="TransporterService"
    )
public class TransporterPort implements TransporterPortType {

  private String name;
  private int identifierCounter;
  private Map<String, String> locations = new HashMap<String, String>();
  private List<JobView> jobs = new ArrayList<JobView>();
  private Random rn = new Random();
  
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
    }
    else{
      locations.put("Setúbal", "South");
      locations.put("Évora", "South");
      locations.put("Portalegre", "South");
      locations.put("Beja", "South");
      locations.put("Faro", "South");
    }

  }

  @Override
  public String ping(String name) {
    System.out.println("Received Ping from " + name);
    return "Pong " + name + "!";
  }

  @Override
  public JobView requestJob(String origin, String destination, int price)
      throws BadLocationFault_Exception, BadPriceFault_Exception {
    
    JobView budgetJob;
    
    //check Price
    if(price < 0){
      BadPriceFault badPrice = new BadPriceFault();
      badPrice.setPrice(price);
      throw new BadPriceFault_Exception("Requested a Job with price a negative price", badPrice);
    }
    
    //check Origin Location
    if(origin.equals("") || origin == null){
      BadLocationFault badLocation = new BadLocationFault();
      badLocation.setLocation(origin);
      throw new BadLocationFault_Exception("Requested a Job with a origin location unknown", badLocation);
    }
      
    //check Destination Location
    if(destination.equals("") || destination == null) {
      BadLocationFault badLocation = new BadLocationFault();
      badLocation.setLocation(destination);
      throw new BadLocationFault_Exception("Requested a Job with a destination location unknown", badLocation);
    }
    
    //doesn't operate in region 
    if (!(locations.containsKey(origin)) || !(locations.containsKey(destination))) {
    return null;
    }
    
    if(price > 100) {
      return null;
    }
    
    budgetJob = createNewJob(origin, destination);
    
    if(price <= 10) {
      budgetJob.setJobPrice(rn.nextInt(price));
      jobs.add(budgetJob);
      return budgetJob;
    }
    
    else if(price > 10 && price <= 100) {
      budgetJob.setJobPrice(calcPrice(price));
      jobs.add(budgetJob);
      return budgetJob;
    }
    
    //TODO see if it shouldn't throw an Exception
    return null;
  }

  private int calcPrice(int price) {
    if(price % 2 == 0) {
      if(name.matches("UpaTransporter[123456789]*[02468]$")) {
        //transporter and price even
        return rn.nextInt(price);
      }
      else{
        //price even but transporter odd
        //TODO Does the price have to be between price and 100?
        return rn.nextInt(price) + price;
      }
    }
    
    else {
      if((name.matches("UpaTransporter[123456789]*[02468]$"))) {
        //transporter even but price odd
        return rn.nextInt(price) + price;
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
    //TODO Don't know if this is right
    budgetJob.setJobState(budgetJob.getJobState().PROPOSED);
    return budgetJob;
  }
  
 
  private String getIdentifer(){
    identifierCounter++;
    return name + "." + identifierCounter;
  }

  @Override
  public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
    String[] parts = id.split(".");
    int i = Integer.parseInt(parts[parts.length - 1]);
    
    if(jobs.get(i) == null){
      //TODO throw exception
      return null;
    }
    
    if(accept){
      jobs.get(i).setJobState(jobs.get(i).getJobState().ACCEPTED);
    }
    
    else{
      jobs.get(i).setJobState(jobs.get(i).getJobState().REJECTED);
    }
    return jobs.get(i);
  }
  

  @Override
  public JobView jobStatus(String id) {
    String[] parts = id.split(".");
    int i = Integer.parseInt(parts[parts.length - 1]);

    if(jobs.get(i) == null) {
      return null;
    }
    
    //TODO see if this is safe or is needed to use other object
    return jobs.get(i);
    
  }
  

  @Override
  public List<JobView> listJobs() {
    //TODO see if this is safe or if we need to return something else
    return jobs;
  }
  

  @Override
  public void clearJobs() {
    jobs = new ArrayList<JobView>();
    identifierCounter = 0;
  }

}
