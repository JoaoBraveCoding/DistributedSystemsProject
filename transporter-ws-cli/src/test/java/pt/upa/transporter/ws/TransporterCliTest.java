package pt.upa.transporter.ws;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.TransporterClient;

import static org.junit.Assert.assertEquals;

import javax.xml.registry.JAXRException;

import org.junit.runner.RunWith;
import mockit.*;
import mockit.integration.junit4.JMockit;

import org.junit.Test;

@RunWith(JMockit.class)
public class TransporterCliTest extends AbstractTransporterCliTest{

  TransporterClient tc = null;
  UDDINaming fuddin = null;

  protected void populate() throws JAXRException{
    fuddin = new UDDINaming(null);
    //tc = new TransporterClient(uddiURL, name);
    
  }
  
  
  @Test
  public void create_transporter_client(){
    new MockUp<TransporterClient>() {
      @Mock
      void setUDDINaming() {
        //this.uddiNaming = fuddin;//fake uddiNaming
      }
      
      @Mock
      void setEndpointAdress(){
        
      }
    };
  }
  
  /*
  @Test
  public void ping(){
    new MockUp<Resolve>() {
      @Mock
      double sqrt(double x) {
        assertEquals("sqrt argument", x, 9.0, 1e-9);
        return 3.0; }
    };
    Resolve r = new Resolve(2, 5, 2);
    assertEquals("first root", -0.5, r.resolve(), 1e-9);
    assertEquals("second root", -2.0, r.other(), 1e-9);
  }
  */
  
  //fazer com que uddinaming.lookup(name) devolva um bom endpointadress
  
  //mock um transporterport?
  
  //mock um transporter service?
  

}