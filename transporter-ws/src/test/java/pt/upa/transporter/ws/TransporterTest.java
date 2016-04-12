package pt.upa.transporter.ws;

import org.junit.Test;

import pt.upa.transporter.ws.JobView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class TransporterTest extends AbstractTransporterTest{

  TransporterPort transporter;
  List<JobView> jobViews = new ArrayList<JobView>();
  
  @Override
  protected void populate(){
    transporter = new TransporterPort("UpaTransporter1");
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw);
    
  }
  
  @Test
  public void transporter_ping_success(){
    String result = transporter.ping("Test");
    assertEquals("Pong Test!", result);
  }
  
  //assert within 5 seconds because of jobState
  @Test
  public void good_job_request() throws BadLocationFault_Exception, BadPriceFault_Exception{
    JobView jv2 = transporter.requestJob("Beja", "Faro", 50);
    assertEquals(true, viewsEquals(jv2, 0));
  }

  private boolean viewsEquals(JobView jv, int i){
    JobView expected = jobViews.get(i);
    if(jv.getJobDestination().equals(expected.getJobDestination()) &&
       jv.getCompanyName().equals(expected.getCompanyName()) &&
       jv.getJobIdentifier().equals(expected.getJobIdentifier()) &&
       jv.getJobOrigin().equals(expected.getJobOrigin()) &&
       jv.getJobPrice() > 10 && jv.getJobPrice() <= 100 &&
       jv.getJobState().equals(expected.getJobState())){
      return true;
    }
    return false;
  }
}