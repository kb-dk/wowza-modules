package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

/**
 * Exception thrown on trouble extracting a key from a query string.
 */
public class IllegallyFormattedQueryStringException extends Exception {

    public IllegallyFormattedQueryStringException(String message) {
        super(message);
    }
}
