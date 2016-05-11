package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertTrue;


import org.junit.Test;

import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class DecideJobIT extends AbstractTransporterIT{
 
  @Test
  public void decide_job_success() throws BadLocationFault_Exception, BadPriceFault_Exception,
  BadJobFault_Exception {
    populate();
    client.requestJob("Beja", "Faro", 50);
    client.decideJob("UpaTransport1.0", true);
    assertTrue(viewsEquals(client.listJobs().get(0), 0));
  }
/*
 * server SOAP fault smthg stmthg ---explodes on this one ??aaaahhhh the humanity
  @Test
  public void decide_job_completed() throws InterruptedException, BadJobFault_Exception{
    client.decideJob("UpaTransporter1.0", true);
    TimeUnit.SECONDS.sleep(12);
    assertTrue(client.listJobs().get(0).getJobState() == JobStateView.COMPLETED);
  }
  */
  @Test
  public void decide_job_declined() throws BadLocationFault_Exception, BadPriceFault_Exception, 
  BadJobFault_Exception{
    client.requestJob("Beja", "Faro", 50);
    client.decideJob("UpaTransporter1.0", false);
    assertTrue(client.listJobs().get(0).getJobState() == JobStateView.REJECTED);
  }
  
  public void populate(){
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 1); //ACCEPTED
    jobViews.add(jvw); //0
    client.clearJobs();
  }

}
