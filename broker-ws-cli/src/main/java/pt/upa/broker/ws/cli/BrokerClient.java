package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

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
  private String name;
  
  
  public BrokerClient(String uddiURL, String name) throws JAXRException, UnknownServiceException{
    this.uddiURL = uddiURL;
    this.name = name;
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
    
    int connectionTimeout = 10000;
    // The connection timeout property has different names in different versions of JAX-WS
    // Set them all to avoid compatibility issues
    final List<String> CONN_TIME_PROPS = new ArrayList<String>();
    CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
    CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
    CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
    // Set timeout until a connection is established (unit is milliseconds; 0 means infinite)
    for (String propName : CONN_TIME_PROPS)
        requestContext.put(propName, connectionTimeout);
    System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

    int receiveTimeout = 10000;
    // The receive timeout property has alternative names
    // Again, set them all to avoid compability issues
    final List<String> RECV_TIME_PROPS = new ArrayList<String>();
    RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
    RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
    RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
    // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
    for (String propName : RECV_TIME_PROPS)
        requestContext.put(propName, receiveTimeout);
    System.out.printf("Set receive timeout to %d milliseconds%n", receiveTimeout);
  }
  
  public String ping(String string) throws JAXRException, UnknownServiceException {
    
    String result = "";
    try{
      result = port.ping(string);
    } catch(WebServiceException wse){
      System.out.println("Caught: " + wse);
      Throwable cause = wse.getCause();
      if (cause != null && cause instanceof SocketTimeoutException) {
          System.out.println("The cause was a timeout exception: " + cause);
      }
      this.setEndpointAddresss(this.name);
      this.createPort();
      return port.ping(string);
    }
    return result;
  }
  
  public String requestTransport(String origin, String destination, int price) throws 
  InvalidPriceFault_Exception, UnavailableTransportFault_Exception, 
  UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception, JAXRException, UnknownServiceException{
    String result = "";
    try{
      result = port.requestTransport(origin, destination, price);
    } catch(WebServiceException wse){
      System.out.println("Caught: " + wse);
      Throwable cause = wse.getCause();
      if (cause != null && cause instanceof SocketTimeoutException) {
          System.out.println("The cause was a timeout exception: " + cause);
      }
      this.setEndpointAddresss(this.name);
      this.createPort();
      return port.requestTransport(origin, destination, price);
    }
    return result;
  }
  
  public TransportView viewTransport(String id) throws UnknownTransportFault_Exception, JAXRException, UnknownServiceException{
    TransportView result =  null;
    try{
      result =  port.viewTransport(id);
    } catch(WebServiceException wse){
      System.out.println("Caught: " + wse);
      Throwable cause = wse.getCause();
      if (cause != null && cause instanceof SocketTimeoutException) {
          System.out.println("The cause was a timeout exception: " + cause);
      }
      this.setEndpointAddresss(this.name);
      this.createPort();
      return port.viewTransport(id);
    }
    return result;
  }
  
  public  List<TransportView> listTransports() throws JAXRException, UnknownServiceException{
    List<TransportView> result = null;
    try{
      result =  port.listTransports();
    } catch(WebServiceException wse){
      System.out.println("Caught: " + wse);
      Throwable cause = wse.getCause();
      if (cause != null && cause instanceof SocketTimeoutException) {
          System.out.println("The cause was a timeout exception: " + cause);
      }
      this.setEndpointAddresss(this.name);
      this.createPort();
      return port.listTransports();
    }
    return result;
  }
  
  public void clearTransports() throws JAXRException, UnknownServiceException{
    try{
      port.clearTransports();
    } catch(WebServiceException wse){
      System.out.println("Caught: " + wse);
      Throwable cause = wse.getCause();
      if (cause != null && cause instanceof SocketTimeoutException) {
          System.out.println("The cause was a timeout exception: " + cause);
      }
      this.setEndpointAddresss(this.name);
      this.createPort();
      port.clearTransports();
    }
  }
  
}
