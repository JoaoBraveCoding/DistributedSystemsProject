package pt.upa.broker.ws;

import java.util.List;

import javax.jws.WebService;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
    )
public class BrokerPort implements BrokerPortType {

  @Override
  public String ping(String name) {
    // TODO Auto-generated method stub
    System.out.println("Received ping request with message " + name);
    return "Broker online";
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

}
