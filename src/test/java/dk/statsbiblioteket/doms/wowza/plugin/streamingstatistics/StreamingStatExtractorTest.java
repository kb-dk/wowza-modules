package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StreamingStatExtractorTest extends TestCase {

    private WMSLogger logger;
    private TicketToolMock ticketTool;

    // Default test values
    private static final String defaultStatLogDir = "/log/dir";
    private static final String defaultQueryString = "ticket=abcd";
    private static final String defaulStreamName = "default-stream-name";
    private static final String defaultUsername = "127.0.0.1";
    private static final String defaultResource = "a0639529-124a-453f-b4ea-59f833b47333";
    private static final String defaultEduPersonTargetedID = "0123456789abcd";
    private static final String defaulStorageDir = "/vhost/storage/dir";
    private IMediaStreamMock defaultStream;
    private IClientMock defaultIClient;
    private IApplicationInstanceMock defaultIAppInstance;

    public StreamingStatExtractorTest() {
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
    public void testReadLog() throws IOException {
        int logSize = 10;
        BufferedReader logReader = getLogReader(logSize);
        StreamingStatExtractor extractor = new StreamingStatExtractor(logger, logReader);
        extractor.readLog(logReader);
        List<StreamingStatLogEntry> logEntries = extractor.listOfLogEntries;
        // Validate
        assertEquals(logSize, logEntries.size());
        int i = 0;
        for (Iterator<StreamingStatLogEntry> iterator = logEntries.iterator(); iterator.hasNext(); ) {
            StreamingStatLogEntry logEntry = iterator.next();
            assertEquals("StreamingStatLogEntry value", Event.STREAMING_START, logEntry.getEvent());
            assertEquals("StreamingStatLogEntry value", defaultStream.getUniqueStreamIdStr(),
                         logEntry.getConnectionID());
            assertEquals("StreamingStatLogEntry value", "au.dk", logEntry.getOrganisationID());
            assertEquals("StreamingStatLogEntry value", "1x1-" + i, logEntry.getUserID());
            assertEquals("StreamingStatLogEntry value", "tv2news", logEntry.getChannelID());
            assertEquals("StreamingStatLogEntry value", "Nyheder", logEntry.getProgramTitle());
            assertEquals("StreamingStatLogEntry value", "2007-03-04T00:00:00+0100", logEntry.getProgramStart());
            i++;
        }
    }

    @Test
    public void testGetNumberOfStartedProgramViews() throws IOException {
        int logSize = 10;
        BufferedReader logReader = getLogReader(logSize);
        StreamingStatExtractor extractor = new StreamingStatExtractor(logger, logReader);
        extractor.readLog(logReader);
        // Validate
        assertEquals(logSize, extractor.getNumberOfStartedProgramViews());
    }

    private BufferedReader getLogReader(int logSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < logSize; i++) {
            List<Property> properties = new ArrayList<Property>();
            // Setup user info
            properties.add(new Property("schacHomeOrganization", "au.dk"));
            properties.add(new Property("eduPersonTargetedID", "1x1-" + i));
            // Setup program info
            properties.add(new Property("metaChannelName", "tv2news"));
            properties.add(new Property("metaTitle", "Nyheder"));
            properties.add(new Property("metaDateTimeStart", "2007-03-04T00:00:00+0100"));
            Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties);
            Event logEvent = Event.STREAMING_START;
            // Test
            String logEntryString = new StreamingStatLogEntry(logger, defaultStream, logEvent, ticket).getLogString();
            sb.append(logEntryString).append("\n");
        }
        BufferedReader logReader = new BufferedReader(new StringReader(sb.toString()));
        return logReader;
    }
}
