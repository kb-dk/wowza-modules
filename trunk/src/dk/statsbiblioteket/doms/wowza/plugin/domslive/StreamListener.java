package dk.statsbiblioteket.doms.wowza.plugin.domslive;

import com.wowza.wms.stream.IMediaStreamActionNotify2;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

/**
 * Created by IntelliJ IDEA.
* User: abr
* Date: Nov 26, 2010
* Time: 3:07:58 PM
* To change this template use File | Settings | File Templates.
*/
class StreamListener implements IMediaStreamActionNotify2 {
    public void onPlay(IMediaStream stream, String streamName,
                       double playStart, double playLen, int playReset) {
        DomsStreamingEventHandler.getLogger().info("onPublish");
        getLogger().info("onStreamPlay (name)     : " + stream.getName());
        getLogger().info("onStreamPlay (name)     : " + streamName);
        getLogger().info("onStreamPlay (ext)  : " + stream.getExt());
        getLogger().info("onStreamPlay (cachename)     : " + stream.getCacheName());
        getLogger().info("onStreamPlay (contextstr)     : " + stream.getContextStr());
        getLogger().info("onStreamPlay (querystr)     : " + stream.getQueryStr());
        getLogger().info("onStreamPlay (streamtype)     : " + stream.getStreamType());
    }

    public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
        DomsStreamingEventHandler.getLogger().info("onMetaData By: " + stream.getClientId());
    }

    public void onPauseRaw(IMediaStream stream, boolean isPause,
                           double location) {
        DomsStreamingEventHandler.getLogger().info("onPauseRaw By: " + stream.getClientId());
    }

    public void onSeek(IMediaStream stream, double location) {
        DomsStreamingEventHandler.getLogger().info("onSeek");
    }

    public void onStop(IMediaStream stream) {
        DomsStreamingEventHandler.getLogger().info("onStop By: " + stream.getClientId());
        stream.shutdown();
    }

    public void onUnPublish(IMediaStream stream, String streamName,
                            boolean isRecord, boolean isAppend) {
        DomsStreamingEventHandler.getLogger().info("onUnPublish");
    }

    public  void onPublish(IMediaStream stream, String streamName,
                           boolean isRecord, boolean isAppend) {
        DomsStreamingEventHandler.getLogger().info("onPublish");
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
        DomsStreamingEventHandler.getLogger().info("onPause");
    }
    protected static WMSLogger getLogger()
    {
        return WMSLoggerFactory.getLogger(DynamicLiveStreaming.class);
    }

}
