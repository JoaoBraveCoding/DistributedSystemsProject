package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;

public class BrokerClient {
  
  private String uddiURL;
  private UDDINaming uddiNaming;
  private String endpointAddress;
  private BrokerPortType port;
  
  public BrokerClient(String uddiURL, String name) throws JAXRException{
    this.uddiURL = uddiURL;
    setUDDINaming(this.uddiURL);
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

  
  public void createPort(){
    System.out.println("Creating stub ...");
    BrokerService service = new BrokerService();
    port = service.getBrokerPort();

    System.out.println("Setting endpoint address ...");
    BindingProvider bindingProvider = (BindingProvider) port;
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
  }
  
  public String ping(String string) {
    return port.ping(string);
  }
  
}
