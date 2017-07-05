package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.db;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.SessionIDPair;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingEventLoggerIF;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of the event logger interface, that logs events in a database.
 */
public class StreamingDatabaseEventLogger implements StreamingEventLoggerIF {
    /** The used logger. */
    private final WMSLogger logger;
    private Connection testConnection;
    private String jdbcDriverString;
    private String dbConnectionURLString;
    private String dbUser;
    private String dbPassword;

    private static StreamingDatabaseEventLogger instance = null;
    private int session = 0;

    /**
     * Creates database connection using given properties.
     *
     * @param logger The wowza logger.
     * @param jdbcDriverString The JDBC driver to use.
     * @param dbConnectionURLString The connection string to the database.
     * @param dbUser Database username.
     * @param dbPassword Database password.
     */
    private StreamingDatabaseEventLogger(WMSLogger logger, String jdbcDriverString, String dbConnectionURLString,
                                         String dbUser, String dbPassword) {
        this.logger = logger;
        this.jdbcDriverString = jdbcDriverString;
        this.dbConnectionURLString = dbConnectionURLString;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.logger.info("Statistics logger " + this.getClass().getName() + " has been created.");
    }

    /**
     * Make a connection to the database.
     * @return A database connection.
     */
    private Connection getDBConnection() {
        if (testConnection != null) {
            return testConnection;
        }
        try {
            synchronized (StreamingDatabaseEventLogger.class) {
                try {
                    Class.forName(this.jdbcDriverString);
                } catch (ClassNotFoundException e) {
                    this.logger.error("Could not find the JDBC driver! - " + this.jdbcDriverString, e);
                    throw new RuntimeException("Could not find the JDBC driver! - " + this.jdbcDriverString, e);
                }
                Connection dbConnection = DriverManager.getConnection(this.dbConnectionURLString, this.dbUser, this.dbPassword);
                logger.info("Created connection: " + dbConnection);
                return dbConnection;
            }
        } catch (SQLException sqle) {
            this.logger
                    .error("Could not connect to db with the connection string: " + this.dbConnectionURLString + ", username: " + this.dbUser
                                   + ", password: " + this.dbPassword, sqle);
            throw new RuntimeException(
                    "Could not connect to db with the connection string: " + this.dbConnectionURLString + ", username: " + this.dbUser
                            + ", password: " + this.dbPassword, sqle);
        }
    }

    /** TEST constructor!!! initalises database in alternative fashion. */
    private StreamingDatabaseEventLogger(WMSLogger logger, Connection connection) {
        super();
        this.testConnection = connection;
        this.logger = logger;
        this.logger.info("Statistics logger " + this.getClass().getName() + " has been created. "
                                 + "ONLY FOR TEST PURPOSE! DB-connection not established in a safe way.");
    }

    /**
     * Creates the singleton objects - test version. Is robust for multiple concurrent requests for create.
     * Only the first request for create, actually creates the object.
     *
     * CAUTION! Once the test singleton is created, since it's a singleton, a non-test version will not be possible.
     *
     * @param logger The WMS logger.
     * @param connection The database connection.
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
    public static synchronized void createInstance(WMSLogger logger, String jdbcDriverString,
                                                   String dbConnectionURLString, String dbUser, String dbPassword)
            throws FileNotFoundException, IOException {
        if ((logger == null)) {
            throw new IllegalArgumentException(
                    "A parameter is null. " + "logger=" + logger);
        }
        if (instance == null) {
            instance = new StreamingDatabaseEventLogger(logger, jdbcDriverString, dbConnectionURLString, dbUser,
                                                        dbPassword);
        }
    }

    /**
     * Get the singleton instance, assuming it is already initialised.
     * @return The singleton instance.
     */
    public static synchronized StreamingDatabaseEventLogger getInstance() {
        return instance;
    }

    /**
     * Generate the session id for a log string.
     * In the database logging, this is just a counter increasing with one on each call, plus a copy
     * of the object id.
     * @param mcmObjectID The MCM object id.
     * @return The session id.
     */
    @Override
    public SessionIDPair getStreamingLogSessionID(String mcmObjectID) {
        return new SessionIDPair(Integer.toString(session++), session + "-" + mcmObjectID);
    }

    /**
     * Log an event in the database.
     * @param logEntry
     */
    @Override
    public void logEvent(StreamingStatLogEntry logEntry) {
        if (Event.PLAY.equals(logEntry.getEvent()) || Event.PAUSE.equals(logEntry.getEvent())
                || Event.REWIND.equals(logEntry.getEvent()) || Event.STOP.equals(logEntry.getEvent())) {
            logger.info("Streaming statistics logging line: " + logEntry);
            logger.info("Streaming statistics logging line (in DB): " + logEntry);
            logEventInDB(logEntry);
        }
    }

    /**
     * Log event in database. Adds a row to the database with the information in the log entry.
     * Will log errors in log files, but otherwise ognore them.
     * @param logEntry The log entry to add.
     */
    private synchronized void logEventInDB(StreamingStatLogEntry logEntry) {
        String query = "INSERT INTO events VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = getDBConnection().prepareStatement(query)) {
            logEntry.setEventID(getNextEventID());
            logger.info("Next event id: " + logEntry.getEventID());
            stmt.setLong(1, logEntry.getEventID());
            stmt.setTimestamp(2, new Timestamp(logEntry.getTimestamp().getTime()));
            stmt.setString(3, logEntry.getStreamName());
            stmt.setString(4, logEntry.getEvent().toString());
            stmt.setInt(5, logEntry.getUserID());
            stmt.setLong(6, logEntry.getStartedAt());
            stmt.setLong(7, logEntry.getEndedAt());
            stmt.setString(8, logEntry.getWayfAttr());
            logger.info("Executing query: " + query);
            stmt.executeUpdate();
            logger.info("Creating event: " + query);
        } catch (SQLException e) {
            logger.error(
                    "An SQL exception occured during onConnect call.", e);
        }
    }

    /**
     * Gets next event ID to generate log entry.
     * @return Next event ID. Assumes no other process is writing event IDs to the database.
     * @throws SQLException on database trouble.
     */
    protected synchronized int getNextEventID() throws SQLException {
        try (Statement stmt = getDBConnection().createStatement()) {
            String queryString = "SELECT MAX(event_id) as max_event_id FROM events";
            logger.info("[TEST] Executing query: " + queryString);
            ResultSet rs = stmt.executeQuery(queryString);
            rs.next();
            int eventID = rs.getInt("max_event_id");
            return eventID + 1;
        }
        /*
                 ResultSet resultSet = stmt.getGeneratedKeys();
                 resultSet.getLong("event_id");
                 */
    }

    /**
     * Return the newest log event from database.
     * @return
     */
    @Override
    public StreamingStatLogEntry getLogEntryLatest() {
        StreamingStatLogEntry logEntry = null;
        List<StreamingStatLogEntry> logEntryLatest = getLogEntryLatest(1);
        if (!logEntryLatest.isEmpty()) {
            logEntry = logEntryLatest.get(0);
        }
        return logEntry;
    }

    /**
     * Return the newest numberOfEntries log event from database.
     * @param numberOfEntries The number of log events to return.
     * @return
     */
    public List<StreamingStatLogEntry> getLogEntryLatest(int numberOfEntries) {
        List<StreamingStatLogEntry> logEntries = new ArrayList<StreamingStatLogEntry>();
        try (Statement stmt = getDBConnection().createStatement()) {
            String mcmSessionID = null;
            String mcmObjectSessionID = null;
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
                String wayfAttr = rs.getString("wayf_attr");
                StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, eventID, timestamp, streamName,
                                                                           userID, mcmSessionID, mcmObjectSessionID,
                                                                           startedAt, endedAt, StreamingStatLogEntry
                        .getEventFromString(eventType), wayfAttr);
                logEntries.add(logEntry);
                logger.debug("Resulting log entry: " + logEntry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve latest logged event.", e);
        }
        return logEntries;
    }
}
