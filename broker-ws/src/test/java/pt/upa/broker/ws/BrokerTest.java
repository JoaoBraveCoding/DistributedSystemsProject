package pt.upa.broker.ws;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class BrokerTest extends AbstractBrokerTest{
  
  BrokerPort broker;
  
  @Override
  protected void populate() {
    broker = new BrokerPort();
    TransportView tw = createTransportView("UpaTransporter1", "Job", "Lisboa", "Leiria", 33);
    broker.getTransports().add(tw);
    //FIXME add mores transports so I can test properly the other methods
  }
  

  @Test
  public void broker_ping_success() {
      String result = broker.ping("Test");
      assertEquals("Pong Test! (0/0) transporters online/transporters", result);
  }
  
  @Test (expected = UnknownLocationFault_Exception.class)
  public void broker_requestTransport_noOrigin() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
      broker.requestTransport("", "Lisboa", 12);
  }

  @Test (expected = UnknownLocationFault_Exception.class)
  public void broker_requestTransport_noDestination() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
      broker.requestTransport("Lisboa", "", 12);
  }
  
  @Test (expected = UnknownLocationFault_Exception.class)
  public void broker_requestTransport_destinationUnknown() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
      broker.requestTransport("Lisboa", "Torres Vedras", 12);
  }
  
  @Test (expected = UnknownLocationFault_Exception.class)
  public void broker_requestTransport_originUnknown() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
      broker.requestTransport("Torres Vedras", "Lisboa", 12);
  }
  
  @Test (expected = UnavailableTransportFault_Exception.class)
  public void broker_requestTransport_noTransporter() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
      broker.requestTransport("Viseu", "Lisboa", 12);
  }
  
  @Test
  public void broker_clearTransports_success() {
      broker.clearTransports();
      //FIXME what should be asserted in this operation
      //assertEquals("Pong Test! (0/0) transporters online/transporters", broker.getTransporters().isEmpty());
  }  
  
  
}
