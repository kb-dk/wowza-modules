package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import org.junit.jupiter.api.Disabled;
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
        TicketToolInterface ticketTool = new TicketTool("http://alhena:7950/ticket-system-service/tickets", logger);
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
        try {
            ClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            Client client = Client.create(clientConfig);
            WebResource query = client.resource("http://alhena:7950/ticket-system-service/tickets")
                    .path("/issueTicket").queryParam("ipAddress", username)
                    .queryParam("id", resource).queryParam("type","Streame");
            for (Property prop : properties) {
                query = query.queryParam(prop.getName(), prop.getValue());
            }
            return query.accept(MediaType.APPLICATION_JSON).post(Map.class);
        } catch (UniformInterfaceException e) {
            throw new RuntimeException("Unexpected event", e);
        }
    }

}
