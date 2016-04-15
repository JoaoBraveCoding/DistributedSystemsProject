package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class ClearJobsIT extends AbstractTransporterIT{
  
  @Test
  public void clear_jobs_success() throws BadLocationFault_Exception, BadPriceFault_Exception{
    client.requestJob("Beja", "Faro", 50);
    client.clearJobs();
    assertTrue(client.listJobs().size() == 0);
  }
  
  public void populate(){
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw); //0
  }
}
