package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class JobStatusIT extends AbstractTransporterIT{
 
  @Test
  public void job_status_success() throws BadLocationFault_Exception, BadPriceFault_Exception {
    populate();
    client.requestJob("Beja", "Faro", 50);
    JobView jv = client.jobStatus("UpaTransporter1.0");
    System.out.println( "job status @ job_status_succes:" +jv.getJobState() );
    assertTrue(viewsEquals(jv, 0));
  }
  //test other job request cases?
  
  public void populate(){
    client.clearJobs();
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw); //0
  }
}
