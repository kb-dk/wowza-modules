package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

/**
 * This class is used to validate the ticket
 */
public class TicketChecker {
    private final WMSLogger logger;
    private final String presentationType;
    private final TicketToolInterface ticketTool;

    public TicketChecker(String presentationType, TicketToolInterface ticketTool) {
        super();
        this.presentationType = presentationType;
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        this.ticketTool = ticketTool;
    }

    /**
     * Check if a stream is allowed to play
     *
     * @return true if allowed, false otherwise.
     */
    public boolean checkTicket(IMediaStream stream) {
        IClient client = stream.getClient();
        if (client == null) {
            logger.debug("No client, returning ", stream);
            return false;
        }
        return checkTicket(stream.getName(), client.getQueryStr(), client.getIp());
    }

    /**
     * Check if a stream is allowed to play
     *
     * @return true if allowed, false otherwise.
     */
    public boolean checkTicket(IHTTPStreamerSession httpSession) {
        return checkTicket(httpSession.getStream().getName(), httpSession.getQueryStr(), httpSession.getIpAddress());
    }

    private boolean checkTicket(String name, String query, String ip) {
        logger.trace(
                "checkTicket(String name=" + name
                        + ", String query=" + query + ")");
        try {
            Ticket streamingTicket = StringAndTextUtil.getTicket(query, ticketTool);
            logger.debug("Ticket received: " + (streamingTicket != null ? streamingTicket.getId() : "null"));
            if (
                    streamingTicket != null &&
                            isClientAllowed(streamingTicket, ip) &&
                            ticketForThisPresentationType(streamingTicket) &&
                            doesTicketAllowThisStream(name, streamingTicket)
                    ) {
                logger.info(
                        "checkTicket(String name=" + name
                                + ", String query=" + query + ") successful.");
                return true;
            } else {
                logger.info("Client not allowed to get content streamed for String name=" + name
                                    + ", String query=" + query
                                     + ")");
                return false;
            }
        } catch (IllegallyFormattedQueryStringException e) {
            logger.warn("Illegally formatted query string [" + query + "].");
            return false;
        }
    }

    private boolean ticketForThisPresentationType(Ticket streamingTicket) {
        return streamingTicket.getType().equals(presentationType);
    }

    private boolean doesTicketAllowThisStream(String name, Ticket streamingTicket) {
        name = clean(name);
        for (String resource : streamingTicket.getResources()) {
            if (resource.contains(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if the ticket is given to the same IP address as the client
     * @param streamingTicket the ticket
     * @param ip ip of client
     * @return true if the ip is the same for the ticket and the user
     */
    private boolean isClientAllowed(Ticket streamingTicket, String ip) {

        boolean isAllowed = (ip != null) && (ip.equals(streamingTicket.getIpAddress()));
        logger.debug("isClientAllowed - ipOfClient: " + ip + ", streamingTicket.getIpAddress(): "
                + streamingTicket.getIpAddress() + ", isAllowed: " + isAllowed);
        return isAllowed;
    }

    private String clean(String name) {
        if (name.contains(".")){
            name = name.substring(0, name.indexOf("."));
        }
        if (name.contains(":")) {
            name = name.substring(name.lastIndexOf(':') + 1);
        }

        return name;
    }
}
