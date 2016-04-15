package pt.upa.broker.ws;

import org.junit.*;

public abstract class AbstractBrokerCliTest {

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
