package pt.upa.broker.ws.it;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class RequestTransportIT extends AbstractBrokerIT{
 
	@Test(expected = InvalidPriceFault_Exception.class)
	  public void broker_viewTransport_incorrectPrice() throws UnknownTransportFault_Exception, 
	  InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		client.requestTransport("Lisboa", "Coimbra", -10);
	  }
	
	@Test(expected = UnknownLocationFault_Exception.class)
	  public void broker_viewTransport_incorrectDestiny() throws UnknownTransportFault_Exception, 
	  InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		client.requestTransport("Lisboa", "Japão", 49);
	  }
	
	@Test(expected = UnknownLocationFault_Exception.class)
	  public void broker_viewTransport_incorrectOrigin() throws UnknownTransportFault_Exception, 
	  InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		client.requestTransport("Japão","Lisboa", 49);
	  }
  
	@Test
	  public void broker_viewTransport_success() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception  {
		String id = client.requestTransport("Coimbra","Lisboa", 49);
		System.out.println("TRANSPORT ID: " + id);
	  }
}
