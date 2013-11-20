package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;

import java.util.ArrayList;
import java.util.List;

public class StreamingStatLogEntryTest extends TestCase {

    private WMSLogger logger;
    private TicketToolMock ticketTool;

    // Default test values
    private static final String defaultQueryString = "ticket=abcd";
    private static final String defaulStreamName = "default-stream-name";
    private static final String defaultUsername = "127.0.0.1";
    private static final String defaultResource = "a0639529-124a-453f-b4ea-59f833b47333";
    private static final String defaulStorageDir = "/vhost/storage/dir";
    private IMediaStreamMock defaultStream;
    private IClientMock defaultIClient;
    private IApplicationInstanceMock defaultIAppInstance;

    public StreamingStatLogEntryTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        this.ticketTool = new TicketToolMock();
        this.defaultIAppInstance = new IApplicationInstanceMock(defaulStorageDir);
        this.defaultIClient = new IClientMock(defaultIAppInstance, logger, defaultQueryString);
        this.defaultStream = new IMediaStreamMock(logger, defaulStreamName, defaultIClient);
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
    public void testConstructorValueMapping() {
        // Setup environment
        List<Property> properties = new ArrayList<Property>();
        // Setup user info
        properties.add(new Property("schacHomeOrganization", "au.dk"));
        properties.add(new Property("eduPersonTargetedID", "1x1"));
        // Setup ticket
        Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties);
        Event logEvent = Event.PLAY;
        // Test
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logEvent, ticket, "");
        // Validate
        assertEquals("StreamingStatLogEntry value", Event.PLAY, logEntry.getEvent());
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
        Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties);
        Event logEvent = Event.PLAY;
        // Test
        String logEntry = new StreamingStatLogEntry(logEvent, ticket, "").getLogString();
        String logHeader = StreamingStatLogEntry.getLogStringHeadline();
        assertEquals("Expected same amount of entries in header and logline", logEntry.split(";").length,
                logHeader.split(";").length);
    }

    @Override
    public String toString() {
        return "StreamingStatLogEntryTest [logger=" + logger + ", ticketTool=" + ticketTool + ", defaultStream=" + defaultStream
                + ", defaultIClient=" + defaultIClient + ", defaultIAppInstance=" + defaultIAppInstance + "]";
    }
}
