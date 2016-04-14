package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PingIT extends AbstractBrokerIT{
 
  @Test
  public void broker_ping_success() {
    String result = client.ping("Client");
    assertEquals("Pong Client! (2/2) transporters online/transporters", result);
  }

}
