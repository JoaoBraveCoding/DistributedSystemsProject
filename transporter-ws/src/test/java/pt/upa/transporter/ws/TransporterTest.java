package pt.upa.transporter.ws;

import org.junit.Test;

import pt.upa.broker.ws.TransportView;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class TransporterTest extends AbstractTransporterTest{

  TransporterPort transporter;
  List<JobView> jobViews = new ArrayList<JobView>();
  
  @Override
  protected void populate(){
    transporter = new TransporterPort("UpaTransporter1");
    JobView jvw = createJobView(compName, id, origin, destination, price); //TODO
    
  }
  
  @Test
  public void transporter_ping_success(){
    
  }

}