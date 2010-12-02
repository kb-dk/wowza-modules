package dk.statsbiblioteket.doms.wowza.plugin.domslive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Dec 2, 2010
 * Time: 1:50:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    // Create a pattern to match a correct query string
    private static Pattern queryPattern = Pattern.compile(
            "shard=(http://www.statsbiblioteket.dk/doms/shard/uuid:([^&]*))"
            + "&ticket=([^&]*)&port=([^&]*)");

    public static String extractTicket(String queryString){
        return extract(queryString,3);
    }

    public static String extractShardID(String queryString){
        return extract(queryString,2);
    }

    public static String extractShardURL(String queryString){
        return extract(queryString,1);
    }


    public static String extractPortID(String queryString){
        return extract(queryString,4);
    }

    private static String extract(String queryString, int group){
        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();
        // Extract
        if (matchFound) {
            return matcher.group(group);
        } else {
            throw new IllegalArgumentException("Query string '"+queryString+"'is not of the expected"
                                               + " format.");
        }

    }
}
