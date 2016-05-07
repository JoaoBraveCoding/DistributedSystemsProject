package pt.upa.ws;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

public class SecurityFunctions {
  
  public static byte[] digestBroker(String msg, byte[] nonce) throws NoSuchAlgorithmException{
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

  public static byte[] digestTransporter(String msg, byte[] nonce, String TransporterName) throws NoSuchAlgorithmException{
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

  public static byte[] decryptMessage(PublicKey publicKey, byte[] message) throws Exception{
    // get an RSA cipher object
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

    // decrypt the ciphered digest using the public key
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    return cipher.doFinal(message);
  }

  public static Key getKey(String path) throws Exception{
    byte[] privEncoded = readFile(path);
    PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
    KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
    return keyFacPriv.generatePrivate(privSpec);
  }

  /** auxiliary method to calculate digest from text and cipher it */
  public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {

    // get a signature object using the SHA-1 and RSA combo
    // and sign the plain-text with the private key
    Signature sig = Signature.getInstance("SHA1WithRSA");
    sig.initSign(privateKey);
    sig.update(bytes);
    byte[] signature = sig.sign();

    return signature;
  }
  
  /**
   * auxiliary method to calculate new digest from text and compare it to the
   * to deciphered digest
   */
  public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey)
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
