package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnHTTPSession;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.mcm.MCMPortalInterfaceStatisticsImpl;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.mcm.StreamingMCMEventLogger;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

/**
 * Module that logs events in the MCM database.
 */
public class StatisticLoggingMCMModuleBase extends ModuleBase implements IModuleOnApp, IModuleOnStream,
        IModuleOnHTTPSession {

    private static final String PLUGIN_NAME = "CHAOS Wowza plugin - Statistics MCM";
    private static final String PLUGIN_VERSION =
            StatisticLoggingMCMModuleBase.class.getPackage().getImplementationVersion();

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    private static final String PROPERTY_GENERAL_MCM_SERVER_URL = "GeneralMCMServerURL";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_SESSION
            = "StatisticsLoggingMCMStatisticsMethodCreateStatSession";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CLIENT_SETTING_ID
            = "StatisticsLoggingMCMValueClientSettingID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_REPOSITORY_ID
            = "StatisticsLoggingMCMValueRepositoryID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_OBJECT_SESSION
            = "StatisticsLoggingMCMStatisticsMethodCreateStatObjectSession";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TYPE_ID
            = "StatisticsLoggingMCMValueObjectTypeID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_TYPE_ID
            = "StatisticsLoggingMCMValueChannelTypeID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_IDENTIFIER
            = "StatisticsLoggingMCMValueChannelIdentifier";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TITLE
            = "StatisticsLoggingMCMValueObjectTitle";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_EVENT_TYPE_ID
            = "StatisticsLoggingMCMValueEventTypeID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_COLLECTION_ID
            = "StatisticsLoggingMCMValueObjectCollectionID";
    private static final String PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_DURATION_SESSION
            = "StatisticsLoggingMCMStatisticsMethodCreateDurationSession";

    public StatisticLoggingMCMModuleBase() {
        super();
    }

    /**
     * Initialise the statistics logger, based on properties.
     * @param appInstance The app instance that was started.
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
                                  PROPERTY_GENERAL_MCM_SERVER_URL,
                                  PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_SESSION,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CLIENT_SETTING_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_REPOSITORY_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_OBJECT_SESSION,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TYPE_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_TYPE_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_IDENTIFIER,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TITLE,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_EVENT_TYPE_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_COLLECTION_ID,
                                  PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_DURATION_SESSION);

            //Read parameters
            String mcmConnectionURLString = cr.get(PROPERTY_GENERAL_MCM_SERVER_URL);
            String mcmStatisticsMethodCreateStatSession = cr
                    .get(PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_SESSION);
            String clientSettingID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CLIENT_SETTING_ID);
            String repositoryID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_REPOSITORY_ID);
            String mcmStatisticsMethodCreateStatObjectSession = cr
                    .get(PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_STAT_OBJECT_SESSION);
            String objectTypeID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TYPE_ID);
            String channelTypeID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_TYPE_ID);
            String channelIdentifier = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_CHANNEL_IDENTIFIER);
            String objectTitle = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_TITLE);
            String eventTypeID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_EVENT_TYPE_ID);
            String objectCollectionID = cr.get(PROPERTY_STATISTICS_LOGGING_MCM_VALUE_OBJECT_COLLECTION_ID);
            String mcmStatisticsMethodCreateDurationSession = cr
                    .get(PROPERTY_STATISTICS_LOGGING_MCM_STATISTICS_METHOD_CREATE_DURATION_SESSION);
            if (StreamingMCMEventLogger.getInstance() == null) {
                StreamingMCMEventLogger.createInstance(getLogger());
            }
            if (MCMPortalInterfaceStatisticsImpl.getInstance() == null) {
                MCMPortalInterfaceStatisticsImpl
                        .createInstance(getLogger(), appInstance.getVHost().getHomePath(), mcmConnectionURLString,
                                        mcmStatisticsMethodCreateStatSession, clientSettingID, repositoryID,
                                        mcmStatisticsMethodCreateStatObjectSession, objectTypeID, channelTypeID,
                                        channelIdentifier, objectTitle, eventTypeID, objectCollectionID,
                                        mcmStatisticsMethodCreateDurationSession);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize StreamingDatabaseEventLogger.", e);
        }
    }

    @Override
    public void onAppStop(IApplicationInstance appInstance) {
        getLogger().info("onAppStop: " + PLUGIN_NAME + " version " + PLUGIN_VERSION);
    }

    /**
     * Register an action listener that logs events to MCM, unless statistics are disabled in query string using
     * "statistics=off".
     * @param stream The stream created to add an action listener for.
     */
    @Override
    public void onStreamCreate(IMediaStream stream) {
        if (stream.getClient() == null) {
            return;
        }
        getLogger().info("onStreamCreate by: " + stream.getClientId());
        String queryString = String.valueOf(stream.getClient().getQueryStr());
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
                                                                                         StreamingMCMEventLogger
                                                                                                 .getInstance());
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            props.put("streamActionNotifierForStatistics", streamActionNotify);
        }
        stream.addClientListener(streamActionNotify);
    }

    /**
     * Remove the previously added action listener.
     * @param stream The stream destroyed to remove an action listener from.
     */
    @Override
    public void onStreamDestroy(IMediaStream stream) {
        if (stream.getClient() == null) {
            return;
        }
        getLogger().info("onStreamDestroy by: " + stream.getClientId());
        IMediaStreamActionNotify actionNotify = null;
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            actionNotify = (IMediaStreamActionNotify) stream.getProperties().get("streamActionNotifierForStatistics");
        }
        if (actionNotify != null) {
            stream.removeClientListener(actionNotify);
            getLogger().info("removeClientListener: " + stream.getSrc());
        }
    }

    /**
     * On HTTP connections, log the event directly, unless turned off in the query string using "statistics=off".
     * @param ihttpStreamerSession The http streamer session.
     */
    @Override
    public void onHTTPSessionCreate(IHTTPStreamerSession ihttpStreamerSession) {
        WMSLogger logger = getLogger();
        logger.info("onHTTPSessionCreate by: " + ihttpStreamerSession.getIpAddress());
        String queryString = ihttpStreamerSession.getQueryStr();
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

        String wayfAttr;
        try {
            wayfAttr = StringAndTextUtil.extractValueFromQueryStringAndKey("waifAttr", queryString);
            wayfAttr = new String(Base64.getDecoder().decode(wayfAttr));
        } catch (IllegallyFormattedQueryStringException e) {
            wayfAttr = "";
        }

        // Get session and object ID
        String sessionID;
        String objectID;
        try {
            sessionID = StringAndTextUtil.extractValueFromQueryStringAndKey("SessionID", queryString);
            objectID = StringAndTextUtil.extractValueFromQueryStringAndKey("ObjectID", queryString);
        } catch (IllegallyFormattedQueryStringException e) {
            logger.warn("Illegal query string in '" + ihttpStreamerSession.getUri() + "'. Not able to log.", e);
            return;
        }

        // Log event
        String streamName = ihttpStreamerSession.getStreamName();
        logger.debug("Event triggered [onHTTPSessionCreate]" + streamName);
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, streamName, 0,
                                                                   sessionID,
                                                                   objectID,
                                                                   ihttpStreamerSession.getPlayStart(),
                                                                   ihttpStreamerSession.getPlayDuration()
                                                                           - ihttpStreamerSession.getPlayStart(),
                                                                   StreamingStatLogEntry.Event.PLAY, wayfAttr);
        StreamingMCMEventLogger.getInstance().logEvent(logEntry);
    }

    /**
     * On HTTP connections, log the event directly, unless turned off in the query string using "statistics=off".
     * @param ihttpStreamerSession The http streamer session.
     */
    @Override
    public void onHTTPSessionDestroy(IHTTPStreamerSession ihttpStreamerSession) {
        WMSLogger logger = getLogger();
        logger.info("onHTTPSessionDestroy by: " + ihttpStreamerSession.getIpAddress());
        String queryString = String.valueOf(ihttpStreamerSession.getQueryStr());
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

        String wayfAttr;
        try {
            wayfAttr = StringAndTextUtil.extractValueFromQueryStringAndKey("waifAttr", queryString);
            wayfAttr = new String(Base64.getDecoder().decode(wayfAttr));
        } catch (IllegallyFormattedQueryStringException e) {
            wayfAttr = "";
        }

        // Get object ID
        String objectID;
        try {
            objectID = StringAndTextUtil.extractValueFromQueryStringAndKey("ObjectID", queryString);
        } catch (IllegallyFormattedQueryStringException e) {
            logger.warn("Illegal query string in '" + ihttpStreamerSession.getUri() + "'. Not able to log.", e);
            return;
        }

        // Log event
        String streamName = ihttpStreamerSession.getStreamName();
        logger.debug("Event triggered [onHTTPSessionDestroy]: " + streamName);
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, streamName, 0,
                                                                   ihttpStreamerSession.getSessionId(),
                                                                   ihttpStreamerSession.getSessionId() + objectID,
                                                                   ihttpStreamerSession.getPlayStart(),
                                                                   ihttpStreamerSession.getPlayDuration()
                                                                           - ihttpStreamerSession.getPlayStart(),
                                                                   StreamingStatLogEntry.Event.STOP, wayfAttr);
        StreamingMCMEventLogger.getInstance().logEvent(logEntry);
    }
}
