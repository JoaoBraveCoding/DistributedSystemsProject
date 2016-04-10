package pt.upa.broker.ws;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class BrokerTest extends AbstractBrokerTest{
  
  BrokerPort broker;
  
  @Override
  protected void populate() {
    // TODO Auto-generated method stub
    broker = new BrokerPort();
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
  
  @Test (expected = UnavailableTransportFault_Exception.class)
  public void requestTransport() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
      broker.requestTransport("Viseu", "Lisboa", 12);
  }
  
}
