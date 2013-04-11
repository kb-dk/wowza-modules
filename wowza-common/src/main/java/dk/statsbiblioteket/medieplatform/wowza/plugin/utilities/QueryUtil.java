package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Dec 2, 2010
 * Time: 1:50:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryUtil {

    // Create a pattern to match a correct query string
    private static Pattern queryPattern = Pattern.compile("ticket=([^&]*)");

    public static String extractTicketID(String queryString) throws IllegallyFormattedQueryStringException {
        return extract(queryString, 1);
    }

    private static String extract(String queryString, int group) throws IllegallyFormattedQueryStringException {
        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();
        // Extract
        if (matchFound) {
            return matcher.group(group);
        } else {
            throw new IllegallyFormattedQueryStringException(
                    "Query string '" + queryString + "' is not of the expected format.");
        }

    }
}
