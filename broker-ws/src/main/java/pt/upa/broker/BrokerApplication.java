package pt.upa.broker;

import java.util.Collection;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.broker.ws.BrokerPort;


public class BrokerApplication {
  private static BrokerPort broker;
	public static void main(String[] args) throws Exception {
    //Check arguments
    if (args.length < 3) {
      System.err.println("Argument(s) misssing!");
      System.err.printf("Usage: java %s uddiURL wsName wsURL ca-wsName%n", BrokerApplication.class.getName());
    }

    System.out.println(BrokerApplication.class.getSimpleName() + " starting...");
  		    
    String uddiURL = args[0];
    String name    = args[1];
    String url     = args[2];
    //String ca   = args[3];
    String number = args[4];
    
    boolean primaryBroker = false;
    if(number.equals("1")){
      System.out.println("I'm a primary broker");
      primaryBroker = true;
    } else {
      System.out.println("I'm a secondary broker");
    }
    
    Endpoint endpoint = null;
    UDDINaming uddiNaming = null;
    try {
      broker = new BrokerPort();
      endpoint = Endpoint.create(broker); // TODO check if successful
      
      //publish endpoint
      System.out.printf("Starting %s%n", url);
      endpoint.publish(url);
      
      
      //publish to UDDI
      System.out.printf("publishing '%s' to UDDI at %s%n", name, uddiURL);
      uddiNaming = new UDDINaming(uddiURL);
      
      Collection<String> resultUDDIList;
      resultUDDIList = uddiNaming.list("UpaTransporter%");
      if(resultUDDIList == null || resultUDDIList.isEmpty()){
        System.out.println("Did not get a list of transporters from UDDI");
      }
      
      //Adding transporter to broker
      System.out.println("Adding transporters...");
      for(String s : resultUDDIList)
        broker.addTransporter(s, uddiNaming);
      
      //Adding ca to broker
      //String caEndpointAddress = uddiNaming.lookup(ca); 
      //broker.addCa(caEndpointAddress);
      
      uddiNaming.rebind(name, url);

      //wait
      System.out.println("Awating connections");
      System.out.println("Press enter to shutdown");
      System.in.read();
      
    } catch(Exception e) {
      System.out.printf("Caught exception: %s%n", e);
      e.printStackTrace();
    
    } finally {
      try {
        if (endpoint != null) {
          //stop endpoint
          endpoint.stop();
          System.out.printf("Stopped %s%n", url);
        }
      } catch (Exception e) {
        System.out.printf("Caught exception when stopping: %s%n", e);
      }
      try {
        if (uddiNaming != null) {
          // delete from uddi
          uddiNaming.unbind(name);
          System.out.printf("Deleted '%s' from UDDI%n", name);
          }    
        } catch (Exception e) {
          System.out.printf("Caught exception when deleting: %s%n", e);
      }
    }
  }
}
