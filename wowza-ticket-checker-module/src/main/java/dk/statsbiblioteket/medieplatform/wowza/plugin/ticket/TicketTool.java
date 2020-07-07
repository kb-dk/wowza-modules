package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.StatusType;

import org.apache.cxf.jaxrs.client.WebClient;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

public class TicketTool implements TicketToolInterface {

    private WMSLogger logger;
    private WebClient restApi;

    public TicketTool(String serviceURL, WMSLogger logger) {
        super();
        restApi = WebClient.create(serviceURL);
        this.logger = logger;
    }

    /* (non-Javadoc)
      * @see dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface#resolveTicket(java.lang.String)
      */
    @Override
    public Ticket resolveTicket(String ticketID) {
        try {
            Ticket ticketXml = restApi.path("/resolveTicket/").path(ticketID).get(Ticket.class);
            logger.debug("resolveTicket: Ticket received: '" + ticketID + "'");
            return ticketXml;

        } catch (WebApplicationException e) {
            StatusType responseStatus = e.getResponse().getStatusInfo();
            logger.debug("The session might have timed out for ticket '"
                                 + ticketID + "'. Ticket service response status: " + responseStatus.getStatusCode());
            return null;
        }
    }
}
