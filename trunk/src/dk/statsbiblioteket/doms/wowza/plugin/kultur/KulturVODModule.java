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
        String fullname = appInstance.getApplication().getName() + "/"
                          + appInstance.getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger().info("***Entered onAppStart: " + fullname);
		getLogger().info("onAppStart: " + pluginName + " version " + pluginVersion);
		getLogger().info("onAppStart: VHost home path: " + vhostDir);
		getLogger().info("onAppStart: VHost storaga dir: " + storageDir);

		// Create File mapper
        IMediaStreamFileMapper defaultFileMapper = appInstance.getStreamFileMapper();
        ConfigReader cr = new ConfigReader(
                new File(vhostDir + "/conf/kultur/" + "doms-wowza-plugin.properties"));
        String ticketCheckerLocation = cr.get("ticketCheckerLocation", "http://alhena:7980/authchecker");
        TicketTool ticketTool = new TicketTool(ticketCheckerLocation);
        String invalidTicketVideo = vhostDir + "/" + (cr.get("ticketInvalidFile", "rck.flv"));
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(ticketTool, invalidTicketVideo);
        /*DomsUriToFileMapper domsUriToFileMapper = new DomsUriToFileMapper(
        		storageDir,
                cr.get("sdf", "yyyy-MM-dd-HH-mm-ss"),
                appInstance.decodeStorageDir(
                        cr.get("ticketInvalidFile", "rck.flv")
                ),
                cr.get("ticketCheckerLocation",
                       "http://alhena:7980/authchecker"),
                defaultFileMapper);*/
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
        getLogger().info("onConnect (properties)    : "
                         + client.getProperties());
        getLogger().info("onConnect (page URL)      : " + client.getPageUrl());
        getLogger().info("onConnect (protocol)      : " + client.getProtocol());
        getLogger().info("onConnect (referer)       : " + client.getReferrer());
        getLogger().info("onConnect (page URI)      : " + client.getUri());
        getLogger().info("onConnect (Message)       : "
                         + function.getMessage().toString());
        // Auto-accept is false in Application.xml. Therefore it is 
        // necessary to accept the connection explicitly here.
        client.acceptConnection();
    }

	@Override
	public void onConnectAccept(IClient arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectReject(IClient arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnect(IClient arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaStreamCreate(IMediaStream stream) {
        getLogger().info("onMediaStreamCreate (client ID)     : " + stream.getClient().getClientId());
        getLogger().info("onMediaStreamCreate (stream name)   : " + stream.getName());
	}

	@Override
	public void onMediaStreamDestroy(IMediaStream stream) {
        getLogger().info("onMediaStreamDestroy (stream name)   : " + stream.getName());
	}
}
