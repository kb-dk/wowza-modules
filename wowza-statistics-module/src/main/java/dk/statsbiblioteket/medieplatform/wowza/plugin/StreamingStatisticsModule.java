package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.IClient;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnConnect;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;
import com.wowza.wms.stream.IMediaStreamNotify;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingEventLogger;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketTool;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;

import java.io.File;
import java.io.IOException;

/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg + abr + kfc
 */
public class StreamingStatisticsModule extends ModuleBase
        implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IMediaStreamNotify {

    private static final String PLUGIN_NAME = "Wowza statistics logger plugin";
    private static final String PLUGIN_VERSION = "${project.version}";
    private StreamingEventLogger streamingEventLogger;

    public StreamingStatisticsModule() {
        super();
    }

    /**
     * Called when Wowza is started.
     *
     * @param appInstance The application running.
     */
    @Override
    public void onAppStart(IApplicationInstance appInstance) {
        String appName = appInstance.getApplication().getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger().info("***Entered onAppStart: " + appName
                + "\n  Plugin: " + PLUGIN_NAME + " version " + PLUGIN_VERSION
                + "\n  VHost home path: " + vhostDir + " VHost storage dir: " + storageDir);
        try {
            //Initialise the config reader
            ConfigReader cr;
            cr = new ConfigReader(new File(vhostDir + "/conf/" + appName + "/wowza-modules.properties"));

            //Read to initialise the ticket checker
            String ticketCheckerLocation = cr
                    .get("ticketCheckerLocation", "missing-ticket-checker-location-in-property-file");
            TicketTool ticketTool = new TicketTool(ticketCheckerLocation, getLogger());

            // Setup streaming statistics logger
            String statLogFileHomeDir = cr
                    .get("streamingStatisticsLogFolder", "missing-streamingStatisticsLogFolder");
            streamingEventLogger = new StreamingEventLogger(ticketTool, getLogger(), statLogFileHomeDir);
        } catch (IOException e) {
            getLogger().error("An IO error occured.", e);
            throw new RuntimeException("An IO error occured.", e);
        }
    }

    /**
     * Called when a new video stream connection is started.
     *
     * The method accepts a connection.
     *
     * @param client
     * @param function
     * @param params
     */
    @Override
    public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
        getLogger().debug("onConnect, clientID='" + client.getClientId() + "', queryString='" + client.getQueryStr() + "'");
        // Auto-accept is false in Application.xml. Therefore it is 
        // necessary to accept the connection explicitly here.
        client.acceptConnection();
    }

    /**
     * Add the StreamingStatisticsIMediaStreamActionNotify2 listener to the stream.
     * @param stream
     */
    @Override
    public void onStreamCreate(IMediaStream stream) {
        getLogger().debug("onStreamCreate, clientID='" + stream.getClientId() + "'");
        IMediaStreamActionNotify streamActionNotify = new StreamingStatisticsIMediaStreamActionNotify2(streamingEventLogger);
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            props.put("streamActionNotifier", streamActionNotify);
        }
        stream.addClientListener(streamActionNotify);
    }

    /**
     * Disconnect the notifier when the stream is destroyed
     * @param stream
     */
    @Override
    public void onStreamDestroy(IMediaStream stream) {
        getLogger().debug("onStreamDestroy, clientID='" + stream.getClientId() + "'");
        IMediaStreamActionNotify actionNotify;
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            actionNotify = (IMediaStreamActionNotify) stream.getProperties().get("streamActionNotifier");
        }
        if (actionNotify != null) {
            stream.removeClientListener(actionNotify);
        }
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onAppStop(IApplicationInstance appInstance) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnectAccept(IClient client) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnectReject(IClient client) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onDisconnect(IClient client) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onMediaStreamCreate(IMediaStream stream) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onMediaStreamDestroy(IMediaStream stream) {
        // Do nothing.
    }

}
