package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RequestTransportIT extends AbstractBrokerIT{
 
  @Test
  public void broker_request_transport_success() {
    String result = client.requestTransport("Beja", "Faro", 50);
    assertEquals("Pong Client! (2/2) transporters online/transporters", result);
  }

}
