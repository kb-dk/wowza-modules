package dk.statsbiblioteket.chaos.wowza.plugin.statistic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.MCMPortalInterfaceStatisticsMock;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.mcm.MCMPortalInterfaceStatisticsImpl;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.db.StreamingDatabaseEventLogger;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.db.StreamingDatabaseEventLoggerTest;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.mcm.StreamingMCMEventLogger;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingEventLoggerIF;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;

import junit.framework.Assert;
import junit.framework.TestCase;

public class StatisticLoggingStreamListenerTest extends TestCase {
	
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    private WMSLogger logger;
	private Connection connection;
	private StatisticLoggingStreamListener statLogSBMediaStreamActionNotify;
    private StatisticLoggingStreamListener statLogMCMMediaStreamActionNotify;
	private StreamingEventLoggerIF streamingMCMEventLogger;
    private StreamingEventLoggerIF streamingDatabaseEventLogger;
    private MCMPortalInterfaceStatisticsMock mcmPortalInterfaceStatisticsMock;

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

        synchronized (StreamingMCMEventLogger.class) {
            StreamingMCMEventLogger.createInstance(logger);
        }
        this.streamingMCMEventLogger = StreamingMCMEventLogger.getInstance();
	}

	@Before
	public void setUp() throws Exception {
		org.apache.log4j.BasicConfigurator.configure();
		logger.info("setUp()");
        mcmPortalInterfaceStatisticsMock = new MCMPortalInterfaceStatisticsMock(logger);
        MCMPortalInterfaceStatisticsImpl.createInstanceForTestPurpose(mcmPortalInterfaceStatisticsMock);
		IClient client = new IClientMock("sessionID=sample.mp4&objectID=643703&includeFiles=true");
		IMediaStreamMock mediaStream = new IMediaStreamMock("sample2.mp4", client);

        StreamingDatabaseEventLoggerTest.createDBEventTable(logger, connection);

        statLogMCMMediaStreamActionNotify = new StatisticLoggingStreamListener(logger, mediaStream,
                                                                              streamingMCMEventLogger);
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
		Date dateBeforeConnection = new Date();
		IClient client = new IClientMock("queryString");
		IMediaStreamMock mediaStream = new IMediaStreamMock("sample.mp4", client);
		statLogSBMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
		statLogSBMediaStreamActionNotify.onPause(mediaStream, true, 0.0);
        statLogMCMMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
        statLogMCMMediaStreamActionNotify.onPause(mediaStream, true, 0.0);
		dumpDB2Log(10);
		// Fetch data
		StreamingStatLogEntry logEntry = StreamingDatabaseEventLogger.getInstance().getLogEntryLatest();
		logger.debug("Found log entry : " + logEntry.toString());

		Assert.assertEquals("Result is:", 2, logEntry.getEventID());
		Assert.assertEquals("Result is:", mediaStream.getClientId(), logEntry.getUserID());
		Assert.assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
		Assert.assertEquals("Result is:", Event.PAUSE, logEntry.getEvent());

        Assert.assertEquals("Result is:", "0", mcmPortalInterfaceStatisticsMock.lastSessionID);
        Assert.assertEquals("Result is:", "0-0", mcmPortalInterfaceStatisticsMock.lastObjectSessionID);
        Assert.assertEquals("Result is:", 0, mcmPortalInterfaceStatisticsMock.lastStartedAt);
        Assert.assertTrue("Result is:", mcmPortalInterfaceStatisticsMock.lastEndedAt <= 1);
	}

	@Test
	public void testStatisticLoggingSBMediaStreamActionNotify2TestOnStop() throws SQLException, InterruptedException {
		// Establish connection
		Date dateBeforeConnection = new Date();
		IClient client = new IClientMock("queryString");
		IMediaStreamMock mediaStream = new IMediaStreamMock("sample.mp4", client);
		statLogSBMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
		statLogSBMediaStreamActionNotify.onStop(mediaStream);
        statLogMCMMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
        statLogMCMMediaStreamActionNotify.onStop(mediaStream);
		// Fetch data
		StreamingStatLogEntry logEntry = StreamingDatabaseEventLogger.getInstance().getLogEntryLatest();
		logger.debug("Found log entry: " + logEntry.toString());

		Assert.assertEquals("Result is:", 2, logEntry.getEventID());
		Assert.assertEquals("Result is:", mediaStream.getClientId(), logEntry.getUserID());
		Assert.assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
		Assert.assertEquals("Result is:", Event.STOP, logEntry.getEvent());

        Assert.assertEquals("Result is:", "0", mcmPortalInterfaceStatisticsMock.lastSessionID);
        Assert.assertEquals("Result is:", "0-0", mcmPortalInterfaceStatisticsMock.lastObjectSessionID);
        Assert.assertEquals("Result is:", 0, mcmPortalInterfaceStatisticsMock.lastStartedAt);
        Assert.assertTrue("Result is:", mcmPortalInterfaceStatisticsMock.lastEndedAt <= 1);
	}

	@Test
	public void testStatisticLoggingSBMediaStreamActionNotify2TestOnRewind() throws SQLException {
		// Establish connection
		Date dateBeforeConnection = new Date();
		IClient client = new IClientMock("queryString");
		IMediaStreamMock mediaStream = new IMediaStreamMock("sample.mp4", client);
		statLogSBMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
		statLogSBMediaStreamActionNotify.onSeek(mediaStream, 0.0);
        statLogMCMMediaStreamActionNotify.onPlay(mediaStream, mediaStream.getName(), 0.0, 0.0, 0);
        statLogMCMMediaStreamActionNotify.onSeek(mediaStream, 0.0);
		dumpDB2Log(10);
		// Fetch data
		StreamingStatLogEntry logEntry = StreamingDatabaseEventLogger.getInstance().getLogEntryLatest();
		logger.debug("Found log entry: " + logEntry.toString());

		Assert.assertEquals("Result is:", 2, logEntry.getEventID());
		Assert.assertEquals("Result is:", mediaStream.getClientId(), logEntry.getUserID());
		Assert.assertTrue(dateBeforeConnection.getTime() <= logEntry.getTimestamp().getTime());
		Assert.assertEquals("Result is:", Event.REWIND, logEntry.getEvent());

        Assert.assertEquals("Result is:", "0", mcmPortalInterfaceStatisticsMock.lastSessionID);
        Assert.assertEquals("Result is:", "0-0", mcmPortalInterfaceStatisticsMock.lastObjectSessionID);
        Assert.assertEquals("Result is:", 0, mcmPortalInterfaceStatisticsMock.lastStartedAt);
        Assert.assertTrue("Result is:", mcmPortalInterfaceStatisticsMock.lastEndedAt <= 1);
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
