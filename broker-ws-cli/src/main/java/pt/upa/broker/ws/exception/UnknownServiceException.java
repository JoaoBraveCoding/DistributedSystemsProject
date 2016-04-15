package pt.upa.broker.ws.exception;

public class UnknownServiceException extends Exception {

  private static final long serialVersionUID = 1L;

  private String message;
  public UnknownServiceException(String message){
    this.message = message;
  }
  
  public String getMessage(){
    return message;
  }
}
