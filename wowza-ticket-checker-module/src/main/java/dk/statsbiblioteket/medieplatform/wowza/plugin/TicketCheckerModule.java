package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnConnect;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamNotify;

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
public class TicketCheckerModule extends ModuleBase
        implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IMediaStreamNotify {

    private static final String PLUGIN_NAME = "Wowza Ticket Checker Plugin";
    private static final String PLUGIN_VERSION = "${project.version}";
    private TicketChecker ticketChecker;

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


    /** Check ticket to see if streaming is allowed. Otherwise report failure. */
    @Override
    public void onStreamCreate(IMediaStream stream) {
        if (!ticketChecker.checkTicket(stream)) {
            sendClientOnStatusError(stream.getClient(), "NetConnection.Connect.Rejected", "Streaming not allowed");
            sendStreamOnStatusError(stream, "NetStream.Play.Failed", "Streaming not allowed");
            stream.getClient().setShutdownClient(true);
            stream.getClient().shutdownClient();
        }
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onStreamDestroy(IMediaStream stream) {
        // Do nothing.
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
