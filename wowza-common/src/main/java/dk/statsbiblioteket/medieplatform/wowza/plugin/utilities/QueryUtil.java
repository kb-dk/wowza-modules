package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract ticket ID from query string
 */
public class QueryUtil {

    // Create a pattern to match a correct query string. Note extra / is sometimes added and stripped here.
    private static Pattern queryPattern = Pattern.compile("ticket=([^/&]*)");

    static String extractTicketID(String queryString) throws IllegallyFormattedQueryStringException {
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

    /**
     * Get ticket extracted from the query string.
     * @param queryString Query string from which to extract the ticket.
     * @param ticketTool Ticket tool used for looking up tickets.
     * @return Ticket extracted
     * @throws IllegallyFormattedQueryStringException If query string is illegally formatted
     */
    public static Ticket getTicket(String queryString, TicketToolInterface ticketTool)
            throws IllegallyFormattedQueryStringException {
        String ticketID = extractTicketID(queryString);
        return ticketTool.resolveTicket(ticketID);
    }
}
