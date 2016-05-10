package pt.upa.broker.ws.handler;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
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

import pt.upa.ca.ws.cli.CaClient;

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
  
  private HashMap<String, HashMap<String, Boolean>> usedNonces = new HashMap<String, HashMap<String, Boolean>>();

  
  //
  // Handler interface methods
  //
  public Set<QName> getHeaders() {
    return null;
  }

  public boolean handleMessage(SOAPMessageContext smc) {
    System.out.println("HeaderHandler: Handling message.");

    CaClient client = null;
    
    
    
    try {
      client = new CaClient ("http://localhost:9090","UpaCa");
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    
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
        PrivateKey privKey = SecurityFunctions.getPrivateKeyFromKeystore("keys/UpaBroker.jks", "passwd".toCharArray(), "UpaBroker", "passwd".toCharArray()); 
        
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

        
        //get certificate from CA
        String textBrokerCertificate = client.requestCertificate("UpaBroker");

        Name certificateName = se.createName("certificate", "e", "urn:upa");
        SOAPHeaderElement certificateElement = sh.addHeaderElement(certificateName);
        certificateElement.addTextNode(textBrokerCertificate);
        System.out.println("Message sent");

      } else {
        System.out.println("Reading header in inbound SOAP message...");
        if(smc.SERVLET_REQUEST != null){
          return true;
        }

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
        /*
        for(Entry<byte[], HashMap<String, Boolean>> entry : usedNonces.entrySet()){
          byte[] key = entry.getKey();
          System.out.println(Arrays.equals(key, nonce)+ " - -----");
          HashMap<String, Boolean> value = entry.getValue();
          System.out.println(value.containsKey(transporterText));
        }*/
        
        System.out.println(usedNonces.containsKey(nonceText) + "  ------");
        
        if(usedNonces.containsKey(nonceText) && usedNonces.get(nonceText).containsKey(transporterText)){
          System.out.println("Nonce element already used");
          return false;
        }
        
        if(!usedNonces.containsKey(nonceText)) {
          usedNonces.put(nonceText, new HashMap<String, Boolean>());
        }
        
        usedNonces.get(nonceText).put(transporterText, true);
        
        //get certificate from CA
        String transporterCertificateText = client.requestCertificate(transporterText);
        byte[] byteCertificate = parseBase64Binary(transporterCertificateText);
        CertificateFactory cf   = CertificateFactory.getInstance("X.509");
        Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(byteCertificate));

        PublicKey pubKeyTransporter = certificate.getPublicKey();
        
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