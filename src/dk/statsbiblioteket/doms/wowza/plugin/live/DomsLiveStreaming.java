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
import dk.statsbiblioteket.doms.wowza.plugin.utilities.ConfigReader;

import java.io.IOException;
import java.io.File;

/* Other Events that can be included:

	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
	}

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
		
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
	}

	public void onStreamCreate(IMediaStream stream) {
		getLogger().info("onStreamCreate by: " + stream.getClientId());
		String filepath = stream.getStreamFileForRead().getAbsolutePath();
		String filename = stream.getStreamFileForRead().getName();
		getLogger().info("Filepath (onPlay)    : " + filepath);
		getLogger().info("Filename (onPlay)    : " + filename);
		IMediaStreamActionNotify actionNotify  = streamAuthenticater;
		WMSProperties props = stream.getProperties();
		synchronized(props) {
			props.put("streamActionNotifier", actionNotify);
		}
		stream.addClientListener(actionNotify);
	}

	public void onStreamDestroy(IMediaStream stream) {
		getLogger().info("onStreamDestroy by: " + stream.getClientId());
		IMediaStreamActionNotify actionNotify = null;
		WMSProperties props = stream.getProperties();
		synchronized(props) {
			actionNotify = (IMediaStreamActionNotify)stream.getProperties().get(
			"streamActionNotifier");
		}
		if (actionNotify != null) {
			stream.removeClientListener(actionNotify);
			getLogger().info("removeClientListener: "+stream.getSrc());
		}
	}

	public void onHTTPSessionCreate(IHTTPStreamerSession httpSession) {
		getLogger().info("onHTTPSessionCreate: " + httpSession.getSessionId());
	}

	public void onHTTPSessionDestroy(IHTTPStreamerSession httpSession) {
		getLogger().info("onHTTPSessionDestroy: " + httpSession.getSessionId());
	}
*/

/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg
 */
public class DomsLiveStreaming extends ModuleBase {


    public DomsLiveStreaming() {
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
        getLogger().info("***Entered onAppStart: " + fullname);


        IMediaStreamFileMapper defaultFileMapper
                = appInstance.getStreamFileMapper();


        ConfigReader cr = new ConfigReader(
                new File(appInstance.getVHost().getHomePath()
                         +"/conf/domslive/"
                         +"domslive-wowza-plugin.properties"));


        appInstance.addMediaStreamListener(
                new DomsMediaStreamListener(
                        appInstance,
                        new DomsUriToFileMapper(
                                appInstance.decodeStorageDir(
                                        cr.get("storageDir",
                                               appInstance.getStreamStorageDir())),
                                cr.get("sdf",
                                       "yyyy-MM-dd-HH-mm-ss"),
                                cr.get("ticketInvalidFile",
                                       "rck.flv"),
                                cr.get("ticketCheckerLocation",
                                       "http://alhena:7980/authchecker"),
                                defaultFileMapper),
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

    }

    protected static WMSLogger getLogger()
    {
        return WMSLoggerFactory.getLogger(DomsLiveStreaming.class);
    }

}