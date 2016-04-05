package pt.upa.broker;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Collection;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.broker.ws.BrokerPort;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

public class BrokerApplication {
  private static BrokerPort broker;
	public static void main(String[] args) throws Exception {
    //Check arguments
    if (args.length < 3) {
      System.err.println("Argument(s) misssing!");
      System.err.printf("Usage: java %s uddiURL wsName wsURL%n", BrokerApplication.class.getName());
    }

    System.out.println(BrokerApplication.class.getSimpleName() + " starting...");
  		    
    String uddiURL = args[0];
    String name    = args[1];
    String url     = args[2];
    
    Endpoint endpoint = null;
    UDDINaming uddiNaming = null;
    try {
      broker = new BrokerPort();
      endpoint = Endpoint.create(broker);
      
      //publish endpoint
      System.out.printf("Starting %s%n", url);
      endpoint.publish(url);
      
      //publish to UDDI
      System.out.printf("publishing '%s' to UDDI at %s%n", name, uddiURL);
      uddiNaming = new UDDINaming(uddiURL);
      
      //TODO ask professor if why does the uddiNaming.list does not work
//      Collection<String> resultUDDIList;
//      resultUDDIList = uddiNaming.list("UpaTransporter1");
//      if(resultUDDIList == null || resultUDDIList.isEmpty()){
//        System.out.println("Did not get a list of transporters from UDDI");
//      }
      
      //TODO ADD for to add all the transporters
      //Adding transporter to broker
      System.out.println("Adding transporters...");
      broker.addTransporter("Something", uddiNaming);
      
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
