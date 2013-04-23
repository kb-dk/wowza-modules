package dk.statsbiblioteket.chaos.wowza.plugin.authentication.mockobjects;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import com.wowza.util.ElapsedTimer;
import com.wowza.util.IOPerformanceCounter;
import com.wowza.wms.amf.AMFObj;
import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.netconnection.INetConnection;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.rtp.model.RTPStream;
import com.wowza.wms.stream.FastPlaySettings;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify;
import com.wowza.wms.stream.IMediaStreamActionNotify2;
import com.wowza.wms.stream.IMediaStreamCallback;
import com.wowza.wms.stream.IMediaStreamMetaDataProvider;
import com.wowza.wms.stream.IMediaStreamPlay;
import com.wowza.wms.stream.MediaStreamMap;
import com.wowza.wms.stream.livepacketizer.ILiveStreamPacketizer;

public class IMediaStreamMock implements IMediaStream {

	private String name;
	private IClient iClient;
	
	public IMediaStreamMock(String name, IClient iClient) {
		super();
		this.name = name;
		this.iClient = iClient;
	}

	@Override
	public void addAudioCodecConfigPacket(long timecode, AMFPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAudioData(byte[] data, int offset, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addClientListener(IMediaStreamActionNotify actionListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addClientListener(IMediaStreamActionNotify2 actionListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDataData(byte[] data, int offset, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVideoCodecConfigPacket(long timecode, AMFPacket packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVideoData(byte[] data, int offset, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearFastPlaySettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearLoggingValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean[] getAccess(IClient client, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFPacket getAudioCodecConfigPacket(long timecode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAudioMissing() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAudioSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getAudioTC() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBufferTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] getBurstStartStop(boolean isStart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCacheName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IClient getClient() {
		return this.iClient;
	}

	@Override
	public int getClientId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContextStr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDataMissing() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDataSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getDataTC() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDataType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ElapsedTimer getElapsedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FastPlaySettings getFastPlaySettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHTTPStreamerSession getHTTPStreamerSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHeaderSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AMFPacket getLastKeyFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFPacket getLastPacket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLiveStreamPacketizer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILiveStreamPacketizer getLiveStreamPacketizer(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLiveStreamPacketizerList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLiveStreamRepeater() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getMaxTimecode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IOPerformanceCounter getMediaIOPerformance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMediaStreamMetaDataProvider getMetaDataProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public INetConnection getNetConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AMFPacket> getPlayPackets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMediaStreamPlay getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WMSProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPublishAudioCodecId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPublishVideoCodecId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getQueryStr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RTPStream getRTPStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getReceiveVideoFPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AMFObj getRespAMFAudioObj() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFObj getRespAMFDataObj() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFObj getRespAMFVideoObj() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSrc() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public File getStreamFileForRead() {
		return new File(name);
	}

	@Override
	public File getStreamFileForRead(String name, String ext, String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getStreamFileForWrite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getStreamFileForWrite(String name, String ext, String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStreamType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaStreamMap getStreams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueStreamIdStr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFPacket getVideoCodecConfigPacket(long timecode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVideoMissing() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getVideoSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getVideoTC() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void handleCallback(RequestFunction function) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean idle() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long incrementMediaInBytes(long increment) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long incrementMediaLossBytes(long bytes, long count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long incrementMediaOutBytes(long bytes, long count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void init(MediaStreamMap parent, int src, WMSProperties properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initLiveStreamRepeating(String liveStreamPacketizer,
			String liveStreamRepeater) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAppend() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClustered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPlay() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPublishStreamReady(boolean checkAudio, boolean checkVideo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReceiveAudio() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReceiveVideo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRecord() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSendPlayStopLogEvent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSendPublishStopLogEvent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSendRecordStopLogEvent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double length() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void notifyActionOnMetaData(AMFPacket metaDataPacket) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyActionPause(boolean isPause, long location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyActionPauseRaw(boolean isPause, long location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyActionPlay(String streamName, double playStart,
			double playLen, int playReset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyActionPublish(String streamName, boolean isRecord,
			boolean isAppend) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyActionSeek(double location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyActionStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyActionUnPublish(String streamName, boolean isRecord,
			boolean isAppend) {
		// TODO Auto-generated method stub

	}

	@Override
	public void packetComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void publish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerCallback(String handlerName,
			IMediaStreamCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerOnPlayStatus(IMediaStreamCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerOnStatus(IMediaStreamCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeClientListener(IMediaStreamActionNotify actionListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeClientListener(IMediaStreamActionNotify2 actionListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(String handlerName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(String handlerName, Object... params) {
		// TODO Auto-generated method stub

	}

	@Override
	public int sendControlBytes(int controlType, OutputStream out) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void sendDirect(String handlerName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendDirect(String handlerName, Object... params) {
		// TODO Auto-generated method stub

	}

	@Override
	public int sendLivePlaySeek(OutputStream out, String name, long timecode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendLivePlayStart(OutputStream out, String name, long timecode,
			long timecodeOffset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendLivePlaySwitch(OutputStream out, String name, long timecode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPauseNotify(long timecode, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPauseNotify(OutputStream out, long timecode, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayReset(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayReset(OutputStream out, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlaySeek(long location, long seekLocation, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlaySeek(OutputStream out, long location, long seekLocation,
			String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlaySeek(OutputStream out, long location, long seekLocation,
			String name, List<Integer> seekTypes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStart(String name, long timecode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStart(OutputStream out, String name, boolean isSwitch,
			long timecode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStart(OutputStream out, String name, boolean isSwitch,
			long timecode, List<Integer> seekTypes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStart(OutputStream out, String name, boolean isSwitch,
			boolean isLive, long timecode, List<Integer> seekTypes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStatus(long timecode, int statusType, double duration,
			double bytesSent) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStatus(OutputStream out, long timecode, int statusType,
			double duration, double bytesSent) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStop(long location, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlayStop(OutputStream out, long location, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlaySwitch(String name, long timecode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendPlaySwitch(OutputStream out, String name, boolean isSwitch,
			long timecode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendStreamNotFound(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendStreamNotFound(OutputStream out, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendUnpauseNotify(long location, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendUnpauseNotify(OutputStream out, long location, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendUnpauseNotify(OutputStream out, long location, String name,
			List<Integer> seekTypes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sendVODPlaySwitch(OutputStream out, String name, long timecode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAppend(boolean isAppend) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAudioSize(int audioSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAudioTC(long audioTC) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAudioTC(long audioTC, boolean isAbsolute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBufferTime(int bufferTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setClient(IClient client) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setClustered(boolean isClustered) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDataSize(int dataSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDataTC(long dataTC) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDataTC(long dataTC, boolean isAbsolute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDataType(int dataType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExt(String ext) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFastPlaySettings(FastPlaySettings fastPlaySettings) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHTTPStreamerSession(IHTTPStreamerSession httpStreamerSession) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeaderSize(int headerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIsPlaying(boolean isPlaying) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLiveStreamPacketizer(String liveStreamPacketizer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLiveStreamPacketizerList(String liveStreamPacketizerList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLiveStreamRepeater(String liveStreamRepeater) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMetaDataProvider(
			IMediaStreamMetaDataProvider metaDataProvider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String name, String ext) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String name, String oldName, String ext,
			String queryStr, double playStart, double playLen,
			int playTransition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetConnection(INetConnection netConnection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOpen(boolean isOpen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlay(boolean isPlay) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlayer(IMediaStreamPlay player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPublishAudioCodecId(int publishAudioCodecId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPublishVideoCodecId(int publishVideoCodecId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setQueryStr(String queryStr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRTPStream(RTPStream rtpStream) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReceiveAudio(boolean receiveAudio) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReceiveVideo(boolean receiveVideo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReceiveVideoFPS(int receiveVideoFPS) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRecord(boolean isRecord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSendPlayStopLogEvent(boolean sendPlayStopLogEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSendPublishStopLogEvent(boolean sendPlayStopLogEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSendRecordStopLogEvent(boolean sendPlayStopLogEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSrc(int src) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStreamType(String streamType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVideoSize(int videoSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVideoTC(long videoTC) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVideoTC(long videoTC, boolean isAbsolute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startAudioPacket() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDataPacket() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startPublishing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startVideoPacket() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopName(String name, String oldName, String ext,
			String queryStr, double playStart, double playLen,
			int playTransition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopPublishing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchName(String name, String oldName, String ext,
			String queryStr, double playStart, double playLen,
			int playTransition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trim() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterCallback(String handlerName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterOnPlayStatus(IMediaStreamCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterOnStatus(IMediaStreamCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLoggingDuration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLoggingValues() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMediaCasterPlay() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMergeOnMetadata() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setMediaCasterPlay(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMergeOnMetadata(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}
