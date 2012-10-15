package dk.statsbiblioteket.doms.wowza.plugin.ticket;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TicketToolTest extends TestCase {

    private WMSLogger logger;

    public TicketToolTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
    }

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
    }

    @After
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testValidateTicket() {
        // Setup environment
        TicketToolInterface ticketTool = new TicketTool("http://alhena:7480/authchecker-service/tickets", logger);
        String username = "aUsername";
        String url = "anURL";
        Ticket issuedTicket = issueTicket(username, url, new ArrayList<TicketProperty>());
        logger.debug("Issued ticket: " + issuedTicket);
        String ticketID = issuedTicket.getID();
        Ticket resolvedTicket = ticketTool.resolveTicket(ticketID);
        logger.debug("Resolved ticket: " + resolvedTicket);
        assertEquals(issuedTicket, resolvedTicket);
    }

    private Ticket issueTicket(String username, String resource, List<TicketProperty> properties) {
        try {
            WebResource query = Client.create().resource("http://alhena:7480/authchecker-service/tickets")
                    .path("/issueTicket").queryParam("username", username)
                    .queryParam("resource", resource);
            for (TicketProperty prop : properties) {
                query = query.queryParam(prop.getName(), prop.getValue());
            }
            return query.post(Ticket.class);
        } catch (UniformInterfaceException e) {
            throw new RuntimeException("Unexpected event", e);
        }
    }

}
