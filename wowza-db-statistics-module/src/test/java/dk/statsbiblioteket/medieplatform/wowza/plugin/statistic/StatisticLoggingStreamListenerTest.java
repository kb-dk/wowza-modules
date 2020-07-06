package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test logging in the stream listener.
 */
public class StatisticLoggingStreamListenerTest extends TestCase {
    
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

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

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        logger.info("setUp()");
        IMediaStream mediaStream = mock(IMediaStream.class);

        StreamingDatabaseEventLoggerTest.createDBEventTable(logger, connection);

        statLogSBMediaStreamActionNotify = new StatisticLoggingStreamListener(logger, mediaStream,
                                                                              streamingDatabaseEventLogger);
    }

    @After
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

        Assert.assertEquals("Result is:", 2, logEntry.getEventID());
        Assert.assertEquals("Result is:", mediaStream.getClientId(), logEntry.getUserID());
        Assert.assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
        Assert.assertEquals("Result is:", Event.PAUSE, logEntry.getEvent());
        Assert.assertEquals("Result is:", "test \n", logEntry.getWayfAttr());
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

        Assert.assertEquals("Result is:", 2, logEntry.getEventID());
        Assert.assertEquals("Result is:", mediaStream.getClientId(), logEntry.getUserID());
        Assert.assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
        Assert.assertEquals("Result is:", Event.STOP, logEntry.getEvent());
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

        Assert.assertEquals("Result is:", 2, logEntry.getEventID());
        Assert.assertEquals("Result is:", mediaStream.getClientId(), logEntry.getUserID());
        Assert.assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
        Assert.assertEquals("Result is:", Event.REWIND, logEntry.getEvent());
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
