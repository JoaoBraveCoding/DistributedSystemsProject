package pt.upa.broker.ws.it;

import javax.xml.registry.JAXRException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.broker.ws.exception.UnknownServiceException;

public abstract class AbstractBrokerIT {
  protected static BrokerClient client;
  private static String wsName  = "UpaBroker";
  private static String uddiURL = "http://localhost:9090";
 //one-time initialization and clean-up
  
  protected abstract void populate(); // each test adds its own data


  @BeforeClass
  public static void oneTimeSetUp() throws JAXRException, UnknownServiceException {
      client = new BrokerClient(uddiURL, wsName);
  }

  @AfterClass
  public static void oneTimeTearDown() { 
      client = null;
  }

}
