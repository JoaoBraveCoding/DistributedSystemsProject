package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class DecideJobIT extends AbstractTransporterIT{
  List<JobView> jobViews = new ArrayList<JobView>();
 
  @Test
  public void decide_job_success() throws BadLocationFault_Exception, BadPriceFault_Exception,
  BadJobFault_Exception {
    client.requestJob("Beja", "Faro", 50);
    client.decideJob("UpaTransport1.0", true);
    assertEquals(client.listJobs().get(0), jobViews.get(0));
  }
  
  @Test
  public void decide_job_completed() throws InterruptedException, BadJobFault_Exception{
    client.decideJob("UpaTransporter1.0", true);
    TimeUnit.SECONDS.sleep(12);
    assertTrue(client.listJobs().get(0).getJobState() == JobStateView.COMPLETED);
  }
  
  @Test
  public void decide_job_declined() throws BadLocationFault_Exception, BadPriceFault_Exception, 
  BadJobFault_Exception{
    client.requestJob("Beja", "Faro", 50);
    client.decideJob("UpaTransporter1.0", false);
    assertTrue(client.listJobs().get(0).getJobState() == JobStateView.REJECTED);
  }

}
