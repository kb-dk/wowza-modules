package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;


import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import org.junit.jupiter.api.Disabled;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import dk.statsbiblioteket.medieplatform.ticketsystem.Property;

import javax.ws.rs.core.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TicketToolTest {

    private WMSLogger logger;

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

    @Test
    @Disabled
    public void testValidateTicket() {
        // Setup environment
        TicketToolInterface ticketTool = new TicketTool("http://iapetus:9651/ticket-system-service/tickets", logger);
        String username = "aUsername";
        String url = "doms_reklamefilm:uuid:35a1aa76-97a1-4f1b-b5aa-ad2a246eeeec";
        Map<String, String> ticketMap = issueTicket(username, url, Arrays.asList(new Property("ip_role_mapper.SBIPRoleMapper", "SB_PUB")));
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
        WebClient client = WebClient.create("http://iapetus:9651/ticket-system-service/tickets");
        WebClient clientRequest = client.path("/issueTicket")
            .query("ipAddress", username)
            .query("id", resource)
            .query("type","Streame");
        for (Property prop : properties) {
            clientRequest = clientRequest.query(prop.getName(), prop.getValue());
        }
        Map<String, String> resp = clientRequest.accept(MediaType.APPLICATION_JSON).post(new Object(), Map.class);
        return resp;
    }

}
