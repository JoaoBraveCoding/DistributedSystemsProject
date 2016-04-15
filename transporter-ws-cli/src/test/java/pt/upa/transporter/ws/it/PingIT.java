package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PingIT extends AbstractTransporterIT{
 
  @Test
  public void transporter_ping_success() {
    String result = client.ping("Client");
    assertEquals("Pong Client!", result);
  }

}
