package pt.upa.ca.exception;

public class NullValueReceivedException extends CaException{

  private static final long serialVersionUID = 1L;

  @Override
  public String getMessage() {
      return "A null value was received instead of a name of a server";
  }
}
