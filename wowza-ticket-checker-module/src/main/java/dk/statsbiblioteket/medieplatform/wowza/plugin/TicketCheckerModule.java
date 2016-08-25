package dk.statsbiblioteket.medieplatform.wowza.plugin;

import java.io.File;
import java.io.IOException;
import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnConnect;
import com.wowza.wms.module.IModuleOnHTTPSession;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;
import com.wowza.wms.stream.IMediaStreamNotify;

import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketTool;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;

/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg + abr + kfc
 */
public class TicketCheckerModule extends ModuleBase
        implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IMediaStreamNotify, IModuleOnHTTPSession {

    private static final String PLUGIN_NAME = "Wowza Ticket Checker Plugin";
    private static final String PLUGIN_VERSION = TicketCheckerModule.class.getPackage().getImplementationVersion();
    private static final String STREAM_ACTION_NOTIFIER = "streamActionNotifier";
    private TicketChecker ticketChecker;
    private StreamAuthenticator streamAuthenticator;

    public TicketCheckerModule() {
        super();
    }

    /**
     * Called when Wowza is started.
     * We use this to intialise the ticket checker.
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
            String presentationType = cr.get("presentationType", "Stream");

            ticketChecker = new TicketChecker(presentationType, ticketTool);

            // Initialise stream authenticator
            streamAuthenticator = new StreamAuthenticator(ticketChecker);
        } catch (IOException e) {
            getLogger().error("An IO error occured.", e);
            throw new RuntimeException("An IO error occured.", e);
        }
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
        // Auto-accept is false in Application.xml. Therefore it is
        // necessary to accept the connection explicitly here.
        client.acceptConnection();
    }


    /** Check ticket to see if streaming is allowed. Otherwise report failure.
     * Also add a listener to make sure we recheck the ticket once we have the stream name.
     *
     * @param stream The stream being created.
     * */
    @Override
    public void onStreamCreate(IMediaStream stream) {
        if (stream.getClient() == null) {
            //Not an RTMP stream. Authentication done on HTTP connect.
            return;
        }
        if (!ticketChecker.checkTicket(stream, stream.getClient())) {
            sendClientOnStatusError(stream.getClient(), "NetConnection.Connect.Rejected", "Streaming not allowed");
            sendStreamOnStatusError(stream, "NetStream.Play.Failed", "Streaming not allowed");
            stream.getClient().setShutdownClient(true);
            stream.getClient().shutdownClient();
            return;
        }
        WMSProperties props = stream.getProperties();
        synchronized(props) {
            props.put(STREAM_ACTION_NOTIFIER, streamAuthenticator);
        }
        stream.addClientListener(streamAuthenticator);
    }

    /**
     * Remove the listener once we are done streaming. Note that we can't be sure the stream authenticator in the given
     * stream is the same stream authenticator that is stored in this objects, since applications are loaded and
     * unloaded while the stream is active.
     *
     * @param stream The stream being destroyed.
     */
    @Override
    public void onStreamDestroy(IMediaStream stream) {
        WMSProperties props = stream.getProperties();
        IMediaStreamActionNotify thisStreamAuthenticator;
        synchronized(props) {
            thisStreamAuthenticator = (IMediaStreamActionNotify) props.get(STREAM_ACTION_NOTIFIER);
        }
        if (thisStreamAuthenticator != null) {
            stream.removeClientListener(thisStreamAuthenticator);
            props.remove(STREAM_ACTION_NOTIFIER);
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

    public void onHTTPSessionCreate(IHTTPStreamerSession httpSession) {
        if (!ticketChecker.checkTicket(httpSession)) {
            httpSession.rejectSession();
            getLogger().warn("User not allowed to stream: " + httpSession.getUri());
        }
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onHTTPSessionDestroy(IHTTPStreamerSession ihttpStreamerSession) {
        // Do nothing
    }
}
