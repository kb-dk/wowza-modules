package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
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
import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketProperty;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketTool;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.ConfigReader;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.QueryUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg + abr
 */
public class KulturVODModule extends ModuleBase implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IMediaStreamNotify {

	private static String pluginName = "Kultur Wowza plugin";
	private static String pluginVersion = "2.0.8"; 

    public KulturVODModule() {
        super();
    }

    /**
     * Called when Wowza is started.
     * We use this to set up the DomsUriToFileMapper. 
     *
     * @param appInstance The application running.
     */
    @Override
    public void onAppStart(IApplicationInstance appInstance) {
        String fullname = appInstance.getApplication().getName()
        					+ "/" + appInstance.getName();
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
				cr = new ConfigReader(
				        new File(vhostDir + "/conf/kultur/" + "doms-wowza-plugin.properties"));
	        String ticketCheckerLocation = cr.get("ticketCheckerLocation", "missing-ticket-checker-location-in-property-file");
	        TicketTool ticketTool = new TicketTool(ticketCheckerLocation, getLogger());
	        String invalidTicketVideo = vhostDir + "/" + (cr.get("ticketInvalidFile", "missing-invalid-file-in-property-file"));
	        WebResource besRestApi = Client.create().resource(cr.get("broadcastExtractionServiceRestApi", "missing-bes-service-location-in-property-file"));
	        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketTool, invalidTicketVideo, appInstance.getStreamStorageDir(), besRestApi);
	        // Set File mapper
	        appInstance.setStreamFileMapper(ticketToFileMapper);
	        // Setup streaming statistics logger
	        String statLogFileHomeDir = vhostDir + "/" + cr.get("streamingStatisticsLogFolder", "missing-streamingStatisticsLogFolder-in-kultur");
	        StreamingEventLogger.createInstance(ticketTool, getLogger(), statLogFileHomeDir);
	        getLogger().info("onAppStart: StreamFileMapper: \""
	                         + appInstance.getStreamFileMapper().getClass().getName() + "\".");
		} catch (IOException e) {
			getLogger().error("An IO error occured.", e);
			throw new RuntimeException("An IO error occured.", e);
		}
    }
    
    @Override
	public void onAppStop(IApplicationInstance appInstance) {
    	// Do nothing.
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
	@SuppressWarnings("unchecked")
	public void onStreamCreate(IMediaStream stream) {
		getLogger().info("onStreamCreate by: " + stream.getClientId());
		IMediaStreamActionNotify streamActionNotify  = new KulturVODIMediaStreamActionNotify2();
		WMSProperties props = stream.getProperties();
		synchronized(props) {
			props.put("streamActionNotifier", streamActionNotify);
		}
		stream.addClientListener(streamActionNotify);
		getLogger().info("onStreamCreate: Ready to log Streaming event for stream: " + stream);
		StreamingEventLogger.getInstance().logUserEventStreamingStarted(stream);
	}

	@Override
	public void onStreamDestroy(IMediaStream stream) {
		getLogger().info("onStreamDestroy by: " + stream.getClientId());
		IMediaStreamActionNotify actionNotify = null;
		WMSProperties props = stream.getProperties();
		synchronized(props) {
			actionNotify = (IMediaStreamActionNotify)stream.getProperties().get("streamActionNotifier");
		}
		if (actionNotify != null) {
			stream.removeClientListener(actionNotify);
			getLogger().info("removeClientListener: "+stream.getSrc());
		}
		StreamingEventLogger.getInstance().logUserEventStreamingEnded(stream);
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
