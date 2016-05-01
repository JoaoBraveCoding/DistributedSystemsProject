package pt.upa.ca.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.UnknownServiceException;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.upa.ca.ws.CaService;
import pt.upa.ca.ws.CaPortType;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


public class CaClient {
  
  private UDDINaming uddiNaming;
  private String endpointAddress;
  private CaPortType port;
  private String uddiURL;
  
  
  public CaClient(String uddiURL, String name) throws JAXRException, UnknownServiceException{
    this.uddiURL = uddiURL;
    setUDDINaming(uddiURL);
    setEndpointAddresss(name);
    createPort();
  }

  private void setUDDINaming(String uddiURL) throws JAXRException, UnknownServiceException {
    try{
      this.uddiNaming = new UDDINaming(uddiURL);
    } catch (JAXRException e) {UnknownServiceException ex = new UnknownServiceException("Client failed lookup on UDDI at " + uddiURL + "!");
                                ex.initCause(new JAXRException());
                                throw ex;}
  }
  
  private void setEndpointAddresss(String name) throws JAXRException, UnknownServiceException {
    try{
      System.out.printf("Looking for '%s'%n", name);
      endpointAddress = uddiNaming.lookup(name);    
      if (endpointAddress == null) {
        System.out.println("Not found!");
        throw new UnknownServiceException("Service with name " + name + " not found on UDDI at " + 
                  uddiURL);
      } else {
        System.out.printf("Found %s%n", endpointAddress);
      }
    } catch (JAXRException e) {UnknownServiceException ex = new UnknownServiceException("Client failed lookup on UDDI at " + uddiURL + "!");
                                ex.initCause(new JAXRException());
                                throw ex;}
  }
  
  private void createPort(){
    System.out.println("Creating stub ...");
    CaService service = new CaService();
    port = service.getCaPort();

    System.out.println("Setting endpoint address ...");
    BindingProvider bindingProvider = (BindingProvider) port;
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
  }
  
  public String ping(String string) {
    return port.ping(string);
  }
  
}
