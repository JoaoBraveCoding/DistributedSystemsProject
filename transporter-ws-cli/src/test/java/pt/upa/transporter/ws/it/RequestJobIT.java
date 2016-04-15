package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class RequestJobIT extends AbstractTransporterIT{
  List<JobView> jobViews = new ArrayList<JobView>();
 
  @Test
  public void request_job_success() throws BadLocationFault_Exception, BadPriceFault_Exception {
    populate();
    client.requestJob("Beja", "Faro", 50);
    assertEquals(client.listJobs().get(0), jobViews.get(0));//use viewsEquals ? (define in abstract)
  }
  //test other job request cases?
  
  public void populate(){
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw); //0
  }
}
