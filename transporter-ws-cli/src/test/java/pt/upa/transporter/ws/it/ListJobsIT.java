package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class ListJobsIT extends AbstractTransporterIT{

  @Test
  public void list_jobs_success() throws BadLocationFault_Exception, BadPriceFault_Exception{
    populate();
    client.requestJob("Beja", "Faro", 50);
    JobView jv = client.listJobs().get(0);
    assertTrue(viewsEquals(jv, 0));
    client.clearJobs();
    assertTrue(client.listJobs().size() == 0);
  }
  
  public void populate(){
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw); //0
  }
}
