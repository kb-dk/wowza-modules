package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;

import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingEventLogger;

/**
 * This class handles logging of users actually playing the video
 */
public class StreamingStatisticsIMediaStreamActionNotify2 implements IMediaStreamActionNotify2 {

    private final StreamingEventLogger streamingEventLogger;

    public StreamingStatisticsIMediaStreamActionNotify2(StreamingEventLogger streamingEventLogger) {
        this.streamingEventLogger = streamingEventLogger;
    }

    private static String getQueryString(IMediaStream stream) {
        return stream.getClient().getQueryStr();
    }

    private static String getStreamingUrl(IMediaStream stream) {
        return stream.getClient().getUri() + '?' + stream.getClient().getQueryStr() + '/' +  stream.getExt() + ':'
                    + stream.getName();
    }

    public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen, int playReset) {
        streamingEventLogger.logUserEventPlay(getQueryString(stream),
                                              getStreamingUrl(stream));
    }

    public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
        // Do nothing
    }

    public void onPauseRaw(IMediaStream stream, boolean isPause, double location) {
        streamingEventLogger.logUserEventPause(getQueryString(stream),
                                               getStreamingUrl(stream));
    }

    public void onSeek(IMediaStream stream, double location) {
        streamingEventLogger.logUserEventSeek(getQueryString(stream),
                                              getStreamingUrl(stream));
    }

    public void onStop(IMediaStream stream) {
        streamingEventLogger.logUserEventStop(getQueryString(stream),
                                              getStreamingUrl(stream));
    }

    public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
        // Do nothing
    }

    public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
        // Do nothing
    }

    public void onPause(IMediaStream stream, boolean isPause, double location) {
        streamingEventLogger.logUserEventPause(getQueryString(stream),
                                               getStreamingUrl(stream));
    }
}
