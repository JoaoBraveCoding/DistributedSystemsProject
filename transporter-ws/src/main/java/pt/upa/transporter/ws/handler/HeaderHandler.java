package pt.upa.transporter.ws.handler;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import pt.upa.ws.SecurityFunctions;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

/**
 *  This SOAPHandler shows how to set/get values from headers in
 *  inbound/outbound SOAP messages.
 *
 *  A header is created in an outbound message and is read on an
 *  inbound message.
 *
 *  The value that is read from the header
 *  is placed in a SOAP message context property
 *  that can be accessed by other handlers or by the application.
 */
public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {

  public static final String CONTEXT_PROPERTY = "my.property";
  
  //
  // Handler interface methods
  //
  public Set<QName> getHeaders() {
    return null;
  }

  public boolean handleMessage(SOAPMessageContext smc) {
    System.out.println("HeaderHandler: Handling message.");

    Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

    try {
      if (outboundElement.booleanValue()) {
        System.out.println("Writing header in outbound SOAP message...");

        // get SOAP envelope
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();

        //get message from body
        SOAPBody sb = se.getBody();                
        Iterator it = sb.getChildElements();
        if(!it.hasNext()) {
          System.out.println("Body entry element not found.");
          return true;
        }
        SOAPElement element = (SOAPElement) it.next();
        String plainText = element.getTextContent();

        //get random for nonce
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte nonce[] = new byte[16];
        random.nextBytes(nonce);
               
        //make digest
        byte[] digest = SecurityFunctions.digestBroker(plainText, nonce);

        //encrypt digest = signature
        PrivateKey privKey = SecurityFunctions.getPrivKey("keys/UpaBrokerPriv.key");
        byte[] signature = SecurityFunctions.makeDigitalSignature(digest, privKey);

        // add header
        SOAPHeader sh = se.getHeader();
        if (sh == null)
          sh = se.addHeader();

        //turn signature into text
        String textSignature = printBase64Binary(signature);

        // get first header element TODO what's with the e?
        Name signatureName = se.createName("signature", "e", "urn:upa");
        SOAPHeaderElement elementHeader = sh.addHeaderElement(signatureName);
        elementHeader.addTextNode(textSignature);

        //turn signature into text
        String textNonce = printBase64Binary(nonce);
        
        Name nonceName = se.createName("nonce", "e", "urn:upa");
        SOAPHeaderElement nonceElement = sh.addHeaderElement(nonceName);
        nonceElement.addTextNode(textNonce);



      } else {
        System.out.println("Reading header in inbound SOAP message...");

        // get SOAP envelope header
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPHeader sh = se.getHeader();

        //get text from body
        SOAPBody sb = se.getBody();                
        Iterator it = sb.getChildElements();
        if(!it.hasNext()) {
          System.out.println("Body entry element not found.");
          return true;
        }
        SOAPElement element = (SOAPElement) it.next();
        String bodyText = element.getTextContent();                

        // check header
        if (sh == null) {
          System.out.println("Header not found.");
          return false;
        }

        // get signature element
        Name signatureName = se.createName("signature", "e", "urn:upa");
        it = sh.getChildElements(signatureName);

        // check header element
        if (!it.hasNext()) {
          System.out.println("Signature element not found.");
          return false;
        }
        SOAPElement signatureElement = (SOAPElement) it.next();

        // get header element value
        String signatureText = signatureElement.getValue();

        //change signature to byte
        byte[] signature = parseBase64Binary(signatureText);

        // get nonce element
        Name nonceName = se.createName("nonce", "e", "urn:upa");
        it = sh.getChildElements(nonceName);

        if (!it.hasNext()) {
          System.out.println("Nonce element not found.");
          return false;
        }
        SOAPElement nonceElement = (SOAPElement) it.next();

        // get header element value
        String nonceText = nonceElement.getValue();

        //change nonce to byte
        byte[] nonce = parseBase64Binary(nonceText);

        // get transporter element
        Name transporterName = se.createName("transporter", "e", "urn:upa");
        it = sh.getChildElements(transporterName);

        if (!it.hasNext()) {
          System.out.println("Transporter name not found.");
          return false;
        }
        SOAPElement transporterElement = (SOAPElement) it.next();

        // get header element value
        String transporterText = transporterElement.getValue();

        //get certificate from CA
        String transporterCertificateText = null;//client.requestCertificate(transporterText);
        byte[] transporterCertificate = parseBase64Binary(transporterCertificateText);

        PublicKey pubKeyCA = SecurityFunctions.getPubKey("keys/CaPub.key");
        byte[] transporterPublicKey = SecurityFunctions.decryptMessage(pubKeyCA, transporterCertificate);
        PublicKey pubKeyTransporter = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(transporterPublicKey));

        byte[] computedDigest = SecurityFunctions.digestTransporter(bodyText, nonce, transporterText);

        //Fazer o verify - should the signature be already decrypted or does the function do that?
        if(SecurityFunctions.verifyDigitalSignature(signature, computedDigest, pubKeyTransporter)){
          System.out.println("Wrong digital signature.");
          return false;
        }

      }
    } catch (Exception e) {
      System.out.print("Caught exception in handleMessage: ");
      System.out.println(e);
      e.printStackTrace();
      System.out.println("Continue normal processing...");
    }

    return true;
  }

  public boolean handleFault(SOAPMessageContext smc) {
    System.out.println("Ignoring fault message...");
    return true;
  }

  public void close(MessageContext messageContext) {
  }
 
}