package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
    )
public class BrokerPort implements BrokerPortType {

  private List<TransporterPortType> transporters = new ArrayList<TransporterPortType>();
  
  @Override
  public String ping(String name) {
    String returnValue;
    System.out.println("Received ping from " + name);
    
    //Pingging transporters 
    System.out.println("Pingging transporters");
    for(TransporterPortType port : transporters){
      returnValue = port.ping("Broker");
      System.out.println(returnValue);
    }
    
    return "Pong " + name + "!";
  }

  @Override
  public String requestTransport(String origin, String destination, int price) throws InvalidPriceFault_Exception,
      UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TransportView> listTransports() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clearTransports() {
    // TODO Auto-generated method stub
    
  }

	// TODO
   
  public List<TransporterPortType> getTransporters(){
    return this.transporters;
  }
  
  public void addTransporter(String name, UDDINaming uddiNaming) throws JAXRException{
    String endpointAddress = name;
    if (endpointAddress == null) {
      System.out.println("Not found!");
      return;
    }
    TransporterService service = new TransporterService();
    TransporterPortType port = service.getTransporterPort();
    
    BindingProvider bindingProvider = (BindingProvider) port;
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

    transporters.add(port);
  }
  
}
