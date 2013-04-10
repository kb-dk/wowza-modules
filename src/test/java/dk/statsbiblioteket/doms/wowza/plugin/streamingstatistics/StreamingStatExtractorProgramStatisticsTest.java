package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StreamingStatExtractorProgramStatisticsTest extends TestCase {

    private WMSLogger logger;
    private TicketToolMock ticketTool;

    private static final String defaultQueryString = "ticket=abcd";
    private static final String defaulStreamName = "default-stream-name";
    private static final String defaultResource = "a0639529-124a-453f-b4ea-59f833b47333";
    private static final String defaultUsername = "127.0.0.1";
    private static final String defaulStorageDir = "/vhost/storage/dir";
    private IMediaStreamMock defaultStream;
    private IClientMock defaultIClient;
    private IApplicationInstanceMock defaultIAppInstance;
    private StreamingStatLogEntry defaultStreamingStatLogEntry;

    public StreamingStatExtractorProgramStatisticsTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        this.ticketTool = new TicketToolMock();
        this.defaultIAppInstance = new IApplicationInstanceMock(defaulStorageDir);
        this.defaultIClient = new IClientMock(defaultIAppInstance, logger, defaultQueryString);
        this.defaultStream = new IMediaStreamMock(logger, defaulStreamName, defaultIClient);
        this.defaultStreamingStatLogEntry = createDefaultLogEntry();
    }

    private StreamingStatLogEntry createDefaultLogEntry() {
        List<Property> properties = new ArrayList<Property>();
        // Setup user info
        properties.add(new Property("schacHomeOrganization", "au.dk"));
        properties.add(new Property("eduPersonTargetedID", "1x1"));
        // Setup program info
        properties.add(new Property("metaChannelName", "tv2news"));
        properties.add(new Property("metaTitle", "Nyheder"));
        properties.add(new Property("metaDateTimeStart", "2007-03-04T00:00:00+0100"));
        dk.statsbiblioteket.medieplatform.ticketsystem.Ticket ticket = ticketTool.issueTicket(

                defaultUsername, defaultResource, properties);
        Event logEvent = Event.STREAMING_START;
        // Test
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, defaultStream, logEvent, ticket);
        return logEntry;
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
    public void testAddSingleLogLine() {
        StreamingStatExtractorProgramStatistics programStat = new StreamingStatExtractorProgramStatistics(logger);
        programStat.add(defaultStreamingStatLogEntry);
        int programPlayCount = programStat.getPlayCount(defaultStreamingStatLogEntry.getProgramTitle());
        // Validate
        int expectedPlayCount = 1;
        assertEquals(expectedPlayCount, programPlayCount);
    }

    @Test
    public void testAddMultipleEntries() throws InvalidLogLineParseException, HeadlineEncounteredException {
        StreamingStatExtractorProgramStatistics programStat = new StreamingStatExtractorProgramStatistics(logger);
        // 4x
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-1;Program-1;2011-03-01T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-1;Program-1;2011-03-01T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-1;Program-1;2011-03-01T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-1;Program-1;2011-03-01T20:00:00+0100"));
        // 5x
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-04T20:00:00+0100"));
        // 3x
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-01T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-01T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-2;Program-1;2011-03-01T20:00:00+0100"));
        // 1x
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-01T20:00:00+0100"));
        // 2x
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-07T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-07T20:00:00+0100"));
        // 6x
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-04T20:00:00+0100"));
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-3;Program-1;2011-03-04T20:00:00+0100"));
        // 1x
        programStat.add(new StreamingStatLogEntry(logger,
                                                  "2011-04-12 13:49:26.789;streamID;STREAMING_START;userId;userRole;orgId;Kanal-1;Program-2;2011-02-04T20:00:00+0100"));
        // Result
        logger.info(programStat.toString());

        // Validate
        assertEquals("Total count", 22, programStat.getPlayCount());
        assertEquals("Program-1 count", 21, programStat.getPlayCount("Program-1"));
        assertEquals("Program-1, Kanal-1 count", 4, programStat.getPlayCount("Program-1", "Kanal-1"));
        assertEquals("Program-1, Kanal-2 count", 8, programStat.getPlayCount("Program-1", "Kanal-2"));
        assertEquals("Program-1, Kanal-3 count", 9, programStat.getPlayCount("Program-1", "Kanal-3"));
        assertEquals("Program-1, Kanal-1 2011-03-01 count", 4,
                     programStat.getPlayCount("Program-1", "Kanal-1", "2011-03-01T20:00:00+0100"));
        assertEquals("Program-1, Kanal-2 2011-03-01 count", 3,
                     programStat.getPlayCount("Program-1", "Kanal-2", "2011-03-01T20:00:00+0100"));
        assertEquals("Program-1, Kanal-3 2011-03-04 count", 6,
                     programStat.getPlayCount("Program-1", "Kanal-3", "2011-03-04T20:00:00+0100"));
        assertEquals("Program-2, Kanal-1 2011-02-04 count", 1,
                     programStat.getPlayCount("Program-2", "Kanal-1", "2011-02-04T20:00:00+0100"));
        // Ikke eksisterende
        assertEquals("Program-3 count", 0, programStat.getPlayCount("Program-3"));
        assertEquals("Program-4, Kanal-1 count", 0, programStat.getPlayCount("Program-4", "Kanal-1"));
        assertEquals("Program-1, Kanal-4 count", 0, programStat.getPlayCount("Program-1", "Kanal-4"));
        assertEquals("Program-1, Kanal-1 2011-03-07 count", 0,
                     programStat.getPlayCount("Program-1", "Kanal-1", "2011-03-07T20:00:00+0100"));
        assertEquals("Program-1, Kanal-4 2011-03-07 count", 0,
                     programStat.getPlayCount("Program-1", "Kanal-4", "2011-03-07T20:00:00+0100"));
        assertEquals("Program-3, Kanal-1 2011-03-07 count", 0,
                     programStat.getPlayCount("Program-3", "Kanal-1", "2011-03-07T20:00:00+0100"));
    }

}
