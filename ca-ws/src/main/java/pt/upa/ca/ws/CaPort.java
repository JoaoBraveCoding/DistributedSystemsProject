package pt.upa.ca.ws;

import java.security.cert.Certificate;

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
  public String requestCertificate(String name) throws Exception{
    if(name == null){
      throw new NullValueReceivedException();
    }
    Certificate certificate = SecurityFunctions.readCertificateFile("keys/" + name + ".cer");
    String stringCertificate = printBase64Binary(certificate.getEncoded());
    System.out.println("Certificate for " + name + " sent.");
    return stringCertificate;
  }
 
}
