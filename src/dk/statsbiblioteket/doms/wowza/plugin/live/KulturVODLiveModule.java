package dk.statsbiblioteket.doms.wowza.plugin.live;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import dk.statsbiblioteket.doms.wowza.plugin.DomsUriToFileMapper;
import dk.statsbiblioteket.doms.wowza.plugin.kultur.TicketToFileMapper;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketTool;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.ConfigReader;

import java.io.IOException;
import java.io.File;


/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg
 */
public class KulturVODLiveModule extends ModuleBase {

	private static String pluginName = "Kultur Live Wowza plugin";
	private static String pluginVersion = "1.0.2 - Streaming file from ticket"; 

    public KulturVODLiveModule() {
        super();
    }

    /**
     * Called when Wowza is started.
     *
     * @param appInstance The application running.
     */
    public void onAppStart(final IApplicationInstance appInstance)
            throws IOException {
        String fullname = appInstance.getApplication().getName() + "/"
                          + appInstance.getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger().info("***Entered onAppStart: " + fullname);
		getLogger().info("onAppStart: " + pluginName + " version " + pluginVersion);
		getLogger().info("onAppStart: VHost home path: " + vhostDir);
		getLogger().info("onAppStart: VHost storaga dir: " + storageDir);
		IMediaStreamFileMapper defaultMapper = appInstance.getStreamFileMapper();
        ConfigReader cr = new ConfigReader(
                new File(appInstance.getVHost().getHomePath()
                         +"/conf/kultur_live/"
                         +"domslive-wowza-plugin.properties"));
        // Note: Media content root folder is different from vhost storage dir from Application.xml
        String mediaContentRootFolder = vhostDir + "/" + cr.get("mediaContentRootFolder", "null");
        String ticketCheckerLocation = cr.get("ticketCheckerLocation", "missing-ticket-checker-location-in-property-file");
        TicketTool ticketTool = new TicketTool(ticketCheckerLocation, getLogger());
        String invalidTicketVideo = vhostDir + "/" + (cr.get("ticketInvalidFile", "missing-invalid-file-in-property-file"));
        IMediaStreamFileMapper streamFileMapper = new TicketToFileMapper(defaultMapper, ticketTool, invalidTicketVideo, mediaContentRootFolder); 
        	
        appInstance.addMediaStreamListener(
                new KulturVODLiveMediaStreamListener(
                        appInstance,
                        streamFileMapper,
                        cr));
        getLogger().info("onAppStart: StreamFileMapper: \""
                         + DomsUriToFileMapper.class.getName() + "\".");
    }

    public void onAppStop(IApplicationInstance appInstance) {
        String fullname = appInstance.getApplication().getName() + "/"
                          + appInstance.getName();
        getLogger().info("onAppStop: " + fullname);
    }


    /**
     * Called when a new video stream connection is started.
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
}