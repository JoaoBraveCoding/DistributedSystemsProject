package pt.upa.broker.ws.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.Test;

import mockit.Mocked;
import mockit.StrictExpectations;
import pt.upa.ws.SecurityFunctions;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

/**
 *  Handler test suite
 */
public class HeaderHandlerTest extends AbstractHandlerTest {

    // tests

    @Test
    public void testHeaderHandlerOutbound(
        @Mocked final SOAPMessageContext soapMessageContext)
        throws Exception {

        // Preparation code not specific to JMockit, if any.
        final String soapText = RT_SOAP_REQUEST;

        final SOAPMessage soapMessage = byteArrayToSOAPMessage(soapText.getBytes());
        final Boolean soapOutbound = true;

        // an "expectation block"
        // One or more invocations to mocked types, causing expectations to be recorded.
        new StrictExpectations() {{
            soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            result = soapOutbound;

            soapMessageContext.getMessage();
            result = soapMessage;
        }};

        // Unit under test is exercised.
        HeaderHandler handler = new HeaderHandler();
        boolean handleResult = handler.handleMessage(soapMessageContext);

        // Additional verification code, if any, either here or before the verification block.

        // assert that message would proceed normally
        assertTrue(handleResult);

        // assert header
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        assertNotNull(soapHeader);

        // assert header element
        Name signature = soapEnvelope.createName("signature", "e", "urn:upa");

        Iterator it = soapHeader.getChildElements(signature);
        assertTrue(it.hasNext());

        // assert header signature
        SOAPElement element = (SOAPElement) it.next();
       
        //obtaining signature
        String valueString = element.getValue();
        byte[] signatureByte = parseBase64Binary(valueString);
        
        //obtaining nonce
        Name nonceName = soapEnvelope.createName("nonce", "e", "urn:upa");
        it = soapHeader.getChildElements(nonceName);
        assertTrue(it.hasNext());
        
        element = (SOAPElement) it.next();
        valueString = element.getValue();
        byte[] nonce = parseBase64Binary(valueString);
        
        //obtaining plaintext
        SOAPBody sb = soapEnvelope.getBody();                
        it = sb.getChildElements();
        assertTrue(it.hasNext());
        
        element = (SOAPElement) it.next();
        String msg = element.getTextContent();
        
        //digest msg + nonce
        byte[] bytes = SecurityFunctions.digestBroker(msg, nonce);
        
        Certificate cer = SecurityFunctions.readCertificateFile("../ca-ws/keys/UpaBroker.cer");
        PublicKey pub = SecurityFunctions.getPublicKeyFromCertificate(cer);
        
        assertTrue(SecurityFunctions.verifyDigitalSignature(signatureByte, bytes, pub));

        //soapMessage.writeTo(System.out);
    }

  /*  @Test
    public void testHeaderHandlerInbound(
        @Mocked final SOAPMessageContext soapMessageContext)
        throws Exception {

        // Preparation code not specific to JMockit, if any.
        final String soapText = HELLO_SOAP_REQUEST.replace("<SOAP-ENV:Header/>",
            "<SOAP-ENV:Header>" +
            "<d:myHeader xmlns:d=\"http://demo\">22</d:myHeader>" +
            "</SOAP-ENV:Header>");
        //System.out.println(soapText);

        final SOAPMessage soapMessage = byteArrayToSOAPMessage(soapText.getBytes());
        final Boolean soapOutbound = false;

        // an "expectation block"
        // One or more invocations to mocked types, causing expectations to be recorded.
        new StrictExpectations() {{
            soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            result = soapOutbound;

            soapMessageContext.getMessage();
            result = soapMessage;

            soapMessageContext.put(HeaderHandler.CONTEXT_PROPERTY, 22);
            soapMessageContext.setScope(HeaderHandler.CONTEXT_PROPERTY, Scope.APPLICATION);
        }};

        // Unit under test is exercised.
        HeaderHandler handler = new HeaderHandler();
        boolean handleResult = handler.handleMessage(soapMessageContext);

        // Additional verification code, if any, either here or before the verification block.

        // assert that message would proceed normally
        assertTrue(handleResult);

        //soapMessage.writeTo(System.out);
    }
*/
}
