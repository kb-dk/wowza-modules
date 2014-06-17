package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringTokenizer;

/**
 * Utility methods for working with Wowza strings.
 */
public class StringAndTextUtil {
    /**
     * Given a query string, return the value for a given key. Throws exception if key is not set.
     * @param key The key to look up in query string.
     * @param queryString The query string to read key from.
     * @return The value of the given key.
     *
     * @throws IllegallyFormattedQueryStringException If key is not set in query string, or if query string has bad
     * format.
     */
    public static String extractValueFromQueryStringAndKey(String key,
            String queryString) throws IllegallyFormattedQueryStringException {
        if (queryString == null) {
            queryString = "";
        }
        // Some players add a / to the query string. Strip it
        if (queryString.endsWith("/")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        StringTokenizer queryItems = new StringTokenizer(queryString, "&");
        while (queryItems.hasMoreTokens()) {
            String queryParameter = queryItems.nextToken();
            StringTokenizer paramTokenizer = new StringTokenizer(queryParameter, "=");
            if (paramTokenizer.countTokens() != 2) {
                //parameters not of the form a=b are ignored
                continue;
            }
            String currentKey = paramTokenizer.nextToken();
            String currentValue = paramTokenizer.nextToken();
            if (currentKey.equalsIgnoreCase(key)) {
                return currentValue;
            }
        }
        throw new IllegallyFormattedQueryStringException(
                "Expected key '" + key + "' not found in query string" + queryString + "'");
    }

    /**
     * Given an input stream, read contents into a string using UTF-8 as charset.
     * @param is Input stream
     * @return Contents of input stream.
     * @throws IOException On trouble reading from input stream.
     */
    public static String convertStreamToString(InputStream is)
                throws IOException {
            /*
             * To convert the InputStream to String we use the
             * Reader.read(char[] buffer) method. We iterate until the
             * Reader return -1 which means there's no more data to
             * read. We use the StringWriter class to produce the string.
             */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
        return writer.toString();
        } else {
            return "";
        }
    }

    /**
     * Get ticket extracted from the query string.
     * @param queryString Query string from which to extract the ticket.
     * @param ticketTool Ticket tool used for looking up tickets.
     * @return Ticket extracted
     * @throws IllegallyFormattedQueryStringException If query string is illegally formatted
     */
    public static Ticket getTicket(String queryString, TicketToolInterface ticketTool)
            throws IllegallyFormattedQueryStringException {
        String ticketID = extractValueFromQueryStringAndKey("ticket", queryString);
        return ticketTool.resolveTicket(ticketID);
    }
}
