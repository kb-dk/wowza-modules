package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.TestTicketStore;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;


public class StreamingStatLogEntryTest {

    private WMSLogger logger;
    
    // Default test values
    private static final String defaultUsername = "127.0.0.1";
    private static final String defaultResource = "a0639529-124a-453f-b4ea-59f833b47333";
    

    public StreamingStatLogEntryTest() {
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
    public void testConstructorValueMapping() {
        // Setup environment
        List<Property> properties = new ArrayList<Property>();
        // Setup user info
        properties.add(new Property("schacHomeOrganization", "au.dk"));
        properties.add(new Property("eduPersonTargetedID", "1x1"));
        // Setup ticket
        TestTicketStore tss = new TestTicketStore();
        Ticket ticket = tss.issueTicket(defaultUsername, defaultResource, properties);
        Event logEvent = Event.PLAY;
        // Test
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logEvent, ticket, "");
        // Validate
        assertEquals(Event.PLAY, logEntry.getEvent(), "StreamingStatLogEntry value");
        assertTrue(logEntry.getLogString().contains("\"schacHomeOrganization\":[\"au.dk\"]"));
        assertTrue(logEntry.getLogString().contains("\"eduPersonTargetedID\":[\"1x1\"]"));
    }

    @Test
    public void testGetLogStringHeadline() {
        // Setup environment
        List<Property> properties = new ArrayList<Property>();
        // Setup user info
        properties.add(new Property("schacHomeOrganization", "au.dk"));
        properties.add(new Property("eduPersonTargetedID", "1x1"));
        // Setup ticket
        TestTicketStore tss = new TestTicketStore();
        Ticket ticket = tss.issueTicket(defaultUsername, defaultResource, properties);
        Event logEvent = Event.PLAY;
        // Test
        String logEntry = new StreamingStatLogEntry(logEvent, ticket, "").getLogString();
        String logHeader = StreamingStatLogEntry.getLogStringHeadline();
        assertEquals(logEntry.split(";").length, logHeader.split(";").length, 
                "Expected same amount of entries in header and logline");
    }
}
