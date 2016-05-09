package pt.upa.ca.ws;

import java.security.PrivateKey;
import javax.jws.WebService;

import pt.upa.ca.exception.NullValueReceivedException;
import pt.upa.ws.SecurityFunctions;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

@WebService(
    endpointInterface="pt.upa.ca.ws.CaPortType",
    name="CaWebService",
    portName="CaPort",
    targetNamespace="http://ws.ca.upa.pt/",
    serviceName="CaService"
    )
public class CaPort implements CaPortType {

  @Override
  public String ping(String name) {
    return "Pong CA server";
  }

  @Override
  public String requestCertificate(String name){
    if(name == null){
      throw new NullValueReceivedException();
    }
    try {
      //Read keys from files
      byte[] serverPubKey = SecurityFunctions.readFile("keys/" + name + "Pub.key");
      PrivateKey caPrivKey    = SecurityFunctions.getPrivKey("keys/CaPriv.key");
  
      //Generate the certificate
      byte[] certificate = SecurityFunctions.makeDigitalSignature(serverPubKey, caPrivKey);
      return printBase64Binary(certificate);

    } catch (Exception e) { e.printStackTrace(); }
    return null;
  }

 
}