package pt.upa.transporter.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;


public class TransporterClient {

  private UDDINaming uddiNaming;
  private String endpointAddress;
  private TransporterPortType port;
 
  public TransporterClient(String uddiURL, String name) throws JAXRException {
    setUDDINaming(uddiURL);
    setEndpointAddresss(name);
    createPort();
  }
  
  private void setUDDINaming(String uddiURL) throws JAXRException {
    this.uddiNaming = new UDDINaming(uddiURL);
  }
  
  private void setEndpointAddresss(String name) throws JAXRException {
    System.out.printf("Looking for '%s'%n", name);
    endpointAddress = uddiNaming.lookup(name);    
    if (endpointAddress == null) {
      System.out.println("Not found!");
      //TODO Throw service not found exception
    } else {
      System.out.printf("Found %s%n", endpointAddress);
    }
  }
  
  private void createPort(){
    System.out.println("Creating stub ...");
    TransporterService service = new TransporterService();
    port = service.getTransporterPort();

    System.out.println("Setting endpoint address ...");
    BindingProvider bindingProvider = (BindingProvider) port;
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
  }
  
  public String ping(String string) {
    return port.ping(string);
  }

  public JobView requestJob(String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
    return port.requestJob(origin, destination, price);    
  }

  public List<JobView> listJobs() {
    return port.listJobs();
  }

  public void clearJobs() {
    port.clearJobs();
  }

  public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
    return port.decideJob(id, accept);
  }
  
  public JobView jobStatus(String id) {
    return port.jobStatus(id);
  }

}
