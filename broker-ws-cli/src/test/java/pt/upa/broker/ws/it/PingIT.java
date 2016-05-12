package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import javax.xml.registry.JAXRException;

import org.junit.Test;

import pt.upa.broker.ws.exception.UnknownServiceException;

public class PingIT extends AbstractBrokerIT{
 
  @Test
  public void broker_ping_success() {
    String result = "";
    try {
      result = client.ping("Client");
    } catch (JAXRException e) {
      System.out.println(e.getMessage());
    } catch (UnknownServiceException e) {
      System.out.println(e.getMessage());
    }
    assertEquals("Pong Client!", result);
  }

  protected void populate() {
  }

}
