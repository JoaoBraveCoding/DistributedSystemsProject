package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;
import pt.upa.broker.ws.exception.UnknownServiceException;

public class BrokerClient {
  
  private UDDINaming uddiNaming;
  private String endpointAddress;
  private BrokerPortType port;
  private String uddiURL;
  
  
  public BrokerClient(String uddiURL, String name) throws JAXRException, UnknownServiceException{
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
  
  public void requestTransport(String origin, String destination, int price) throws 
  InvalidPriceFault_Exception, UnavailableTransportFault_Exception, 
  UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
    port.requestTransport(origin, destination, price);
  }
  
  public  TransportView viewTransport(String id) throws UnknownTransportFault_Exception{
    return port.viewTransport(id);
  }
  
  public  List<TransportView> listTransports(){
    return port.listTransports();
  }
  
  public void clearTransports(){
    port.clearTransports();
  }
  
}
