package dk.statsbiblioteket.medieplatform.wowza.plugin.kultur;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingEventLogger;

/**
 * This class handles logging of users actually playing the video
 */
public class KulturVODIMediaStreamActionNotify2 implements IMediaStreamActionNotify2 {

    public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen, int playReset) {
        StreamingEventLogger.getInstance().logUserEventPlay(stream);
    }

    public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
        // Do nothing
    }

    public void onPauseRaw(IMediaStream stream, boolean isPause, double location) {
        StreamingEventLogger.getInstance().logUserEventPause(stream);
    }

    public void onSeek(IMediaStream stream, double location) {
        StreamingEventLogger.getInstance().logUserEventSeek(stream);
    }

    public void onStop(IMediaStream stream) {
        StreamingEventLogger.getInstance().logUserEventStop(stream);
    }

    public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
        // Do nothing
    }

    public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
        // Do nothing
    }

    public void onPause(IMediaStream stream, boolean isPause, double location) {
        StreamingEventLogger.getInstance().logUserEventPause(stream);
    }
}
