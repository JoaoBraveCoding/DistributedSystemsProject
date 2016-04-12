package pt.upa.transporter.ws;

import org.junit.Test;

import pt.upa.transporter.ws.JobView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

public class TransporterTest extends AbstractTransporterTest{

  TransporterPort transporter;
  TransporterPort transporter2;
  List<JobView> jobViews = new ArrayList<JobView>();
  
  @Override
  protected void populate(){
    transporter = new TransporterPort("UpaTransporter1");
    transporter2 = new TransporterPort("UpaTransporter2");
    
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw); //0
    jvw = createJobView("UpaTransporter2", "UpaTransporter2.0", "Porto", "Braga", 50, 5); //PROPOSED
    jobViews.add(jvw); //1

  }
  
  @Test
  public void transporter_ping_success(){
    String result = transporter.ping("Test");
    assertEquals("Pong Test!", result);
  }
  
  @Test
  public void good_job_request() throws BadLocationFault_Exception, BadPriceFault_Exception{
    JobView jv2 = transporter.requestJob("Beja", "Faro", 50);
    assertTrue(viewsEquals(jv2, 0));
  }

  @Test
  public void good_job_request_transp2() throws BadLocationFault_Exception, BadPriceFault_Exception{
    JobView jv2 = transporter2.requestJob("Porto", "Braga", 50);
    assertTrue(viewsEquals(jv2, 1));
  }

  @Test (expected = BadLocationFault_Exception.class)
  public void job_request_with_empty_origin() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    transporter.requestJob("", "Beja", 50);
  }
  
  @Test (expected = BadLocationFault_Exception.class)
  public void job_request_with_empty_destination() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    transporter.requestJob("Beja", "", 50);
  }
  
  @Test
  public void job_request_with_not_operable_origin() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    JobView jv3 = transporter.requestJob("Porto", "Beja", 50);
    assertNull(jv3);
  }
  
  @Test
  public void job_request_with_not_operable_destination() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    JobView jv3 = transporter.requestJob("Beja", "Porto", 50);
    assertNull(jv3);
  }
  
  @Test
  public void overpriced_job_request() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    JobView jv3 = transporter.requestJob("Beja", "Faro", 120);
    assertNull(jv3);
  }

  @Test
  public void underpriced_job_request() throws BadLocationFault_Exception,
  BadPriceFault_Exception{
    JobView jv3 = transporter.requestJob("Beja",  "Faro",  5);
    assertTrue(jv3.getJobPrice() < 5 && jv3.getJobPrice() >= 0);
  }
  


  private boolean viewsEquals(JobView jv, int i){
    JobView expected = jobViews.get(i);
    if(jv.getJobDestination().equals(expected.getJobDestination()) &&
       jv.getCompanyName().equals(expected.getCompanyName()) &&
       jv.getJobIdentifier().equals(expected.getJobIdentifier()) &&
       jv.getJobOrigin().equals(expected.getJobOrigin()) &&
       expected.getJobPrice() > 10 && expected.getJobPrice() <= 100 &&
       jv.getJobState().equals(expected.getJobState())){
      return true;
    }
    return false;
  }
}