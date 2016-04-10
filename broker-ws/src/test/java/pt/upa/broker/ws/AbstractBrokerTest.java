package pt.upa.broker.ws;

import org.junit.*;
import static org.junit.Assert.*;

public abstract class AbstractBrokerTest {

  @Before
  public void setUp() {
    populate();
  }

  @After
  public void tearDown() {
    //TODO add a way to clean prob with broker.clearTransports
  }

  
  protected abstract void populate(); // each test adds its own data
  
}
