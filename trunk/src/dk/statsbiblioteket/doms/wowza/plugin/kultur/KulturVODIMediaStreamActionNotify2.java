package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;

public class KulturVODIMediaStreamActionNotify2 implements IMediaStreamActionNotify2 {

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

    protected WMSLogger getLogger()
    {
        return WMSLoggerFactory.getLogger(this.getClass());
    }

}
