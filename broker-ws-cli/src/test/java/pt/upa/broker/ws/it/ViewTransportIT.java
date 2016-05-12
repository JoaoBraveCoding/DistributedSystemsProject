package pt.upa.broker.ws.it;

import static org.junit.Assert.assertTrue;

import javax.xml.registry.JAXRException;

import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportStateView;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;
import pt.upa.broker.ws.exception.UnknownServiceException;

public class ViewTransportIT extends AbstractBrokerIT {
 
  @Test
  public void broker_viewTransport_success() throws UnknownTransportFault_Exception, InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception, JAXRException, UnknownServiceException {
	String id = "";
	boolean tmp = false;
	id = client.requestTransport("Lisboa", "Coimbra", 49);
	TransportView transport = client.viewTransport(id);
	if(transport.getState().equals(TransportStateView.HEADING) || transport.getState().equals(TransportStateView.BOOKED)){
	  tmp = true;
	}
  assertTrue(tmp);
  
  }

  protected void populate() {
    try {
      client.clearTransports();
    } catch (JAXRException | UnknownServiceException e) {
      System.out.println(e.getMessage());
    }
  }
}
