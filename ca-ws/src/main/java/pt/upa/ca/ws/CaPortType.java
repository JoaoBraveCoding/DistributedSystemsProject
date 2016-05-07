package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService
public interface CaPortType {

  String ping(String name);
  String requestCertificate(String name);
}
