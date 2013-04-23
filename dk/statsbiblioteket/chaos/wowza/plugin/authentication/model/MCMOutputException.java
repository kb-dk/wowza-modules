package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

public class MCMOutputException extends Exception {

	private static final long serialVersionUID = 7225566001480361840L;

	public MCMOutputException(String message) {
		super(message);
	}

	public MCMOutputException(String message, Throwable cause) {
		super(message, cause);
	}
}
