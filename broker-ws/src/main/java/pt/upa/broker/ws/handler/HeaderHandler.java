package pt.upa.broker.ws.handler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ReadOnlyFileSystemException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
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

import com.sun.xml.messaging.saaj.util.Base64;


import example.crypto.X509DigitalSignature;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
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
    System.out.println("AddHeaderHandler: Handling message.");

    Boolean outboundElement = (Boolean) smc
        .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

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
        String plainText = element.getValue();

        //get random for nonce
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte nonce[] = new byte[16];
        random.nextBytes(nonce);

        //make digest
        byte[] digest = digestBroker(plainText, nonce);

        //encrypt digest = signature

        PrivateKey privKey = (PrivateKey) getKey("../../../../../../../../keys/UpaBrokerPriv.key");
        byte[] signature = X509DigitalSignature.makeDigitalSignature(digest, privKey);

        // add header
        SOAPHeader sh = se.getHeader();
        if (sh == null)
          sh = se.addHeader();

        //turn signature into text
        String textSignature = printHexBinary(signature);

        // get first header element TODO what's with the e?
        Name signatureName = se.createName("signature", "e", "urn:upa");
        SOAPHeaderElement elementHeader = sh.addHeaderElement(signatureName);
        elementHeader.addTextNode(textSignature);

        //turn signature into text
        String textNonce = printHexBinary(nonce);

        Name nonceName = se.createName("nonce", "e", "urn:upa");
        SOAPHeaderElement nonceElement = sh.addHeaderElement(nonceName);
        nonceElement.addTextNode(textNonce);

        //get certificate from CA boiii TODO
        Certificate brokerCertificate = null;//CaPort.requestCertificate("UpaBroker");

        //turn certificate into text
        String textBrokerCertificate = printHexBinary(brokerCertificate.getEncoded());

        Name certificateName = se.createName("certificate", "e", "urn:upa");
        SOAPHeaderElement certificateElement = sh.addHeaderElement(certificateName);
        certificateElement.addTextNode(textBrokerCertificate);


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
        String bodyText = element.getValue();                

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

        // check header element
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

        // check header element
        if (!it.hasNext()) {
          System.out.println("Transporter name not found.");
          return false;
        }
        SOAPElement transporterElement = (SOAPElement) it.next();

        // get header element value
        String transporterText = transporterElement.getTextContent();

        //get certificate from CA boiii TODO
        String transporterCertificateText = null;//CaPort.requestCertificate("transporterText");
        byte[] transporterCertificate = parseBase64Binary(transporterCertificateText);

        PublicKey pubKeyCA = (PublicKey) getKey("../../../../../../../../keys/CaPub.key");
        byte[] transporterPublicKey = decryptMessage(pubKeyCA, transporterCertificate);
        PublicKey pubKeyTransporter = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(transporterPublicKey));



        byte[] computedDigest = digestTransporter(bodyText, nonce, transporterText);

        //Fazer o verify - should the signature be already decrypted or does the function do that?
        if(verifyDigitalSignature(signature, computedDigest, pubKeyTransporter)){
          System.out.println("Wrong digital signature.");
          return false;
        }


//        TODO Dunno what this is
//        // put header in a property context
//        smc.put(CONTEXT_PROPERTY, value);
//        // set property scope to application client/server class can access it
//        smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);

      }
    } catch (Exception e) {
      System.out.print("Caught exception in handleMessage: ");
      System.out.println(e);
      System.out.println("Continue normal processing...");
    }

    return true;
  }

  private byte[] digestBroker(String msg, byte[] nonce) throws NoSuchAlgorithmException{
    // get a message digest object using the specified algorithm
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
    System.out.println(messageDigest.getProvider().getInfo());

    System.out.println("Computing digest ...");
    byte[] plainBytes = msg.getBytes();
    byte[] plainPlusNonce = new byte[nonce.length + plainBytes.length];
    //concat: nonce + message
    System.arraycopy(nonce, 0, plainPlusNonce, 0, nonce.length);
    System.arraycopy(plainBytes, 0, plainPlusNonce, nonce.length, plainBytes.length);
    messageDigest.update(plainPlusNonce);
    return messageDigest.digest();
  }

  private byte[] digestTransporter(String msg, byte[] nonce, String TransporterName) throws NoSuchAlgorithmException{
    // get a message digest object using the specified algorithm
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
    System.out.println(messageDigest.getProvider().getInfo());

    System.out.println("Computing digest ...");
    byte[] plainBytes = msg.getBytes();
    byte[] nameBytes = TransporterName.getBytes();
    byte[] plainPlusNonceAndName = new byte[nonce.length + plainBytes.length + nameBytes.length];
    //concat: name + nonce + message

    System.arraycopy(nameBytes, 0, plainPlusNonceAndName, 0, nameBytes.length);
    System.arraycopy(nonce, 0, plainPlusNonceAndName, nameBytes.length, nonce.length);
    System.arraycopy(plainBytes, 0, plainPlusNonceAndName, nonce.length, plainBytes.length);
    messageDigest.update(plainPlusNonceAndName);
    return messageDigest.digest();
  }

  private static byte[] readFile(String path) throws FileNotFoundException, IOException {
    FileInputStream fis = new FileInputStream(path);
    byte[] content = new byte[fis.available()];
    fis.read(content);
    fis.close();
    return content;
  }


  public boolean handleFault(SOAPMessageContext smc) {
    System.out.println("Ignoring fault message...");
    return true;
  }

  public void close(MessageContext messageContext) {
  }

  public byte[] decryptMessage(PublicKey publicKey, byte[] message) throws Exception{
    // get an RSA cipher object
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

    // decrypt the ciphered digest using the public key
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    return cipher.doFinal(message);
  }

  public Key getKey(String path) throws Exception{
    byte[] privEncoded = readFile(path);
    PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
    KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
    return keyFacPriv.generatePrivate(privSpec);
  }

  /**
   * auxiliary method to calculate new digest from text and compare it to the
   * to deciphered digest
   */
  public boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey)
      throws Exception {

    // verify the signature with the public key
    Signature sig = Signature.getInstance("SHA1WithRSA");
    sig.initVerify(publicKey);
    sig.update(bytes);
    try {
      return sig.verify(cipherDigest);
    } catch (SignatureException se) {
      System.err.println("Caught exception while verifying signature " + se);
      return false;
    }
  }
}