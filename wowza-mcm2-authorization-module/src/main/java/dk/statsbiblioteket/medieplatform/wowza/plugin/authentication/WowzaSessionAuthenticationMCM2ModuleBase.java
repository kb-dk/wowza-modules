package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnCall;
import com.wowza.wms.module.IModuleOnConnect;
import com.wowza.wms.module.IModuleOnHTTPSession;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;

import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;

import java.io.File;
import java.io.IOException;

public class WowzaSessionAuthenticationMCM2ModuleBase extends ModuleBase
             implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IModuleOnCall, IModuleOnHTTPSession {

    private static final String PLUGIN_NAME = "CHAOS Wowza plugin - MCM2 Authentication";
       private static final String PLUGIN_VERSION = "${project.version}";
    private static final String PROPERTY_MCM2_SERVER_URL_KEY = "GeneralMCM2ServerURL";
    private static final String PROPERTY_MCM2_VALIDATION_METHOD = "ValidationMCM2ValidationMethod";
    private StreamAuthenticater streamAuthenticater;

    public WowzaSessionAuthenticationMCM2ModuleBase() {
        super();
    }

    public void onAppStart(IApplicationInstance appInstance) {
        String appName = appInstance.getApplication().getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger()
                .info("***Entered onAppStart: " + appName + "\n  Plugin: " + PLUGIN_NAME + " version " + PLUGIN_VERSION
                              + "\n  VHost home path: " + vhostDir + " VHost storage dir: " + storageDir);
        try {
            //Initialise the config reader
            ConfigReader cr;
            cr = new ConfigReader(new File(vhostDir + "/conf/" + appName + "/wowza-modules.properties"),
                                  PROPERTY_MCM2_SERVER_URL_KEY,
                                  PROPERTY_MCM2_VALIDATION_METHOD);

            //Read parameters
            String validationMethodAtServer = cr.get(PROPERTY_MCM2_VALIDATION_METHOD);
            String connectionUrlString = cr.get(PROPERTY_MCM2_SERVER_URL_KEY);

            //Initialise stream authenticator
            streamAuthenticater = new StreamAuthenticater(getLogger(),
                                                          new MCM2SessionAndFilenameValidater(getLogger(), appInstance,
                                                                                              connectionUrlString, validationMethodAtServer));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize StreamingDatabaseEventLogger.", e);
        }
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
        if (stream.getClient() == null) {
            return;
        }
        getLogger().info("onStreamCreate by: " + stream.getClientId());
        IMediaStreamActionNotify streamActionNotify  = streamAuthenticater;
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
        if (stream.getClient() == null) {
            return;
        }
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
        getLogger().info("onAppStop: " + PLUGIN_NAME + " version " + PLUGIN_VERSION);
    }

    @Override
    public void onCall(String handlerName, IClient client, RequestFunction function, AMFDataList params) {
        getLogger().info("onCall, unimplemented method was called: " + handlerName);
    }

    @Override
    public void onHTTPSessionCreate(IHTTPStreamerSession ihttpStreamerSession) {
        try {
            getLogger().info("onHTTPSessionCreate by: " + ihttpStreamerSession.getIpAddress());
            boolean authenticated = streamAuthenticater
                    .checkAuthorization(ihttpStreamerSession.getQueryStr(), ihttpStreamerSession.getStreamName());
            if (!authenticated) {
                getLogger().warn("Not authorized '" + ihttpStreamerSession.getUri() + "' for: " + ihttpStreamerSession.getIpAddress());
                ihttpStreamerSession.rejectSession();
                ihttpStreamerSession.shutdown();
            }
        } catch (Exception e) {
            getLogger().warn("Error checking authorization of '" + ihttpStreamerSession.getUri() + "' for: " + ihttpStreamerSession.getIpAddress(), e);
            ihttpStreamerSession.rejectSession();
            ihttpStreamerSession.shutdown();
        }
    }

    @Override
    public void onHTTPSessionDestroy(IHTTPStreamerSession ihttpStreamerSession) {
        getLogger().info("onHTTPSessionDestroy by: " + ihttpStreamerSession.getIpAddress());
    }
}
