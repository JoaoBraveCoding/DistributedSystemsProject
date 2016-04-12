package pt.upa.transporter.ws;

import org.junit.*;

public abstract class AbstractTransporterTest {

  @Before
  public void setUp() {
    populate();
  }

  @After
  public void tearDown() {
    //TODO add a way to clean 
  }

 
  protected JobView createJobView(String compName, String id, String origin, String destination, int price, int type) {
    JobView jvw = new JobView();
    jvw.setCompanyName(compName);
    jvw.setJobIdentifier(id);
    jvw.setJobOrigin(origin);
    jvw.setJobDestination(destination);
    jvw.setJobPrice(price);
    switch(type) {
    case 1:
      jvw.setJobState(JobStateView.ACCEPTED);
      break;
    case 2:
      jvw.setJobState(JobStateView.COMPLETED);
      break;
    case 3:
      jvw.setJobState(JobStateView.HEADING);
      break;
    case 4:
      jvw.setJobState(JobStateView.ONGOING);
      break;
    case 5:
      jvw.setJobState(JobStateView.PROPOSED);
      break;
    case 6:
      jvw.setJobState(JobStateView.REJECTED);
      break;
    default:
      //google's java style
    }
    return jvw;
  }
  
  protected abstract void populate(); // each test adds its own data
  
}
