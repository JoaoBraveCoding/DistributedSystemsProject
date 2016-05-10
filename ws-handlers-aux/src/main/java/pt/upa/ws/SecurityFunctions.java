package pt.upa.ws;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;


public class SecurityFunctions {
  
  public static byte[] digestBroker(String msg, byte[] nonce) throws NoSuchAlgorithmException{
    // get a message digest object using the specified algorithm
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

    System.out.println("Computing digestBroker ...");
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

    System.out.println("Computing digestTransporter ...");
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

  /**
   * Returns the public key from a certificate
   * 
   * @param certificate
   * @return
   */
  public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
    return certificate.getPublicKey();
  }

  /**
   * Reads a certificate from a file
   * 
   * @return
   * @throws Exception
   */
  public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
    FileInputStream fis;

    try {
      fis = new FileInputStream(certificateFilePath);
    } catch (FileNotFoundException e) {
      System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
      return null;
    }
    BufferedInputStream bis = new BufferedInputStream(fis);

    CertificateFactory cf = CertificateFactory.getInstance("X.509");

    if (bis.available() > 0) {
      Certificate cert = cf.generateCertificate(bis);
      return cert;
      // It is possible to print the content of the certificate file:
      // System.out.println(cert.toString());
    }
    bis.close();
    fis.close();
    return null;
  }

  /**
   * Reads a PrivateKey from a key-store
   * 
   * @return The PrivateKey
   * @throws Exception
   */
  public static PrivateKey getPrivateKeyFromKeystore(String keyStoreFilePath, char[] keyStorePassword,
      String keyAlias, char[] keyPassword) throws Exception {

    KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
    PrivateKey key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);

    return key;
  }
  
  public static Certificate getCaCertificateFromKeystore(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
    KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
    Certificate cer = keystore.getCertificate("UpaCa");
    return cer;
  }

  /**
   * Reads a KeyStore from a file
   * 
   * @return The read KeyStore
   * @throws Exception
   */
  public static KeyStore readKeystoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
    FileInputStream fis;
    try {
      fis = new FileInputStream(keyStoreFilePath);
    } catch (FileNotFoundException e) {
      System.err.println("Keystore file <" + keyStoreFilePath + "> not fount.");
      return null;
    }
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    keystore.load(fis, keyStorePassword);
    return keystore;
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
  
  /**
   * Verifica se um certificado foi devidamente assinado pela CA
   * 
   * @param certificate
   *            certificado a ser verificado
   * @param caPublicKey
   *            certificado da CA
   * @return true se foi devidamente assinado
   */
  public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
    try {
      certificate.verify(caPublicKey);
    } catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
        | SignatureException e) {
      return false;
    }
    return true;
  }
}
