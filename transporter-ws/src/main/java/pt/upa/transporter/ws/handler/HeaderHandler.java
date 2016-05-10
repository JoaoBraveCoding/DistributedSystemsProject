package pt.upa.transporter.ws.handler;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
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
import javax.xml.ws.handler.MessageContext.Scope;
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

  public static final String CONTEXT_PROPERTY = "transporterName";
  
  private HashMap<String, String> usedNonces = new HashMap<String, String>();
  private HashMap<String, String> noncesSent = new HashMap<String, String>();
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
        //Arrays.fill(nonce, (byte)1);
        
        //dont send the same nonce
        while(noncesSent.containsKey(printBase64Binary(nonce))) {
          random.nextBytes(nonce);
        }
        
        //add nonce to hash of nonces sent
        noncesSent.put(printBase64Binary(nonce), "transp");
        
        // add header
        SOAPHeader sh = se.getHeader();
        if (sh == null)
          sh = se.addHeader();
               
        //UpaTransporterX should be in context
        String transporterNameText = (String) smc.get(CONTEXT_PROPERTY);
        
        // set transporter element
        Name transporterName = se.createName("transporter", "e", "urn:upa");
        SOAPHeaderElement transporterElement = sh.addHeaderElement(transporterName);
        transporterElement.addTextNode(transporterNameText);
        
        //make digest
        byte[] digest = SecurityFunctions.digestTransporter(plainText, nonce, transporterNameText);
        
        //encrypt digest = signature
        PrivateKey privKey = SecurityFunctions.getPrivateKeyFromKeystore("keys/"+ transporterNameText + ".jks", "passwd".toCharArray(), transporterNameText, "passwd".toCharArray());
        byte[] signature = SecurityFunctions.makeDigitalSignature(digest, privKey);


        //turn signature into text
        String textSignature = printBase64Binary(signature);

        // get first header element TODO what's with the e?
        Name signatureName = se.createName("signature", "e", "urn:upa");
        SOAPHeaderElement elementHeader = sh.addHeaderElement(signatureName);
        elementHeader.addTextNode(textSignature);

        //turn nonce into text
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

        //check if is a new nonce
        if(usedNonces.containsKey(nonceText)){
          System.out.println("Nonce already used once");
          return false;
        }
        
        //change nonce to byte
        byte[] nonce = parseBase64Binary(nonceText);
//--
        // get certificate element
        Name certificate = se.createName("certificate", "e", "urn:upa");
        it = sh.getChildElements(certificate);

        // check header element
        if (!it.hasNext()) {
          System.out.println("Certificate element not found.");
          return false;
        }
        SOAPElement certificateElement = (SOAPElement) it.next();

        // get header element value
        String certificateText = certificateElement.getValue();
        
        //get BrokerPubKey from certificate
        byte[] byteCertificate   = parseBase64Binary(certificateText);
        CertificateFactory cf    = CertificateFactory.getInstance("X.509");
        Certificate certificate1 = cf.generateCertificate(new ByteArrayInputStream(byteCertificate));
        PublicKey pubKeyBroker   = SecurityFunctions.getPublicKeyFromCertificate(certificate1);
        
        //computing digest
        byte[] computedDigest = SecurityFunctions.digestBroker(bodyText, nonce);
        
        
        // *** #5 ***
        // put token in request context
        String newValue = "Give me your name";
        System.out.printf("%s put token '%s' on request context%n", "transporter-handler", newValue);
        smc.put(CONTEXT_PROPERTY, newValue);
        // set property scope to application so that server class can
        // access property
        smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
        

        // verify - should the signature be already decrypted or does the function do that?
        if(!SecurityFunctions.verifyDigitalSignature(signature, computedDigest, pubKeyBroker)){
          System.out.println("Wrong digital signature.");
          return false;
        }
        

      }
    } catch (Exception e) {
      System.out.print("Caught exception in handleMessage: ");
      System.out.println(e);
      return false;
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