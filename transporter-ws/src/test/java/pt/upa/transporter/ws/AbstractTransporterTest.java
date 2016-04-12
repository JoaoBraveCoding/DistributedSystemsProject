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

 
  protected JobView createJobView(String compName, String id, String origin, String destination, int price) {
    JobView jvw = new JobView();
    jvw.setCompanyName(compName);
    jvw.setJobIdentifier(id);
    jvw.setJobOrigin(origin);
    jvw.setJobDestination(destination);
    jvw.setJobPrice(price);
    switch(type) {
    case
    }
    jvw.setJobState(JobStateView.); //TODO
    return jvw;
  }
  
  protected abstract void populate(); // each test adds its own data
  
}
