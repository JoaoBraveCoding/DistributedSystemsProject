package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class ViewTransportIT extends AbstractBrokerIT{
 
  @Test
  public void broker_viewTransport_success() throws UnknownTransportFault_Exception, InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
	String id = "";
	id = client.requestTransport("Lisboa", "Coimbra", 49);
	System.out.println("TEST: " + id);
	TransportView transport = client.viewTransport(id);
	System.out.println(transport.getState());
    assertEquals(TransportStateView.BOOKED, transport.getState());
  
  }

  protected void populate() {
    client.clearTransports();
    // TODO Auto-generated method stub
  }
}
