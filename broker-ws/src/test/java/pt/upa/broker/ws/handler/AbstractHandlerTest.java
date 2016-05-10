package pt.upa.broker.ws.handler;

import java.io.ByteArrayInputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import org.junit.AfterClass;
import org.junit.BeforeClass;


/**
 *  Abstract handler test suite
 */
public abstract class AbstractHandlerTest{

    // static members

    /** request-transport SOAP request message captured with LoggingHandler */
    protected static final String RT_SOAP_REQUEST = "<S:Envelope " +
       "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
       "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
       "<SOAP-ENV:Header/>" +
       "<S:Body>" +
       "<ns2:requestTransport xmlns:ns2=\"http://ws.broker.upa.pt/\">" +
       "<origin>Coimbra</origin>" +
       "<destination>Lisboa</destination>" +
       "<price>49</price>" +
       "</ns2:requestTransport>" +
       "</S:Body></S:Envelope>";

    /** request-transport SOAP response message captured with LoggingHandler */
    protected static final String RT_SOAP_RESPONSE = "<S:Envelope ";



    /** SOAP message factory */
    protected static final MessageFactory MESSAGE_FACTORY;

    static {
        try {
            MESSAGE_FACTORY = MessageFactory.newInstance();
        } catch(SOAPException e) {
            throw new RuntimeException(e);
        } 
    }
 

    // helper functions
    protected static SOAPMessage byteArrayToSOAPMessage(byte[] msg) throws Exception {
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(msg);
        StreamSource source = new StreamSource(byteInStream);
        SOAPMessage newMsg = MESSAGE_FACTORY.createMessage();
        SOAPPart soapPart = newMsg.getSOAPPart();
        soapPart.setContent(source);
        return newMsg;
    }


    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }

}
