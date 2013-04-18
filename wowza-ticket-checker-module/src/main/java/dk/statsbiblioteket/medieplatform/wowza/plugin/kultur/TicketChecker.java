package dk.statsbiblioteket.medieplatform.wowza.plugin.kultur;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.model.Resource;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.QueryUtil;

import java.io.File;
import java.util.List;

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
        String ext = stream.getExt();
        String query = stream.getQueryStr();
        logger.trace(
                "checkTicket(IMediaStream stream=" + stream + ", String name=" + name + ", String ext=" + ext
                        + ", String streamQuery=" + query + ")");
        IClient client = stream.getClient();
        if (client == null) {
            logger.debug("No client, returning ", stream);
            return false;
        }
        String clientQuery = stream.getClient().getQueryStr();
        try {
            Ticket streamingTicket = QueryUtil.getTicket(clientQuery, ticketTool);
            logger.debug("Ticket received: " + (streamingTicket != null ? streamingTicket.getId() : "null"));
            if (
                    streamingTicket != null &&
                    isClientAllowed(stream, streamingTicket) &&
                            ticketForThisPresentationType(streamingTicket) &&
                    doesTicketAllowThisStream(name,streamingTicket)
                    ) {
                logger.info(
                        "checkTicket(IMediaStream stream=" + stream + ", String name=" + name + ", String ext="
                                + ext + ", String streamQuery=" + query + ") successful.");
                return true;
            } else {
                logger.info("Client not allowed to get content streamed for IMediaStream stream=" + stream
                                     + ", String name=" + name + ", String ext=" + ext + ", String streamQuery=" + query
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
        boolean ticketForThis = false;
        for (String resource : streamingTicket.getResources()) {
            if (resource.contains(name)){
                ticketForThis = true;
                break;
            }
        }
        return ticketForThis;
    }

    /**
     * This method checks if the ticket is given to the same IP address as the client
     * @param stream the stream
     * @param streamingTicket the ticket
     * @return true if the ip is the same for the ticket and the user
     */
    private boolean isClientAllowed(IMediaStream stream, Ticket streamingTicket) {
        String ipOfClient = stream.getClient().getIp();

        boolean isAllowed = (ipOfClient != null) && (ipOfClient.equals(streamingTicket.getUserIdentifier()));
        logger.debug("isClientAllowed - ipOfClient: " + ipOfClient + ", streamingTicket.getUserIdentifier(): " + streamingTicket.getUserIdentifier() + ", isAllowed: " + isAllowed);
        return isAllowed;
    }

    private String clean(String name) {
        if (name.contains(".")){
            name = name.substring(0,name.indexOf("."));
        }
        if (name.contains(":")) {
            name = name.substring(name.lastIndexOf(':') + 1);
        }

        return name;
    }
}
