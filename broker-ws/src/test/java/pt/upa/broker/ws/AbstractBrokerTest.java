package pt.upa.broker.ws;

import org.junit.*;

public abstract class AbstractBrokerTest {

  @Before
  public void setUp() {
    populate();
  }

  @After
  public void tearDown() {
    //TODO add a way to clean prob with broker.clearTransports
  }

  protected TransportView createTransportView(String tc, String origin, String destination, String identifier, int price) {
    TransportView tw = new TransportView();
    tw.setTransporterCompany(tc);
    tw.setOrigin(origin);
    tw.setDestination(destination);
    tw.setId(identifier);
    tw.setPrice(price);
    tw.setState(TransportStateView.REQUESTED);
    return tw;
  }
  
  protected abstract void populate(); // each test adds its own data
  
}
