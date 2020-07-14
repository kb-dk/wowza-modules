package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

import java.util.Arrays;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.StatusType;

import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

public class TicketTool implements TicketToolInterface {

    private WMSLogger logger;
    private final String serviceUrl;

    public TicketTool(String serviceURL, WMSLogger logger) {
        super();
        this.serviceUrl = serviceURL;
        this.logger = logger;
    }

    /* (non-Javadoc)
      * @see dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface#resolveTicket(java.lang.String)
      */
    @Override
    public Ticket resolveTicket(String ticketID) {
    	WebClient client = getWebclient();
        try {
            Ticket ticketXml = client.path("/resolveTicket/").path(ticketID).get(Ticket.class);
            logger.debug("resolveTicket: Ticket received: '" + ticketID + "'");
            return ticketXml;

        } catch (WebApplicationException e) {
            StatusType responseStatus = e.getResponse().getStatusInfo();
            logger.debug("The session might have timed out for ticket '"
                                 + ticketID + "'. Ticket service response status: " + responseStatus.getStatusCode());
            return null;
        } finally {
        	client.close();
        }
    }
    
    private WebClient getWebclient() {
    	WebClient client = WebClient.create(serviceUrl, Arrays.asList(new JacksonJaxbJsonProvider())); 
    	return client;
    }
}
