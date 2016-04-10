package pt.upa.broker.ws;

import org.junit.*;
import static org.junit.Assert.*;

public abstract class AbstractBrokerTest {

  @Before
  public void setUp() {
    populate();
  }

  @After
  public void tearDown() {
    //TODO add a way to clean prob with broker.clearTransports
  }

  protected void createTransportView(String tc, String string2, String string3, String string4, int i) {
    TransportView tw = new TransportView();
    tw.setTransporterCompany(tc);
    tw.setDestination("Lisboa");
    tw.setOrigin("Leiria");
    tw.setId("Job");
    tw.setPrice(33);
    tw.setState(tw.getState().REQUESTED);
    
  }
  
  protected abstract void populate(); // each test adds its own data
  
}
