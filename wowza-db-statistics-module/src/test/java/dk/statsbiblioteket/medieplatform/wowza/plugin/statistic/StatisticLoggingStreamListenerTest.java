package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingEventLoggerIF;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.db.StreamingDatabaseEventLogger;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.db.StreamingDatabaseEventLoggerTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test logging in the stream listener.
 */
public class StatisticLoggingStreamListenerTest {
    
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);

    private WMSLogger logger;
    private Connection connection;
    private StatisticLoggingStreamListener statLogSBMediaStreamActionNotify;
    private StreamingEventLoggerIF streamingDatabaseEventLogger;

    public StatisticLoggingStreamListenerTest() throws FileNotFoundException, IOException, SQLException {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        try {
            Class.forName("org.hsqldb.jdbcDriver" );
        } catch (Exception e) {
            System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            return;
        }
        this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:streamingstats");
        StreamingDatabaseEventLogger.createInstanceForTestPurpose(logger, connection);
        this.streamingDatabaseEventLogger = StreamingDatabaseEventLogger.getInstance();
    }

    @BeforeEach
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        logger.info("setUp()");
        IMediaStream mediaStream = mock(IMediaStream.class);

        StreamingDatabaseEventLoggerTest.createDBEventTable(logger, connection);

        statLogSBMediaStreamActionNotify = new StatisticLoggingStreamListener(logger, mediaStream,
                                                                              streamingDatabaseEventLogger);
    }

    @AfterEach
    public void tearDown() throws Exception {
        StreamingDatabaseEventLoggerTest.dropDBTable(logger, connection);
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testStatisticLoggingSBMediaStreamActionNotify2TestOnPause() throws SQLException {
        // Establish connection
        String queryString = "sessionID=sample.mp4&objectID=643703&includeFiles=true&wayfAttr=dGVzdCAK";
        Date dateBeforeConnection = new Date();
        
        IClient client = mock(IClient.class);
        when(client.getQueryStr()).thenReturn(queryString);
        IMediaStream mediaStream = mock(IMediaStream.class);
        when(mediaStream.getClient()).thenReturn(client);
        when(mediaStream.getQueryStr()).thenReturn(queryString);
        when(mediaStream.getExt()).thenReturn("flv");
        when(mediaStream.getName()).thenReturn("sample.mp4");
        
        statLogSBMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
        statLogSBMediaStreamActionNotify.onPause(mediaStream, true, 0.0);
        dumpDB2Log(10);
        // Fetch data
        StreamingStatLogEntry logEntry = StreamingDatabaseEventLogger.getInstance().getLogEntryLatest();
        logger.debug("Found log entry : " + logEntry.toString());

        assertEquals(2, logEntry.getEventID(), "Result is:");
        assertEquals(mediaStream.getClientId(), logEntry.getUserID(), "Result is:");
        assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
        assertEquals(Event.PAUSE, logEntry.getEvent(), "Result is:");
        assertEquals("test \n", logEntry.getWayfAttr(), "Result is:");
    }

    @Test
    public void testStatisticLoggingSBMediaStreamActionNotify2TestOnStop() throws SQLException, InterruptedException {
        // Establish connection
        Date dateBeforeConnection = new Date();
        IClient client = mock(IClient.class);
        IMediaStream mediaStream = mock(IMediaStream.class);
        when(mediaStream.getClient()).thenReturn(client);
        when(mediaStream.getName()).thenReturn("sample.mp4");
        
        statLogSBMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
        statLogSBMediaStreamActionNotify.onStop(mediaStream);
        // Fetch data
        StreamingStatLogEntry logEntry = StreamingDatabaseEventLogger.getInstance().getLogEntryLatest();
        logger.debug("Found log entry: " + logEntry.toString());

        assertEquals(2, logEntry.getEventID(), "Result is:");
        assertEquals(mediaStream.getClientId(), logEntry.getUserID(), "Result is:");
        assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
        assertEquals(Event.STOP, logEntry.getEvent(), "Result is:");
    }

    @Test
    public void testStatisticLoggingSBMediaStreamActionNotify2TestOnRewind() throws SQLException {
        // Establish connection
        Date dateBeforeConnection = new Date();
        IClient client = mock(IClient.class);
        IMediaStream mediaStream = mock(IMediaStream.class);
        when(mediaStream.getClient()).thenReturn(client);
        when(mediaStream.getName()).thenReturn("sample.mp4");

        statLogSBMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
        statLogSBMediaStreamActionNotify.onSeek(mediaStream, 0.0);
        dumpDB2Log(10);
        // Fetch data
        StreamingStatLogEntry logEntry = StreamingDatabaseEventLogger.getInstance().getLogEntryLatest();
        logger.debug("Found log entry: " + logEntry.toString());

        assertEquals(2, logEntry.getEventID(), "Result is:");
        assertEquals(mediaStream.getClientId(), logEntry.getUserID(), "Result is:");
        assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
        assertEquals(Event.REWIND, logEntry.getEvent(), "Result is:");
    }

    private void dumpDB2Log(int numberOfEntries) {
        List<StreamingStatLogEntry> logEntries = StreamingDatabaseEventLogger.getInstance().getLogEntryLatest(
                numberOfEntries);
        int i=0;
        logger.debug("Dumping " + logEntries.size() + "/" + numberOfEntries + " entries to the log");
        for (StreamingStatLogEntry logEntry: logEntries) {
            logger.debug("Log entry [" + i + "] : " + logEntry.toString());
            i++;
        }
    }
}
