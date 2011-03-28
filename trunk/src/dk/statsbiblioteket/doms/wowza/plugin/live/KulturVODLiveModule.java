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
     * We use this to set up the DomsUriToFileMapper which will decode the
     * query string of the URL by which we were called, and on the basis of
     * this query string identify the video to be played, and authorize the
     * player against the ticket checker.
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
        /*IMediaStreamFileMapper defaultFileMapper
                = appInstance.getStreamFileMapper();*/
        ConfigReader cr = new ConfigReader(
                new File(appInstance.getVHost().getHomePath()
                         +"/conf/domslive/"
                         +"domslive-wowza-plugin.properties"));
        String ticketCheckerLocation = cr.get("ticketCheckerLocation", "http://alhena:7980/authchecker");
        TicketTool ticketTool = new TicketTool(ticketCheckerLocation);
        String invalidTicketVideo = vhostDir + "/" + (cr.get("ticketInvalidFile", "rck.flv"));
        IMediaStreamFileMapper streamFileMapper = new TicketToFileMapper(ticketTool, invalidTicketVideo); 
        	
        	/*new DomsUriToFileMapper(
        		storageDir,
                cr.get("sdf", "yyyy-MM-dd-HH-mm-ss"),
                vhostDir + "/" + cr.get("ticketInvalidFile", "rck.flv"),
                cr.get("ticketCheckerLocation", "http://alhena:7980/authchecker"),
                defaultFileMapper);*/
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
        getLogger().info("onConnect (properties)    : "
                         + client.getProperties());
        getLogger().info("onConnect (page URL)      : " + client.getPageUrl());
        getLogger().info("onConnect (protocol)      : " + client.getProtocol());
        getLogger().info("onConnect (referer)       : " + client.getReferrer());
        getLogger().info("onConnect (page URI)      : " + client.getUri());
        getLogger().info("onConnect (Message)       : "
                         + function.getMessage().toString());
        //client.rejectConnection("My Error 1", "My Error 3");
        getLogger().debug("Connect params "+params.toString());
        // Auto-accept is false in Application.xml. Therefore it is 
        // necessary to accept the connection explicitly here.
        client.acceptConnection();
    }
}