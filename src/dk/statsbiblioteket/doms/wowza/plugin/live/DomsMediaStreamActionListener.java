package dk.statsbiblioteket.doms.wowza.plugin.live;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Nov 26, 2010
 * Time: 3:07:58 PM
 * To change this template use File | Settings | File Templates.
 */
class DomsMediaStreamActionListener implements IMediaStreamActionNotify2 {


    public void onPlay(IMediaStream stream, String streamName,
                       double playStart, double playLen, int playReset) {
        DomsLiveStreaming.getLogger().info("onPlay");
        IClient client = stream.getClient();
/*
        if (client != null){

            String querystring = stream.getClient().getQueryStr();
            String port = Utils.extractPortID(querystring);
            if (!(port).equals(streamName)){//
                stream.shutdown();
            }

        }
*/

        getLogger().info("onStreamPlay (name)     : " + stream.getName());
        getLogger().info("onStreamPlay (name)     : " + streamName);
        getLogger().info("onStreamPlay (ext)  : " + stream.getExt());
        getLogger().info("onStreamPlay (cachename)     : " + stream.getCacheName());
        getLogger().info("onStreamPlay (contextstr)     : " + stream.getContextStr());
        getLogger().info("onStreamPlay (querystr)     : " + stream.getQueryStr());
        getLogger().info("onStreamPlay (streamtype)     : " + stream.getStreamType());
    }

    private void logClient(String s, IClient client) {
        if (client != null){
            getLogger().info(s+ " (client query string)     : " + client.getQueryStr());
            getLogger().info(s+ " (client page url)     : " + client.getPageUrl());
            getLogger().info(s+" (client uri)  : " + client.getUri());
        }
    }

    public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
        DomsLiveStreaming.getLogger().info("onMetaData By: " + stream.getClientId());
    }

    public void onPauseRaw(IMediaStream stream, boolean isPause,
                           double location) {
        DomsLiveStreaming.getLogger().info("onPauseRaw By: " + stream.getClientId());
    }

    public void onSeek(IMediaStream stream, double location) {
        DomsLiveStreaming.getLogger().info("onSeek");
    }

    public void onStop(IMediaStream stream) {
        DomsLiveStreaming.getLogger().info("onStop By: " + stream.getClientId());
        stream.shutdown();
    }

    public void onUnPublish(IMediaStream stream, String streamName,
                            boolean isRecord, boolean isAppend) {
        DomsLiveStreaming.getLogger().info("onUnPublish");
    }

    public  void onPublish(IMediaStream stream, String streamName,
                           boolean isRecord, boolean isAppend) {
        DomsLiveStreaming.getLogger().info("onPublish");
        if (stream.getClient() != null){
            logClient("onStreamPublish ",stream.getClient());
        }
        getLogger().info("onStreamPublish (name)     : " + stream.getName());
        getLogger().info("onStreamPublish (name)     : " + streamName);
        getLogger().info("onStreamPublish (ext)  : " + stream.getExt());
        getLogger().info("onStreamPublish (cachename)     : " + stream.getCacheName());
        getLogger().info("onStreamPublish (contextstr)     : " + stream.getContextStr());
        getLogger().info("onStreamPublish (querystr)     : " + stream.getQueryStr());
        getLogger().info("onStreamPublish (streamtype)     : " + stream.getStreamType());


    }

    public void onPause(IMediaStream stream, boolean isPause,
                        double location) {
        DomsLiveStreaming.getLogger().info("onPause");
    }
    protected static WMSLogger getLogger()
    {
        return WMSLoggerFactory.getLogger(DomsMediaStreamListener.class);
    }

}
