package pt.upa.broker.ws;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType;


public class BrokerTest extends AbstractBrokerTest{
  
  BrokerPort broker;
  List<TransportView> transportViews = new ArrayList<TransportView>();

  @Override
  protected void populate() {
    broker = new BrokerPort();
    TransportView tw = createTransportView("UpaTransporter1", "Job1", "Lisboa", "Leiria", 33);
    broker.getTransports().add(tw);
    transportViews.add(tw);

    tw = createTransportView("UpaTransporter1", "Job2", "Lisboa", "Santarém", 40);
    broker.getTransports().add(tw);
    transportViews.add(tw);
    
    tw = createTransportView("UpaTransporter2", "Job3", "Porto", "Santarém", 99);
    broker.getTransports().add(tw);
    transportViews.add(tw);
    
    tw = createTransportView("UpaTransporter3", "Job4", "Setúbal", "Santarém", 1);
    broker.getTransports().add(tw);
    transportViews.add(tw);

  }
  
  @Test
  public void broker_ping_success() {
      String result = broker.ping("Test");
      assertEquals("Pong Test!", result);
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
  
  @Test (expected = InvalidPriceFault_Exception.class)
  public void broker_requestTransport_badPrice() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
      broker.requestTransport("Viseu", "Lisboa", -12);
  }
  
  @Test
  public void broker_viewTransport_success() throws UnknownTransportFault_Exception{
    TransportView tw = broker.viewTransport("Job1");
    assertEquals(true, viewsEquals(tw, 0));
  }
  
  @Test (expected = UnknownTransportFault_Exception.class)
  public void broker_viewTransport_unknownTransport() throws UnknownTransportFault_Exception{
    broker.viewTransport("Job34");
  }
  
  @Test
  public void broker_listTransports_success() {
     List<TransportView> tw = broker.listTransports();
     assertEquals(true, arrayViewsEquals(tw));
  }
  
  @Test
  public void broker_clearTransports_success() {
    broker.clearTransports();
    assertEquals(true, broker.getTransports().isEmpty());
  }  
  
  private boolean viewsEquals(TransportView tw, int i){
    TransportView expected = transportViews.get(i);
    if(tw.getDestination().equals(expected.getDestination()) &&
       tw.getId().equals(expected.getId()) &&
       tw.getOrigin().equals(expected.getOrigin()) &&
       tw.getPrice() == expected.getPrice() &&
       tw.getTransporterCompany() == expected.getTransporterCompany() &&
       tw.getState().equals(expected.getState())){
      return true;
    }
    return false;
  }
  
  private boolean arrayViewsEquals(List<TransportView> tw){
    int counter = 0;
    for(TransportView tv : tw){
      if(!(viewsEquals(tv, counter))){
        return false;
      }
      counter++;
    }
    return true;
  }
  
}
