package pt.upa.transporter.ws.it;

import javax.xml.registry.JAXRException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;

public abstract class AbstractTransporterIT {
  protected static TransporterClient client;
  private static String wsName  = "UpaTransporter1";
  private static String uddiURL = "http://localhost:9090";
//one-time initialization and clean-up

  @BeforeClass
  public static void oneTimeSetUp() throws JAXRException {
      client = new TransporterClient(uddiURL, wsName);
  }

  @AfterClass
  public static void oneTimeTearDown() {
      client = null;
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
}
