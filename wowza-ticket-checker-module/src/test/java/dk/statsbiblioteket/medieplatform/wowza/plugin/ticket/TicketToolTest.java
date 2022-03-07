package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.medieplatform.ticketsystem.Property;

public class TicketToolTest {

    private WMSLogger logger;
    private static final String TICKET_SERVICE_URL = "http://iapetus.statsbiblioteket.dk:9651/ticket-system-service/tickets";
    
    public TicketToolTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
    }

    @BeforeEach
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
    }

    @AfterEach
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    /* 
     * Test that is for internal use only, requires access to infrasture not reachable from the internet. 
     * Can be used to verify that tickets can be requested and validated.
     */
    @Test
    //@Disabled
    public void testValidateTicket() {
        // Setup environment
        TicketToolInterface ticketTool = new TicketTool(TICKET_SERVICE_URL, logger);
        String username = "172.18.98.246"; //aUsername";
        String url = "doms_radioTVCollection:uuid:371157ee-b120-4504-bfaf-364c15a4137c";
        Map<String, String> ticketMap = issueTicket(username, url, Arrays.asList(new Property("SBIPRoleMapper", "inhouse")));
        String issuedTicketId = null;
        for (String key : ticketMap.keySet()) {
            issuedTicketId = ticketMap.get(key);
        }
        logger.debug("Issued ticket: " + issuedTicketId);
        dk.statsbiblioteket.medieplatform.ticketsystem.Ticket resolvedTicket = ticketTool.resolveTicket(issuedTicketId);
        logger.debug("Resolved ticket: " + resolvedTicket);
        assertEquals(url, resolvedTicket.getResources().get(0));
        assertEquals(username, resolvedTicket.getIpAddress());

    }

    private Map<String,String> issueTicket(String username, String resource, List<Property> properties) {
        List<Object> providers = new ArrayList<>();
        providers.add(new JacksonJaxbJsonProvider());
        WebClient client = WebClient.create(TICKET_SERVICE_URL, providers);
        WebClient clientRequest = client.path("/issueTicket")
            .query("ipAddress", username)
            .query("id", resource)
            .query("resource", resource)
            .query("type","Stream");
        for (Property prop : properties) {
            clientRequest = clientRequest.query(prop.getName(), prop.getValue());
        }
        Map<String, String> resp = clientRequest.accept(MediaType.APPLICATION_JSON).post(null, Map.class);
        return resp;
    }

}
