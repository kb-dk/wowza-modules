package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.module.IModuleOnConnect;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import com.wowza.wms.stream.IMediaStreamNotify;

import dk.statsbiblioteket.doms.wowza.plugin.DomsUriToFileMapper;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketTool;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.ConfigReader;

import java.io.File;
import java.io.IOException;

/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg + abr
 */
public class KulturVODModule extends ModuleBase implements IModuleOnConnect, IMediaStreamNotify {

	private static String pluginName = "DOMS Wowza plugin";
	private static String pluginVersion = "1.0.2 - Streaming file from ticket"; 

    public KulturVODModule() {
        super();
    }

    /**
     * Called when Wowza is started.
     * We use this to set up the DomsUriToFileMapper. 
     *
     * @param appInstance The application running.
     */
    public void onAppStart(IApplicationInstance appInstance) throws IOException {
        String fullname = appInstance.getApplication().getName()
        					+ "/" + appInstance.getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger().info("***Entered onAppStart: " + fullname);
		getLogger().info("onAppStart: " + pluginName + " version " + pluginVersion);
		getLogger().info("onAppStart: VHost home path: " + vhostDir);
		getLogger().info("onAppStart: VHost storaga dir: " + storageDir);
		// Setup file mapper
        ConfigReader cr = new ConfigReader(
                new File(vhostDir + "/conf/kultur/" + "doms-wowza-plugin.properties"));
        String ticketCheckerLocation = cr.get("ticketCheckerLocation", "missing-ticket-checker-location-in-property-file");
        TicketTool ticketTool = new TicketTool(ticketCheckerLocation, getLogger());
        String invalidTicketVideo = vhostDir + "/" + (cr.get("ticketInvalidFile", "missing-invalid-file-in-property-file"));
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(ticketTool, invalidTicketVideo, appInstance.getStreamStorageDir());
        // Set File mapper
        appInstance.setStreamFileMapper(ticketToFileMapper);
        getLogger().info("onAppStart: StreamFileMapper: \""
                         + appInstance.getStreamFileMapper().getClass().getName() + "\".");
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
    public void onConnect(IClient client, RequestFunction function,
                          AMFDataList params) {
        getLogger().info("onConnect (client ID)     : " + client.getClientId());
        getLogger().info("onConnect (query string)  : " + client.getQueryStr());
        // Auto-accept is false in Application.xml. Therefore it is 
        // necessary to accept the connection explicitly here.
        client.acceptConnection();
    }

	@Override
	public void onConnectAccept(IClient client) {
        getLogger().info("onConnectAccept (client ID)     : " + client.getClientId());
	}

	@Override
	public void onConnectReject(IClient client) {
        getLogger().info("onConnectReject (client ID)     : " + client.getClientId());
	}

	@Override
	public void onDisconnect(IClient client) {
        getLogger().info("onDisconnect (client ID)     : " + client.getClientId());
	}

	@Override
	public void onMediaStreamCreate(IMediaStream stream) {
        getLogger().info("onMediaStreamCreate (client ID)     : " + stream.getClient().getClientId());
        getLogger().info("onMediaStreamCreate (stream name)   : " + stream.getName());
	}

	@Override
	public void onMediaStreamDestroy(IMediaStream stream) {
        getLogger().info("onMediaStreamDestroy (client ID)     : " + stream.getClient().getClientId());
        getLogger().info("onMediaStreamDestroy (stream name)   : " + stream.getName());
	}
}
