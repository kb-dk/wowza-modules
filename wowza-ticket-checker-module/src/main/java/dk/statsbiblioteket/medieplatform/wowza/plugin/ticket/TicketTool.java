package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.StatusType;

import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

public class TicketTool implements TicketToolInterface {

    private WMSLogger logger;
    private WebClient restApi;

    public TicketTool(String serviceURL, WMSLogger logger) {
        super();
        List<Object> providers = new ArrayList<>();
        providers.add(new JacksonJaxbJsonProvider());
        restApi = WebClient.create(serviceURL, providers);
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
