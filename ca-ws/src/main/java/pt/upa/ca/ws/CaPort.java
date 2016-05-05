package pt.upa.ca.ws;

import javax.jws.WebService;

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

 
}