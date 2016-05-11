package pt.upa.ws.handler;

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
    protected static final String RT_SOAP_RESPONSE = "<S:Envelope "
        + "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        + "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<SOAP-ENV:Header>"
        + "<e:transporter xmlns:e=\"urn:upa\">UpaTransporter1</e:transporter>"
        + "<e:signature xmlns:e=\"urn:upa\">ZBhEiDqw1P+swmlZOp9K/86qkW9S6AapwT"
        + "ouqQAhXJ9y5MNEheK0V+7EIGKypOXMrHaSWYRdeS+2cNnyAl9sq3l3lCTp9xby65+xG"
        + "9o09RZyEq3OqTq1i4Jv16eSHfLhuVjiSgny6HfK9+1OHemrKDrdYP3qTUKuwoX3ABg6U"
        + "AnR7VEw5JJm0Y4HIhejSXxhZCaChJvzKjhQEg52VKp7ZGmUKCtRTvvBd4c8hPzwJtuuH7"
        + "oFUbJrJYkb+3w5Igp0I4iXXBkioP71D6e2uvaU8UOJm5tyjNm++aykMxgNBNIR9ZiaaMhW"
        + "5kF3FXy8ZKgeiefcQzz4xQfu/bQX3t4oOA==</e:signature>"
        + "<e:nonce xmlns:e=\"urn:upa\">T8SQDS9sZ7+jbgigVDO0rw==</e:nonce>"
        + "</SOAP-ENV:Header>"
        + "<S:Body><ns2:requestJobResponse xmlns:ns2=\"http://ws.transporter.upa.pt/\">"
        + "<return>"
        + "<companyName>UpaTransporter1</companyName>"
        + "<jobIdentifier>UpaTransporter1.0</jobIdentifier>"
        + "<jobOrigin>Coimbra</jobOrigin>"
        + "<jobDestination>Lisboa</jobDestination>"
        + "<jobPrice>32</jobPrice>"
        + "<jobState>PROPOSED</jobState>"
        + "</return>"
        + "</ns2:requestJobResponse>"
        + "</S:Body></S:Envelope>";
    
    /** request-transport SOAP response message captured with LoggingHandler */
    protected static final String RT_SOAP_RESPONSE_TAMPERED = "<S:Envelope "
        + "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        + "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<SOAP-ENV:Header>"
        + "<e:transporter xmlns:e=\"urn:upa\">UpaTransporter1</e:transporter>"
        + "<e:signature xmlns:e=\"urn:upa\">ZBhEiDqw1P+swmlZOp9K/86qkW9S6AapwT"
        + "ouqQAhXJ9y5MNEheK0V+7EIGKypOXMrHaSWYRdeS+2cNnyAl9sq3l3lCTp9xby65+xG"
        + "9o09RZyEq3OqTq1i4Jv16eSHfLhuVjiSgny6HfK9+1OHemrKDrdYP3qTUKuwoX3ABg6U"
        + "AnR7VEw5JJm0Y4HIhejSXxhZCaChJvzKjhQEg52VKp7ZGmUKCtRTvvBd4c8hPzwJtuuH7"
        + "oFUbJrJYkb+3w5Igp0I4iXXBkioP71D6e2uvaU8UOJm5tyjNm++aykMxgNBNIR9ZiaaMhW"
        + "5kF3FXy8ZKgeiefcQzz4xQfu/bQX3t4oOA==</e:signature>"
        + "<e:nonce xmlns:e=\"urn:upa\">T8SQDS9sZ7+jbgigVDO0rw==</e:nonce>"
        + "</SOAP-ENV:Header>"
        + "<S:Body><ns2:requestJobResponse xmlns:ns2=\"http://ws.transporter.upa.pt/\">"
        + "<return>"
        + "<companyName>UpaTransporter1</companyName>"
        + "<jobIdentifier>UpaTransporter1.0</jobIdentifier>"
        + "<jobOrigin>Coimbra</jobOrigin>"
        + "<jobDestination>Lisboa</jobDestination>"
        + "<jobPrice>1</jobPrice>"
        + "<jobState>PROPOSED</jobState>"
        + "</return>"
        + "</ns2:requestJobResponse>"
        + "</S:Body></S:Envelope>";

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
