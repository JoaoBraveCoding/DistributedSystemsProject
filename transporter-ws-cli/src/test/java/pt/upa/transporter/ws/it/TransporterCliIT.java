package pt.upa.transporter.ws.it;

import org.junit.*;

import pt.upa.transporter.ws.cli.TransporterClient;

import static org.junit.Assert.*;

import javax.xml.registry.JAXRException;

/**
 *  Integration Test example
 *  
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers 
 */
public class TransporterCliIT {

    // static members


    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members


    // initialization and clean-up for each test

    @Before
    public void setUp() throws JAXRException {
      TransporterClient tc = new TransporterClient("uddiURL", "UpaT");//TODO re check
    }

    @After
    public void tearDown() {
    }


    // tests

    @Test
    public void test() {

        // assertEquals(expected, actual);
        // if the assert fails, the test fails
    }

}