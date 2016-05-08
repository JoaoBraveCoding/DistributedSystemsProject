package pt.upa.transporter;

import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.TransporterPort;

public class TransporterApplication {
	public static void main(String[] args) throws Exception {
		//Check arguments
	  if (args.length < 3) {
		  System.err.println("Argument(s) misssing!");
		  System.err.printf("Usage: java %s uddiURL wsName wsURL%n", TransporterApplication.class.getName());
		}
	
	  System.out.println(TransporterApplication.class.getSimpleName() + " starting...");
		
	  String uddiURL = args[0];
	  String name    = args[1];
	  String url     = args[2];
	  
		Endpoint endpoint = null;
		UDDINaming uddiNaming = null;
		TransporterPort tp = new TransporterPort(name);
		try {
		  endpoint = Endpoint.create(tp);
		  
	    //publish endpoint
		  System.out.printf("Starting %s%n", url);
		  endpoint.publish(url);
		  
		  //publish to UDDI
		  System.out.printf("publishing '%s' to UDDI at %s%n", name, uddiURL);
		  uddiNaming = new UDDINaming(uddiURL);
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
		      //stop endpoin
		      tp.stopTimer();
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
