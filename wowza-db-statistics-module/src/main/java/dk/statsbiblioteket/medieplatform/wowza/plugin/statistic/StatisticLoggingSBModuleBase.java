package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;

import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.db.StreamingDatabaseEventLogger;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class StatisticLoggingSBModuleBase extends ModuleBase implements IModuleOnApp, IModuleOnStream {

    private static final String PLUGIN_NAME = "CHAOS Wowza plugin - Statistics SB";
       private static final String PLUGIN_VERSION = "${project.version}";

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    private static final String PROPERTY_STATISTICS_LOGGING_JDBC_DRIVER = "StatisticsLoggingJDBCDriver";
    private static final String PROPERTY_STATISTICS_LOGGING_DB_CONNECTION_URL = "StatisticsLoggingDBConnectionURL";
    private static final String PROPERTY_STATISTICS_LOGGING_DBUSER = "StatisticsLoggingDBUser";
    private static final String PROPERTY_STATISTICS_LOGGING_DB_PASSWORD = "StatisticsLoggingDBPassword";

    public StatisticLoggingSBModuleBase() {
        super();
    }

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
                                  PROPERTY_STATISTICS_LOGGING_JDBC_DRIVER, PROPERTY_STATISTICS_LOGGING_DB_CONNECTION_URL,
                                  PROPERTY_STATISTICS_LOGGING_DBUSER, PROPERTY_STATISTICS_LOGGING_DB_PASSWORD);

            //Read parameters
            String jdbcDriverString = cr.get(PROPERTY_STATISTICS_LOGGING_JDBC_DRIVER);
            String dbConnectionURLString = cr.get(PROPERTY_STATISTICS_LOGGING_DB_CONNECTION_URL);
            String dbUser = cr.get(PROPERTY_STATISTICS_LOGGING_DBUSER);
            String dbPassword = cr.get(PROPERTY_STATISTICS_LOGGING_DB_PASSWORD);

            //Initialize event logger
            if (StreamingDatabaseEventLogger.getInstance() == null) {
                StreamingDatabaseEventLogger
                        .createInstance(getLogger(), jdbcDriverString, dbConnectionURLString, dbUser, dbPassword);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize StreamingDatabaseEventLogger.", e);
        }
    }

    @Override
    public void onAppStop(IApplicationInstance appInstance) {
        getLogger().info("onAppStop: " + PLUGIN_NAME + " version " + PLUGIN_VERSION);
    }

    @Override
    public void onStreamCreate(IMediaStream stream) {
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
                                                                                         StreamingDatabaseEventLogger
                                                                                                 .getInstance());
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            props.put("streamActionNotifierForStatistics", streamActionNotify);
        }
        stream.addClientListener(streamActionNotify);
    }

    @Override
    public void onStreamDestroy(IMediaStream stream) {
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
}
