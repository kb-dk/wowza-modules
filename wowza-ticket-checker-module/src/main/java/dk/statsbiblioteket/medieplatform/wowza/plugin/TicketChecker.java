package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.client.IClient;
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
        String name = stream.getName();
        IClient client = stream.getClient();
        if (client == null) {
            logger.debug("No client, returning ", stream);
            return false;
        }
        String clientQuery = stream.getClient().getQueryStr();
        logger.trace(
                "checkTicket(IMediaStream stream=" + stream + ", String name=" + name
                        + ", String clientQuery=" + clientQuery + ")");
        try {
            Ticket streamingTicket = StringAndTextUtil.getTicket(clientQuery, ticketTool);
            logger.debug("Ticket received: " + (streamingTicket != null ? streamingTicket.getId() : "null"));
            if (
                    streamingTicket != null &&
                            isClientAllowed(stream, streamingTicket) &&
                            ticketForThisPresentationType(streamingTicket) &&
                            doesTicketAllowThisStream(name, streamingTicket)
                    ) {
                logger.info(
                        "checkTicket(IMediaStream stream=" + stream + ", String name=" + name
                                + ", String clientQuery=" + clientQuery + ") successful.");
                return true;
            } else {
                logger.info("Client not allowed to get content streamed for IMediaStream stream=" + stream
                                     + ", String name=" + name + ", String clientQuery=" + clientQuery
                                     + ")");
                return false;
            }
        } catch (IllegallyFormattedQueryStringException e) {
            logger.warn("Illegally formatted query string [" + clientQuery + "].");
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
     * @param stream the stream
     * @param streamingTicket the ticket
     * @return true if the ip is the same for the ticket and the user
     */
    private boolean isClientAllowed(IMediaStream stream, Ticket streamingTicket) {
        String ipOfClient = stream.getClient().getIp();

        boolean isAllowed = (ipOfClient != null) && (ipOfClient.equals(streamingTicket.getIpAddress()));
        logger.debug("isClientAllowed - ipOfClient: " + ipOfClient + ", streamingTicket.getIpAddress(): "
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
