package dk.statsbiblioteket.chaos.wowza.plugin.authentication;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.IClient;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnCall;
import com.wowza.wms.module.IModuleOnConnect;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;
import com.wowza.wms.stream.IMediaStreamActionNotify2;

import dk.statsbiblioteket.chaos.wowza.plugin.authentication.model.MCM2SessionAndFilenameValidater;

import java.io.FileNotFoundException;
import java.io.IOException;

public class WowzaSessionAuthenticationMCM2ModuleBase extends ModuleBase
             implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IModuleOnCall {

	private static String pluginName = "CHAOS Wowza plugin - Authentication";
	private static String pluginVersion = "2.1.0 MCM 2 Authentication";

	public WowzaSessionAuthenticationMCM2ModuleBase() {
		super();
	}

	public void onAppStart(IApplicationInstance appInstance) {
		getLogger().info("onAppStart: " + pluginName + " version " + pluginVersion);
		getLogger().info("onAppStart: VHost home path: " + appInstance.getVHost().getHomePath());
	}

	public void onConnect(IClient client, RequestFunction function,
			AMFDataList params) {
		getLogger().info("onConnect (client ID)   : " + client.getClientId());
		client.acceptConnection("CHAOS connection accepted.");
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	/**
	 * Hook into events related to playing the stream. This is done by
	 * implementing the IMediaStreamActionNotify2 interface with the
	 * StreamAuthenticater class.
	 */
	@SuppressWarnings("unchecked")
	public void onStreamCreate(IMediaStream stream) {
		getLogger().info("onStreamCreate by: " + stream.getClientId());
		IMediaStreamActionNotify streamActionNotify  = getStreamAuthenticater(
				stream.getClient().getAppInstance());
		WMSProperties props = stream.getProperties();
		synchronized(props) {
			props.put("streamActionNotifier", streamActionNotify);
		}
		stream.addClientListener(streamActionNotify);
	}

    public void play(IClient client, RequestFunction function, AMFDataList params) {
    	getLogger().info("Play called for client id was "+client.getClientId());
		this.invokePrevious(client, function, params);
    }

	/**
	 * Unregister event hook for the stream.
	 */
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

	@Override
	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect (client ID)   : " + client.getClientId());
	}

	public void onAppStop(IApplicationInstance appInstance) {
		getLogger().info("onAppStop: " + pluginName + " version " + pluginVersion);
	}

	@Override
	public void onCall(String handlerName, IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("onCall, unimplemented method was called: " + handlerName);
	}

	private IMediaStreamActionNotify2 getStreamAuthenticater(IApplicationInstance appInstance) {
		IMediaStreamActionNotify2 streamAuthenticater;
		try {
			streamAuthenticater = new StreamAuthenticater(getLogger(), 
					new MCM2SessionAndFilenameValidater(getLogger(), appInstance));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not initialize stream authenticater.", e); 
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize stream authenticater.", e); 
		}
		return streamAuthenticater;
	}
}
