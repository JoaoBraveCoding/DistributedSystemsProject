package pt.upa.ca.exception;

public abstract class CaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CaException() {
    }

    public CaException(String msg) {
        super(msg);
    }
}
