package dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.db;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.SessionIDPair;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingEventLoggerIF;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.chaos.wowza.plugin.util.PropertiesUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StreamingDatabaseEventLogger implements StreamingEventLoggerIF {

    private static final String propertyStatisticsLoggingJDBCDriver = "StatisticsLoggingJDBCDriver";
    private static final String propertyStatisticsLoggingDBConnectionURL = "StatisticsLoggingDBConnectionURL";
    private static final String propertyStatisticsLoggingDBUser = "StatisticsLoggingDBUser";
    private static final String propertyStatisticsLoggingDBPassword = "StatisticsLoggingDBPassword";

    private final WMSLogger logger;
    private String jdbcDriverString;
    private String dbConnectionURLString;
    private String dbUser;
    private String dbPassword;

    private static StreamingDatabaseEventLogger instance = null;
    private static Connection dbConnection = null;
    private int session = 0;

    /**
     * Reads db connection information from property file and creates connection
     *
     * @param logger The wowza logger.
     */
    private StreamingDatabaseEventLogger(WMSLogger logger) {
        this.logger = logger;
        if (dbConnection == null) {
            this.jdbcDriverString = PropertiesUtil.getProperty(propertyStatisticsLoggingJDBCDriver);
            this.dbConnectionURLString = PropertiesUtil.getProperty(propertyStatisticsLoggingDBConnectionURL);
            this.dbUser = PropertiesUtil.getProperty(propertyStatisticsLoggingDBUser);
            this.dbPassword = PropertiesUtil.getProperty(propertyStatisticsLoggingDBPassword);
            dbConnection = getNewConnection(this.logger, jdbcDriverString, dbConnectionURLString, dbUser, dbPassword);
            logger.info("Created connection: " + dbConnection);
        }
        this.logger.info("Statistics logger " + this.getClass().getName() + " has been created.");
    }

    /** TEST constructor!!! */
    private StreamingDatabaseEventLogger(WMSLogger logger, Connection connection) {
        super();
        this.logger = logger;
        StreamingDatabaseEventLogger.dbConnection = connection;
        this.logger.info("Statistics logger " + this.getClass().getName() + " has been created. "
                                 + "ONLY FOR TEST PURPOSE! DB-connection not established in a safe way.");
    }

    /**
     * Creates the singleton objects. Is robust for multiple concurrent requests for create.
     * Only the first request for create, actually creates the object.
     */
    public static synchronized void createInstanceForTestPurpose(WMSLogger logger, Connection connection)
            throws FileNotFoundException, IOException {
        if ((logger == null) || (connection == null)) {
            throw new IllegalArgumentException(
                    "A parameter is null. " + "logger=" + logger + " " + "connection=" + connection);
        }
        if (instance == null) {
            instance = new StreamingDatabaseEventLogger(logger, connection);
        }
    }

    /**
     * Creates the singleton objects. Is robust for multiple concurrent requests for create.
     * Only the first request for create, actually creates the object.
     */
    public static synchronized void createInstance(WMSLogger logger, String vHostHomeDirPath)
            throws FileNotFoundException, IOException {
        if ((logger == null) || (vHostHomeDirPath == null)) {
            throw new IllegalArgumentException(
                    "A parameter is null. " + "logger=" + logger + " " + "vHostHomeDirPath=" + vHostHomeDirPath);
        }
        if (instance == null) {
            instance = new StreamingDatabaseEventLogger(logger);
        }
    }

    public static synchronized StreamingDatabaseEventLogger getInstance() {
        return instance;
    }

    @Override
    public SessionIDPair getStreamingLogSessionID(String mcmObjectID) {
        return new SessionIDPair(Integer.toString(session++), session + "-" + mcmObjectID);
    }

    @Override
    public void logEvent(StreamingStatLogEntry logEntry) {
        if (Event.PLAY.equals(logEntry.getEvent()) || Event.PAUSE.equals(logEntry.getEvent())
                || Event.REWIND.equals(logEntry.getEvent()) || Event.STOP.equals(logEntry.getEvent())) {
            logger.info("Streaming statistics logging line: " + logEntry);
            logger.info("Streaming statistics logging line (in DB): " + logEntry);
            logEventInDB(logEntry);
        }
    }

    private synchronized void logEventInDB(StreamingStatLogEntry logEntry) {
        try {
            logEntry.setEventID(getNextEventID());
            logger.info("Next event id: " + logEntry.getEventID());
            Statement stmt = dbConnection.createStatement();
            String query = "INSERT INTO events VALUES (" + logEntry.getEventID() + ", " + "'"
                    + logEntry.getTimestampAsString() + "', " + "'" + logEntry.getStreamName() + "', " + "'"
                    + logEntry.getEvent() + "', " + logEntry.getUserID() + ", " + logEntry.getStartedAt() + ", "
                    + logEntry.getEndedAt() + ");";
            logger.info("Executing query: " + query);
            stmt.executeUpdate(query);
            logger.info("Creating event: " + query);
        } catch (SQLException e) {
            logger.error(
                    "An SQL exception occured during onConnect call. " + "Connection was: " + dbConnection.toString(),
                    e);
        }
    }

    protected synchronized int getNextEventID() throws SQLException {
        Statement stmt = dbConnection.createStatement();
        String queryString = "SELECT MAX(event_id) as max_event_id FROM events";
        logger.info("[TEST] Executing query: " + queryString);
        ResultSet rs = stmt.executeQuery(queryString);
        rs.next();
        int eventID = rs.getInt("max_event_id");
        return eventID + 1;
        /*
                 ResultSet resultSet = stmt.getGeneratedKeys();
                 resultSet.getLong("event_id");
                 */
    }

    protected synchronized static Connection getNewConnection(WMSLogger logger, String jdbcDriver, String connectionURL,
                                                              String user, String password) {
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            logger.error("Could not find the JDBC driver! - " + jdbcDriver, e);
            throw new RuntimeException("Could not find the JDBC driver! - " + jdbcDriver, e);
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connectionURL, user, password);
        } catch (SQLException sqle) {
            logger.error("Could not connect to db with the connection string: " + connectionURL + ", username: " + user
                                 + ", password: " + password, sqle);
            throw new RuntimeException(
                    "Could not connect to db with the connection string: " + connectionURL + ", username: " + user
                            + ", password: " + password, sqle);
        }
        return conn;
    }

    @Override
    public StreamingStatLogEntry getLogEntryLatest() {
        StreamingStatLogEntry logEntry = null;
        try {
            String mcmSessionID = null;
            String mcmObjectSessionID = null;
            Statement stmt = dbConnection.createStatement();
            String queryString = "SELECT * FROM events ORDER BY event_id DESC LIMIT 1";
            logger.info("Executing query: " + queryString);
            ResultSet rs = stmt.executeQuery(queryString);
            if (rs.next()) {
                long eventID = rs.getLong("event_id");
                Date timestamp = rs.getTimestamp("timestamp");
                int userID = rs.getInt("user_id");
                String streamName = rs.getString("stream_name");
                String eventType = rs.getString("event_type");
                long startedAt = rs.getLong("started_at");
                long endedAt = rs.getLong("ended_at");
                logEntry = new StreamingStatLogEntry(logger, eventID, timestamp, streamName, userID, mcmSessionID,
                                                     mcmObjectSessionID, startedAt, endedAt,
                                                     StreamingStatLogEntry.getEventFromString(eventType));
            }
            logger.debug("Resulting log entry: " + logEntry);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve latest logged event.", e);
        }
        return logEntry;
    }

    public List<StreamingStatLogEntry> getLogEntryLatest(int numberOfEntries) {
        List<StreamingStatLogEntry> logEntries = new ArrayList<StreamingStatLogEntry>();
        try {
            String mcmSessionID = null;
            String mcmObjectSessionID = null;
            Statement stmt = dbConnection.createStatement();
            String queryString = "SELECT * FROM events ORDER BY event_id DESC LIMIT " + numberOfEntries;
            logger.info("Executing query: " + queryString);
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                long eventID = rs.getLong("event_id");
                Date timestamp = rs.getTimestamp("timestamp");
                int userID = rs.getInt("user_id");
                String streamName = rs.getString("stream_name");
                String eventType = rs.getString("event_type");
                long startedAt = rs.getLong("started_at");
                long endedAt = rs.getLong("ended_at");
                StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, eventID, timestamp, streamName,
                                                                           userID, mcmSessionID, mcmObjectSessionID,
                                                                           startedAt, endedAt, StreamingStatLogEntry
                        .getEventFromString(eventType));
                logEntries.add(logEntry);
                logger.debug("Resulting log entry: " + logEntry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve latest logged event.", e);
        }
        return logEntries;
    }
}
