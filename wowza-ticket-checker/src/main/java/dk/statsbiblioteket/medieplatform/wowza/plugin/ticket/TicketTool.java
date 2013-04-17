package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

public class TicketTool implements TicketToolInterface {

    private WMSLogger logger;
    private WebResource restApi;

    public TicketTool(String serviceURL, WMSLogger logger) {
        super();
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        restApi = client.resource(serviceURL);
        this.logger = logger;
    }

    /* (non-Javadoc)
      * @see dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface#resolveTicket(java.lang.String)
      */
    @Override
    public Ticket resolveTicket(String ticketID) {
        try {
            Ticket ticketXml = restApi.path("/resolveTicket").queryParam("ID", ticketID).get(Ticket.class);
            logger.debug("resolveTicket: Ticket received: '" + ticketID + "'");
            return ticketXml;

        } catch (UniformInterfaceException e) {
            // If the ticket does not exist, i.e. the session has timed out.
            Status responseStatus = e.getResponse().getClientResponseStatus();
            logger.debug("The session might have timed out for ticket '"
                                 + ticketID + "'. Ticket service response status: " + responseStatus.getStatusCode());
            return null;
        }
    }
}
