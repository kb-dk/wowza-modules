package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract ticket ID from query string
 */
public class QueryUtil {

    // Create a pattern to match a correct query string. Note extra / is sometimes added and stripped here.
    private static Pattern queryPattern = Pattern.compile("ticket=([^/&]*)");

    public static String extractTicketID(String queryString) throws IllegallyFormattedQueryStringException {
        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        // Extract
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegallyFormattedQueryStringException(
                    "Query string '" + queryString + "' is not of the expected format.");
        }

    }

}
