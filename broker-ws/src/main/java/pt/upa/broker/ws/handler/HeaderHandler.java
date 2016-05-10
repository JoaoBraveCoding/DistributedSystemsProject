package pt.upa.broker.ws.handler;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
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
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import javax.xml.ws.ProtocolException;

import pt.upa.ws.SecurityFunctions;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

import pt.upa.ca.ws.cli.CaClient;

public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {
  
  private HashMap<String, HashMap<String, Boolean>> usedNonces = new HashMap<String, HashMap<String, Boolean>>();
  private HashMap<String, String> sentNonces = new HashMap<String, String>();
  
  //
  // Handler interface methods
  //
  public Set<QName> getHeaders() {
    return null;
  }

  public boolean handleMessage(SOAPMessageContext smc) throws ProtocolException {
    System.out.println("HeaderHandler: Handling message.");

    //Connecting to CA
    CaClient client = null;
    try {
      client = new CaClient ("http://localhost:9090","UpaCa");
    } catch (Exception e1) {
      throw new ProtocolException("Ca offline or some problem in comunication can't confirm or send messages");
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
        
        //dont send the same nonce
        while(sentNonces.containsKey(printBase64Binary(nonce))) {
          random.nextBytes(nonce);
        }
        
        //add nonce to hash of nonces sent
        sentNonces.put(printBase64Binary(nonce), "Broker");
               
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
        
        //get BrokerPubKey from certificate
        byte[] byteCertificate   = parseBase64Binary(textBrokerCertificate);
        CertificateFactory cf    = CertificateFactory.getInstance("X.509");
        Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(byteCertificate));
        Certificate caCertificate = SecurityFunctions.getCaCertificateFromKeystore("keys/UpaBroker.jks", "passwd".toCharArray());
        certificate.verify(caCertificate.getPublicKey());

        Name certificateName = se.createName("certificate", "e", "urn:upa");
        SOAPHeaderElement certificateElement = sh.addHeaderElement(certificateName);
        certificateElement.addTextNode(textBrokerCertificate);
        System.out.println("Message sent");

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
          throw new ProtocolException("Header not found");
        }

        // get signature element
        Name signatureName = se.createName("signature", "e", "urn:upa");
        it = sh.getChildElements(signatureName);

        // check header element
        if (!it.hasNext()) {
          throw new ProtocolException("Signature element not found");
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
          throw new ProtocolException("Nonce element not found");
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
          throw new ProtocolException("No transporter name in the header");
        }
        SOAPElement transporterElement = (SOAPElement) it.next();

        // get header element value
        String transporterText = transporterElement.getValue();
        
        if(usedNonces.containsKey(nonceText) && usedNonces.get(nonceText).containsKey(transporterText)){
          throw new ProtocolException("Nonce element already used");
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
        
        Certificate caCertificate = SecurityFunctions.getCaCertificateFromKeystore("keys/UpaBroker.jks", "passwd".toCharArray());
        
        try {
          certificate.verify(caCertificate.getPublicKey());
        }catch (Exception e) {throw new ProtocolException("Invalid Certificate");}
        
        PublicKey pubKeyTransporter = certificate.getPublicKey();
        
        byte[] computedDigest = SecurityFunctions.digestTransporter(bodyText, nonce, transporterText);

        //Fazer o verify - should the signature be already decrypted or does the function do that?
        if(!SecurityFunctions.verifyDigitalSignature(signature, computedDigest, pubKeyTransporter)){
          throw new ProtocolException("Wrong signature");
        }
      }
    } catch (Exception e) {
      System.out.print("Caught exception in handleMessage: ");
      System.out.println(e);
      e.printStackTrace();
      if(e instanceof ProtocolException){
        ProtocolException el = new ProtocolException();
        el.initCause(e.getCause());
        throw el;
      }
      return false;
    }

    return true;
  }

  public boolean handleFault(SOAPMessageContext smc) {
    return false;
  }

  public void close(MessageContext messageContext) {
  }
 
}