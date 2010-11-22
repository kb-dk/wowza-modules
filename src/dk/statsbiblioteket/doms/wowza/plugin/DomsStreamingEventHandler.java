package dk.statsbiblioteket.doms.wowza.plugin;

import com.wowza.wms.application.*;
import com.wowza.wms.module.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.client.IClient;

import dk.statsbiblioteket.doms.wowza.plugin.model.DomsUriToFileMapper;

/**
 * This class handles events that happen during streaming. Also sets up the file
 * mapper that is needed for identifying the file to be played.
 *
 * @author heb + jrg
 */
public class DomsStreamingEventHandler extends ModuleBase {

	public DomsStreamingEventHandler() {
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
	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("***Entered onAppStart: " + fullname);

        String vhostDir = appInstance.getVHost().getHomePath();
        appInstance.addMediaStreamListener(new IMediaStreamNotify(){

            @Override
            public void onMediaStreamCreate(IMediaStream iMediaStream) {
                iMediaStream.addClientListener(new StreamListener());
            }

            @Override
            public void onMediaStreamDestroy(IMediaStream iMediaStream) {
            }
        });

		// Create File mapper
		String storageDir = appInstance.getStreamStorageDir();
		IMediaStreamFileMapper defaultFileMapper
                = appInstance.getStreamFileMapper();

		DomsUriToFileMapper domsUriToFileMapper = new DomsUriToFileMapper(
                storageDir, getLogger(), defaultFileMapper, vhostDir);

		// Set File mapper, which will be used to get name of the stream file
        // from the query string.
		appInstance.setStreamFileMapper(domsUriToFileMapper);
		getLogger().info("onAppStart: StreamFileMapper: \""
                + DomsUriToFileMapper.class.getName() + "\".");
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
        
    }

    /**
     * To pass on ticket and shard url to ticket issuer, we have to get these
     * from the query string.
     * onConnect cannot access the query string, which we would have to get from
     * the stream (which onConnect cannot access). So we have to do it in onPlay
     * instead. So far it seems onPlay is not triggered.. so for now we handle
     * authorization in the DomsUriToFileMapper class instead.
     */
    class StreamListener  implements IMediaStreamActionNotify2 {
        public void onPlay(IMediaStream stream, String streamName,
                           double playStart, double playLen, int playReset) {
            getLogger().info("***Entered onPlay(..., " + streamName
                    + "..., ..., ...)");
            getLogger().info("Stream Name: " + stream.getName());
        }

        public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
            getLogger().info("onMetaData By: " + stream.getClientId());
        }

        public void onPauseRaw(IMediaStream stream, boolean isPause,
                               double location) {
            getLogger().info("onPauseRaw By: " + stream.getClientId());
        }

        public void onSeek(IMediaStream stream, double location) {
            getLogger().info("onSeek");
        }

        public void onStop(IMediaStream stream) {
            getLogger().info("onStop By: " + stream.getClientId());
        }

        public void onUnPublish(IMediaStream stream, String streamName,
                                boolean isRecord, boolean isAppend) {
            getLogger().info("onUnPublish");
        }

        public  void onPublish(IMediaStream stream, String streamName,
                               boolean isRecord, boolean isAppend) {
            getLogger().info("onPublish");
        }

        public void onPause(IMediaStream stream, boolean isPause,
                            double location) {
            getLogger().info("onPause");
        }
    }

}

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
