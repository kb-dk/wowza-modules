package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnHTTPSession;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;

import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.SessionIDPair;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.db.StreamingDatabaseEventLogger;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Module that logs events to a database.
 */
public class StatisticLoggingSBModuleBase extends ModuleBase
        implements IModuleOnApp, IModuleOnStream, IModuleOnHTTPSession {

    private static final String PLUGIN_NAME = "CHAOS Wowza plugin - Statistics SB";
    private static final String PLUGIN_VERSION = StatisticLoggingSBModuleBase.class.getPackage().getImplementationVersion();

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    private static final String PROPERTY_STATISTICS_LOGGING_JDBC_DRIVER = "StatisticsLoggingJDBCDriver";
    private static final String PROPERTY_STATISTICS_LOGGING_DB_CONNECTION_URL = "StatisticsLoggingDBConnectionURL";
    private static final String PROPERTY_STATISTICS_LOGGING_DBUSER = "StatisticsLoggingDBUser";
    private static final String PROPERTY_STATISTICS_LOGGING_DB_PASSWORD = "StatisticsLoggingDBPassword";

    /**
     * Default constructor.
     */
    public StatisticLoggingSBModuleBase() {
        super();
    }

    /**
     * On App Start, initialise database connection.
     * @param appInstance The app being initialised.
     */
    @Override
    public void onAppStart(IApplicationInstance appInstance) {
        String appName = appInstance.getApplication().getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger()
                .info("***Entered onAppStart: " + appName + "\n  Plugin: " + PLUGIN_NAME + " version " + PLUGIN_VERSION
                              + "\n  VHost home path: " + vhostDir + " VHost storage dir: " + storageDir);
        try {
            //Initialise the config reader
            ConfigReader cr;
            cr = new ConfigReader(new File(vhostDir + "/conf/" + appName + "/wowza-modules.properties"),
                                  PROPERTY_STATISTICS_LOGGING_JDBC_DRIVER,
                                  PROPERTY_STATISTICS_LOGGING_DB_CONNECTION_URL, PROPERTY_STATISTICS_LOGGING_DBUSER,
                                  PROPERTY_STATISTICS_LOGGING_DB_PASSWORD);

            //Read parameters
            String jdbcDriverString = cr.get(PROPERTY_STATISTICS_LOGGING_JDBC_DRIVER);
            String dbConnectionURLString = cr.get(PROPERTY_STATISTICS_LOGGING_DB_CONNECTION_URL);
            String dbUser = cr.get(PROPERTY_STATISTICS_LOGGING_DBUSER);
            String dbPassword = cr.get(PROPERTY_STATISTICS_LOGGING_DB_PASSWORD);

            //Initialize event logger
            StreamingDatabaseEventLogger
                    .createInstance(getLogger(), jdbcDriverString, dbConnectionURLString, dbUser, dbPassword);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize StreamingDatabaseEventLogger.", e);
        }
    }

    /**
     * On App Stop nothing special is done.
     * @param appInstance
     */
    @Override
    public void onAppStop(IApplicationInstance appInstance) {
        getLogger().info("onAppStop: " + PLUGIN_NAME + " version " + PLUGIN_VERSION);
    }

    /**
     * On stream creation, add a listener that logs events to the database, unless the querystring contains the
     * entry "streamActionNotifierForStatistics=off"
     * @param stream The stream being added.
     */
    @Override
    public void onStreamCreate(IMediaStream stream) {
        getLogger().info("onStreamCreate by: " + stream.getClientId());
        String queryString = getQueryString(stream);
        String statisticsParameter;

        //Check if statistics are turned off
        try {
            statisticsParameter = StringAndTextUtil.extractValueFromQueryStringAndKey("statistics", queryString);
            if (statisticsParameter.equalsIgnoreCase("off")) {
                return;
            }
        } catch (IllegallyFormattedQueryStringException e) {
            //Not turned off, so ignore
        }

        IMediaStreamActionNotify streamActionNotify = new StatisticLoggingStreamListener(getLogger(), stream,
                                                                                         StreamingDatabaseEventLogger
                                                                                                 .getInstance());
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            props.put("streamActionNotifierForStatistics", streamActionNotify);
        }
        stream.addClientListener(streamActionNotify);
    }

    /**
     * Helper method to get query string.
     * @param stream THe stream to get query string from
     * @return Query string
     */
    private String getQueryString(IMediaStream stream) {
        if (stream.getClient() != null) {
            return String.valueOf(stream.getClient().getQueryStr());
        } else if (stream.getHTTPStreamerSession() != null) {
            return String.valueOf(stream.getHTTPStreamerSession().getQueryStr());
        } else {
            return "";
        }
    }

    /**
     * On stream destruction, remove the action listener that logs events.
     * @param stream
     */
    @Override
    public void onStreamDestroy(IMediaStream stream) {
        getLogger().info("onStreamDestroy by: " + stream.getClientId());
        IMediaStreamActionNotify actionNotify;
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            actionNotify = (IMediaStreamActionNotify) props.get("streamActionNotifierForStatistics");
        }
        if (actionNotify != null) {
            stream.removeClientListener(actionNotify);
            getLogger().info("removeClientListener: " + stream.getSrc());
        }
    }

    /**
     * On HTTP connection, log an event in the database.
     * Note there are no action listeners for HTTP streaming.
     * @param ihttpStreamerSession The http session initiated.
     */
    @Override
    public void onHTTPSessionCreate(IHTTPStreamerSession ihttpStreamerSession) {
        getLogger().info("onHttpSessionCreate by: " + ihttpStreamerSession.getIpAddress());

        String queryString = String.valueOf(ihttpStreamerSession.getQueryStr());
        String mcmObjectID;
        try {
            mcmObjectID = StringAndTextUtil.extractValueFromQueryStringAndKey("ObjectID", queryString);
        } catch (IllegallyFormattedQueryStringException e) {
            mcmObjectID = "Unknown";
        }
        StreamingDatabaseEventLogger eventLogger = StreamingDatabaseEventLogger.getInstance();
        SessionIDPair sessionIDPair = eventLogger.getStreamingLogSessionID(mcmObjectID);
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(getLogger(),
                ihttpStreamerSession.getStreamName(),
                0,
                sessionIDPair.getSessionID(),
                sessionIDPair.getObjectSessionID(),
                0L,
                0L,
                StreamingStatLogEntry.Event.PLAY);
        eventLogger.logEvent(logEntry);
    }

    /**
     * On HTTP disconnection, log an event in the database.
     * Note there are no action listeners for HTTP streaming.
     * @param ihttpStreamerSession The http session initiated.
     */
    @Override
    public void onHTTPSessionDestroy(IHTTPStreamerSession ihttpStreamerSession) {
        getLogger().info("onHttpSessionDestroy by: " + ihttpStreamerSession.getIpAddress());

        String queryString = String.valueOf(ihttpStreamerSession.getQueryStr());
        String mcmObjectID;
        try {
            mcmObjectID = StringAndTextUtil.extractValueFromQueryStringAndKey("ObjectID", queryString);
        } catch (IllegallyFormattedQueryStringException e) {
            mcmObjectID = "Unknown";
        }
        StreamingDatabaseEventLogger eventLogger = StreamingDatabaseEventLogger.getInstance();
        SessionIDPair sessionIDPair = eventLogger.getStreamingLogSessionID(mcmObjectID);
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(getLogger(),
                ihttpStreamerSession.getStreamName(),
                0,
                sessionIDPair.getSessionID(),
                sessionIDPair.getObjectSessionID(),
                0L,
                0L,
                StreamingStatLogEntry.Event.STOP);
        eventLogger.logEvent(logEntry);
    }
}
