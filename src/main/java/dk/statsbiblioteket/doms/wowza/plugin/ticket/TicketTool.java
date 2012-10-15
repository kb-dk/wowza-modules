package dk.statsbiblioteket.doms.wowza.plugin.ticket;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TicketTool implements TicketToolInterface {

    private WMSLogger logger;
    private WebResource restApi;

    public TicketTool(String serviceURL, WMSLogger logger) {
        super();
        Client client = Client.create();
        restApi = client.resource(serviceURL);
        this.logger = logger;
    }

    /* (non-Javadoc)
      * @see dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketToolInterface#resolveTicket(java.lang.String)
      */
    @Override
    public Ticket resolveTicket(String ticketID) {
        try {
            Ticket ticketXml = restApi.path("/resolveTicket").queryParam("ID", ticketID).get(Ticket.class);
            logger.info("resolveTicket: Ticket received.");
            return ticketXml;

        } catch (UniformInterfaceException e) {
            // If the ticket does not exist, i.e. the session has timed out.
            Status responseStatus = e.getResponse().getClientResponseStatus();
            logger.info("The session might have timed out. Ticket service response status: " + responseStatus
                    .getStatusCode());
            return null;
        }
    }
}
