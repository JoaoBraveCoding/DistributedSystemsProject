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
  public String requestCertificate(String name) throws Exception {
    if(name == null){
      throw new NullValueReceivedException();
    }
    
    //Read keys from files
    byte[] serverPubKey = SecurityFunctions.readFile("../../../../../../../key/" + name + "Pub.key");
    PrivateKey caPrivKey    = (PrivateKey) SecurityFunctions.getKey("../../../../../../../key/CaPriv.key");

    //Generate the certificate
    byte[] certificate = SecurityFunctions.makeDigitalSignature(serverPubKey, caPrivKey);
    
    return printBase64Binary(certificate);
  }

 
}