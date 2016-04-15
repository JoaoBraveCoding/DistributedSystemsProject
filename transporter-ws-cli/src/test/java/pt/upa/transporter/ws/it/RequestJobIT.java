package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class RequestJobIT extends AbstractTransporterIT{
 
  @Test
  public void request_job_success() throws BadLocationFault_Exception, BadPriceFault_Exception {
    populate();
    client.requestJob("Beja", "Faro", 50);
    assertTrue(viewsEquals(client.listJobs().get(0), 0));
  }
  //test other job request cases?
  
  @Test (expected = BadLocationFault_Exception.class)
  public void job_request_empty_origin() throws BadLocationFault_Exception, BadPriceFault_Exception{
    client.requestJob("", "Faros", 50);
  }
  
  @Test(expected = BadLocationFault_Exception.class)
  public void job_request_with_empty_destination() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    client.requestJob("Beja", "", 50);
  }
  
  @Test
  public void job_request_not_operable_origin() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    JobView jv = client.requestJob("Porto", "Faro", 50);
    assertNull(jv);
  }
  
  @Test
  public void job_request_not_operable_destination() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    JobView jv = client.requestJob("Beja", "Porto", 50);
    assertNull(jv);
  }
  
  @Test
  public void overpriced_job_request() throws BadLocationFault_Exception, BadPriceFault_Exception{
    JobView jv = client.requestJob("Beja", "Faro", 120);
    assertNull(jv);
  }
  
  @Test
  public void underpriced_job_request() throws BadLocationFault_Exception, BadPriceFault_Exception{
    JobView jv = client.requestJob("Beja", "Faro", 5);
    assertTrue(jv.getJobPrice() < 5 && jv.getJobPrice() >= 0);
  }
  
  @Test
  public void odd_number_price_even_transporter() throws BadLocationFault_Exception, BadPriceFault_Exception{
    JobView jv3 = client2.requestJob("Porto","Braga", 51);
    assertTrue(jv3.getJobPrice() > 51);
  }
  
  @Test
  public void odd_number_price_request() throws BadLocationFault_Exception, BadPriceFault_Exception{
    JobView jv3 = client.requestJob("Beja", "Faro", 51);
    assertTrue(jv3.getJobPrice() < 51);
  }
  
  @Test
  public void odd_number_price_odd_transporter_request() throws BadLocationFault_Exception, 
  BadPriceFault_Exception{
    JobView jv3 = client.requestJob("Beja", "Faro", 51);
    assertTrue(jv3.getJobPrice() < 51);
  }
  
  @Test (expected = BadPriceFault_Exception.class)
  public void negative_number_price_request() throws BadLocationFault_Exception, BadPriceFault_Exception{
    client.requestJob("Beja", "Faro",  -50);
  }
  
  /*@Test (expected = com.sun.xml.ws.fault.ServerSOAPFaultException.class)
  public void null_origin() throws BadLocationFault_Exception, BadPriceFault_Exception{
    client.requestJob(null, "Faro", 50);
  }
  
  @Test (expected = com.sun.xml.ws.fault.ServerSOAPFaultException.class)
  public void null_destination() throws BadLocationFault_Exception, BadPriceFault_Exception{
    client.requestJob("Beja", null, 50);
  }
  */
  public void populate(){
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw); //0
  }
}
