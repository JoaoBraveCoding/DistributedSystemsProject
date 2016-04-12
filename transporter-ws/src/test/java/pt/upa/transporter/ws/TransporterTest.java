package pt.upa.transporter.ws;

import org.junit.Test;

import pt.upa.transporter.ws.JobView;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class TransporterTest extends AbstractTransporterTest{

  TransporterPort transporter;
  List<JobView> jobViews = new ArrayList<JobView>();
  
  @Override
  protected void populate(){
    transporter = new TransporterPort("UpaTransporter1");
    //JobView jvw = createJobView("UpaTransporter1", "Job1", origin, destination, price); //TODO
    
  }
  
  @Test
  public void transporter_ping_success(){
    String result = transporter.ping("Test");
    assertEquals("Pong Test!", result);
  }

}