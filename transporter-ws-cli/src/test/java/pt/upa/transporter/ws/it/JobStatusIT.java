package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

public class JobStatusIT extends AbstractTransporterIT{
  List<JobView> jobViews = new ArrayList<JobView>();
 
  @Test
  public void job_status_success() throws BadLocationFault_Exception, BadPriceFault_Exception {
    populate();
    client.requestJob("Beja", "Faro", 50);
    JobView jv = client.jobStatus("UpaTransporter1.0");
    assertEquals(jv, jobViews.get(0));//use viewsEquals ? (define in abstract)
  }
  //test other job request cases?
  
  public void populate(){
    JobView jvw = createJobView("UpaTransporter1", "UpaTransporter1.0", "Beja", "Faro", 50, 5); //PROPOSED
    jobViews.add(jvw); //0
  }
}
