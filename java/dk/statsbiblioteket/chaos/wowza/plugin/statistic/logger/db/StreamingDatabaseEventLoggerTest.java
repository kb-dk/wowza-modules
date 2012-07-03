package dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.MCMPortalInterfaceStatisticsMock;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.StatisticLoggingStreamListener;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.mcm.MCMPortalInterfaceStatisticsImpl;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingEventLoggerIF;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;

public class StreamingDatabaseEventLoggerTest {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    private WMSLogger logger;
	private Connection connection;
	private StreamingEventLoggerIF streamingEventLogger;

	public StreamingDatabaseEventLoggerTest() throws FileNotFoundException, IOException, SQLException {
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
        synchronized (StreamingDatabaseEventLogger.class) {
            StreamingDatabaseEventLogger.createInstanceForTestPurpose(logger, this.connection);
        }
        this.streamingEventLogger = StreamingDatabaseEventLogger.getInstance();
	}

	@Before
	public void setUp() throws Exception {
		org.apache.log4j.BasicConfigurator.configure();
		IClient client = new IClientMock("sessionID=sample.mp4&objectID=643703&includeFiles=true");
		IMediaStreamMock stream = new IMediaStreamMock("sample2.mp4", client);
        MCMPortalInterfaceStatisticsImpl.createInstanceForTestPurpose(new MCMPortalInterfaceStatisticsMock(logger));
		new StatisticLoggingStreamListener(logger, stream, streamingEventLogger);
		createDBEventTable(logger, this.connection);
	}

	public static void createDBEventTable(WMSLogger logger, Connection connection) throws SQLException {
		Statement stmt2 = connection.createStatement();
		String query = "CREATE TABLE events (" +
			"event_id INTEGER NOT NULL, " + 
			"timestamp TIMESTAMP, " +
			"stream_name VARCHAR, " +
			"event_type VARCHAR, " +
			"user_id INTEGER, " +
			"started_at INTEGER, " +
			"ended_at INTEGER, " +
			"PRIMARY KEY (event_id))";
		stmt2.executeUpdate(query);
		Statement stmt1 = connection.createStatement();
		logger.info("Execute: " + query);
	}
	
	public static void dropDBTable(WMSLogger logger, Connection connection) throws SQLException {
		logger.info("dropTable - test");
		Statement stmt = connection.createStatement();
		String query = "DROP TABLE events";
		stmt.executeUpdate(query);
		logger.info("Execute: " + query);
	}

	@After
	public void tearDown() throws Exception {
		org.apache.log4j.BasicConfigurator.resetConfiguration();
		dropDBTable(logger, connection);
	}

	/**
	 * The purpose of this test is to verify that a db connection can be made to 
	 * the test db.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testStatiticsLoggingSBModuleBaseDBConnectionForTestPurpose() throws SQLException {
		Statement stmt1 = connection.createStatement();
		stmt1.executeUpdate("INSERT INTO events VALUES (2, '2011-02-23 10:56:30.654', 'sample2.mp4', '" + Event.PLAY + "', 1, 0, 3000);");
		//stmt1.executeQuery("INSERT INTO events VALUES (2, 1, '2011-02-23 22:56:30', 'slut');");
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM events WHERE event_id = 2");
		rs.next();
		long eventID = rs.getLong("event_id");
		int userID = rs.getInt("user_id");
		Date eventTime = new Date(rs.getTimestamp("timestamp").getTime());
		String event = rs.getString("event_type");
		// Create date representing the date inserted to the db. Gregorian time does not work with ms.
		Date date = new Date(new GregorianCalendar(2011, 01 /*0-based month*/, 23, 10, 56, 30).getTime().getTime() + 654);
		Assert.assertEquals("Result is:", 2, eventID);
		Assert.assertEquals("Result is:", 1, userID);
		Assert.assertEquals("Result is:", sdf.format(date), sdf.format(eventTime));
		Assert.assertEquals("Result is:", "PLAY", event);
	}

	/**
	 * Test that a play event is registered in the database as an event.
	 * 
	 * @throws SQLException
	 * @throws ParseException 
	 */
	@Test
	public void testStatisticLoggingSBMediaStreamActionNotify2TestOnPlay() throws SQLException, ParseException {
		// Establish connection
		Date dateBeforeConnection = new Date();
		int clientID = 3;
		String streamName = "sample.mp4";
		String mcmSessionID = "abcdef";
		String mcmObjectSessionID = "xyz";
		StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, streamName, clientID, mcmSessionID, mcmObjectSessionID, 0, 0, Event.PLAY);
		streamingEventLogger.logEvent(logEntry);
		// Fetch event information in db
		String eventHappensAfterThisDate = sdf.format(dateBeforeConnection);
		Statement stmt = connection.createStatement();
		String queryString = "SELECT * FROM events WHERE timestamp >= '" + eventHappensAfterThisDate + "'";
		logger.info("[TEST] Executing query: " + queryString);
		ResultSet rs = stmt.executeQuery(queryString);
		rs.next();
		long eventID = rs.getLong("event_id");
		int userID = rs.getInt("user_id");
		Date timestamp = rs.getTimestamp("timestamp");
		String event = rs.getString("event_type");

		logger.debug("Log entry: " + logEntry);
		logger.debug("DB result: (" + eventID + "," + userID + "," + timestamp + "," + event +")");
		Assert.assertEquals("Result is:", logEntry.getEventID(), eventID);
		Assert.assertEquals("Result is:", clientID, userID);
		Assert.assertEquals("Result is:", logEntry.getTimestamp(), timestamp);
		Assert.assertEquals("Result is:", Event.PLAY.toString(), event);
	}
	
	/**
	 * Test that we can get the next id.
	 * 
	 * @throws SQLException
	 * @throws ParseException 
	 */
	@Test
	public void testStatisticLoggingSBMediaStreamActionNotify2TestGetNextEventID() throws SQLException, ParseException {
		// Establish connection
		Date dateBeforeConnection = new Date();
		// Log entry
		long maxPriorToUpdate = getMaxEventID();
		int clientID = 3;
		String streamName = "sample.mp4";
		String mcmSessionID = "abcdef";
		String mcmObjectSessionID = "xyz";
		StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, streamName, clientID, mcmSessionID, mcmObjectSessionID, 0, 0, Event.PLAY);
		streamingEventLogger.logEvent(logEntry);
		long maxAfterUpdate = getMaxEventID();
		Assert.assertEquals("Test that the new log entry is 1 larger that previous max", maxPriorToUpdate+1, maxAfterUpdate);
		
		// Fetch event information in db
		String eventHappensAfterThisDate = sdf.format(dateBeforeConnection);
		Statement stmt = connection.createStatement();
		String queryString = "SELECT * FROM events WHERE timestamp >= '" + eventHappensAfterThisDate + "'";
		logger.info("[TEST] Executing query: " + queryString);
		ResultSet rs = stmt.executeQuery(queryString);
		rs.next();
		long eventID = rs.getLong("event_id");
		int userID = rs.getInt("user_id");
		Date timestamp = rs.getTimestamp("timestamp");
		String event = rs.getString("event_type");

		logger.debug("Log entry: " + logEntry);
		logger.debug("DB result: (" + eventID + "," + userID + "," + timestamp + "," + event +")");
		Assert.assertEquals("Test that the new eventID is equal to max of eventID's", maxAfterUpdate, eventID);
	}
	
	private long getMaxEventID() throws SQLException {
		Statement stmt = connection.createStatement();
		String queryString = "SELECT MAX(event_id) as max_event_id FROM events";
		logger.info("[TEST] Executing query: " + queryString);
		ResultSet rs = stmt.executeQuery(queryString);
		rs.next();
		long eventID = rs.getLong("max_event_id");
		return eventID;
	}

}
