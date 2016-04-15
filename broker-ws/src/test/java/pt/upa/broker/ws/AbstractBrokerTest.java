package pt.upa.broker.ws;

import org.junit.*;

import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public abstract class AbstractBrokerTest {

  @Before
  public void setUp() {
    populate();
  }

  @After
  public void tearDown() {
    //TODO add a way to clean prob with broker.clearTransports
  }

  protected TransportView createTransportView(String tc, String identifier, String origin, String destination, int price) {
    TransportView tw = new TransportView();
    tw.setTransporterCompany(tc);
    tw.setOrigin(origin);
    tw.setDestination(destination);
    tw.setId(identifier);
    tw.setPrice(price);
    tw.setState(TransportStateView.REQUESTED);
    return tw;
  }
  
  protected JobView createJobView(String tc, String identifier, String origin, String destination, int price) {
    JobView jw = new JobView();
    jw.setCompanyName(tc);
    jw.setJobOrigin(origin);
    jw.setJobDestination(destination);
    jw.setJobIdentifier(identifier);
    jw.setJobPrice(price);
    jw.setJobState(JobStateView.PROPOSED);
    return jw;
  }
  
  protected abstract void populate(); // each test adds its own data
  
}
