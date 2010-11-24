package dk.statsbiblioteket.doms.wowza.plugin.domslive.model;

public class InvalidURIException extends Exception {

    private static final long serialVersionUID = -1458744286061227284L;

    public InvalidURIException(String message) {
        super(message);
    }

    public InvalidURIException(String message, Throwable cause) {
        super(message, cause);
    }
}