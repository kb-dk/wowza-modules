package dk.statsbiblioteket.doms.wowza.plugin.kultur;

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
import com.wowza.wms.stream.IMediaStreamFileMapper;
import com.wowza.wms.stream.IMediaStreamNotify;

import dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics.StreamingEventLogger;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketTool;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.ConfigReader;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.DirectoryBasedContentResolver;

import java.io.File;
import java.io.IOException;

/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg + abr + kfc
 */
public class KulturVODModule extends ModuleBase
        implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IMediaStreamNotify {

    private static String pluginName = "Kultur Wowza plugin";
    private static String pluginVersion = "2.0.9";

    public KulturVODModule() {
        super();
    }

    /**
     * Called when Wowza is started.
     * We use this to set up the TicketToFileMapper.
     *
     * @param appInstance The application running.
     */
    @Override
    public void onAppStart(IApplicationInstance appInstance) {
        String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger().info("***Entered onAppStart: " + fullname);
        getLogger().info("onAppStart: " + pluginName + " version " + pluginVersion);
        getLogger().info("onAppStart: VHost home path: " + vhostDir);
        getLogger().info("onAppStart: VHost storaga dir: " + storageDir);
        try {
            // Setup file mapper
            IMediaStreamFileMapper defaultMapper = appInstance.getStreamFileMapper();
            ConfigReader cr;
            cr = new ConfigReader(new File(vhostDir + "/conf/kultur/" + "doms-wowza-plugin.properties"));
            String ticketCheckerLocation = cr
                    .get("ticketCheckerLocation", "missing-ticket-checker-location-in-property-file");
            TicketTool ticketTool = new TicketTool(ticketCheckerLocation, getLogger());
            String invalidTicketVideo = vhostDir + "/" + (cr
                    .get("ticketInvalidFile", "missing-invalid-file-in-property-file"));
            File baseDirectory = new File(appInstance.getStreamStorageDir()).getAbsoluteFile();
            int characterDirs = Integer.parseInt(cr.get("characterDirs", "4"));
            String filenameRegexPattern = cr
                    .get("filenameRegexPattern", "missing-filename-regex-pattern-in-property-file");
            String uriPattern = "file://" + baseDirectory + "/%s";
            ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", baseDirectory,
                                                                                characterDirs, filenameRegexPattern,
                                                                                uriPattern);
            TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketTool,
                                                                           invalidTicketVideo, contentResolver);
            // Set File mapper
            appInstance.setStreamFileMapper(ticketToFileMapper);
            // Setup streaming statistics logger
            String statLogFileHomeDir = cr
                    .get("streamingStatisticsLogFolder", "missing-streamingStatisticsLogFolder-in-kultur");
            StreamingEventLogger.createInstance(ticketTool, getLogger(), statLogFileHomeDir);
            getLogger().info("onAppStart: StreamFileMapper: \"" + appInstance.getStreamFileMapper().getClass().getName()
                                     + "\".");
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
    public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
        getLogger().info("onConnect (client ID)     : " + client.getClientId());
        getLogger().info("onConnect (query string)  : " + client.getQueryStr());
        // Auto-accept is false in Application.xml. Therefore it is 
        // necessary to accept the connection explicitly here.
        client.acceptConnection();
    }


    /**
     * Add the KulturVODIMediaStramActionNotify listener to the stream. This thing is used to track usage
     * and have nothing to do with tickets
     * @param stream
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onStreamCreate(IMediaStream stream) {
        getLogger().info("onStreamCreate by: " + stream.getClientId());
        IMediaStreamActionNotify streamActionNotify = new KulturVODIMediaStreamActionNotify2();
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            props.put("streamActionNotifier", streamActionNotify);
        }
        stream.addClientListener(streamActionNotify);
        getLogger().info("onStreamCreate: Ready to log Streaming event for stream: " + stream);
        StreamingEventLogger.getInstance().logUserEventStreamingStarted(stream);
    }

    /**
     * Disconnect the notifier when the stream is destroyed
     * @param stream
     */
    @Override
    public void onStreamDestroy(IMediaStream stream) {
        getLogger().info("onStreamDestroy by: " + stream.getClientId());
        IMediaStreamActionNotify actionNotify = null;
        WMSProperties props = stream.getProperties();
        synchronized (props) {
            actionNotify = (IMediaStreamActionNotify) stream.getProperties().get("streamActionNotifier");
        }
        if (actionNotify != null) {
            stream.removeClientListener(actionNotify);
            getLogger().info("removeClientListener: " + stream.getSrc());
        }
        StreamingEventLogger.getInstance().logUserEventStreamingEnded(stream);
    }





        /*Mainly here to remember that we can hook this method*/
    @Override
    public void onAppStop(IApplicationInstance appInstance) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnectAccept(IClient client) {
        getLogger().info("onConnectAccept (client ID)     : " + client.getClientId());
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnectReject(IClient client) {
        getLogger().info("onConnectReject (client ID)     : " + client.getClientId());
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onDisconnect(IClient client) {
        getLogger().info("onDisconnect (client ID)     : " + client.getClientId());
    }


    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onMediaStreamCreate(IMediaStream stream) {
        getLogger().info("onMediaStreamCreate (client ID)     : " + stream.getClient().getClientId());
        getLogger().info("onMediaStreamCreate (stream name)   : " + stream.getName());
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onMediaStreamDestroy(IMediaStream stream) {
        getLogger().info("onMediaStreamDestroy (client ID)     : " + stream.getClient().getClientId());
        getLogger().info("onMediaStreamDestroy (stream name)   : " + stream.getName());
    }

}
