package dk.statsbiblioteket.doms.wowza.plugin;

import com.wowza.wms.application.*;
import com.wowza.wms.module.*;
import com.wowza.wms.stream.*;


public class DomsStreamingEventHandler extends ModuleBase {

	public DomsStreamingEventHandler() {
		super();
	}

	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
		// Create File mapper
		String storageDir = appInstance.getStreamStorageDir();
		IMediaStreamFileMapper defaultFileMapper = appInstance.getStreamFileMapper();
		DomsUriToFileMapper domsUriToFileMapper = new DomsUriToFileMapper(storageDir, getLogger(), defaultFileMapper);
		// Set File mapper
		appInstance.setStreamFileMapper(domsUriToFileMapper);
		getLogger().info("onAppStart: StreamFileMapper: \"" + DomsUriToFileMapper.class.getName() + "\".");
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
		getLogger().info("onConnect (properties)    : " + client.getProperties());
		getLogger().info("onConnect (page URL)      : " + client.getPageUrl());
		getLogger().info("onConnect (protocol)      : " + client.getProtocol());
		getLogger().info("onConnect (referer)       : " + client.getReferrer());
		getLogger().info("onConnect (page URI)      : " + client.getUri());
		getLogger().info("onConnect (Message)       : " + function.getMessage().toString());
		
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
			actionNotify = (IMediaStreamActionNotify)stream.getProperties().get("streamActionNotifier");
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
