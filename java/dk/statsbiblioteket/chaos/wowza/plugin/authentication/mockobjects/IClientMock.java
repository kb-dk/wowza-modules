package dk.statsbiblioteket.chaos.wowza.plugin.authentication.mockobjects;

import java.io.File;
import java.util.List;

import com.wowza.util.ElapsedTimer;
import com.wowza.util.IOPerformanceCounter;
import com.wowza.wms.amf.AMFData;
import com.wowza.wms.amf.AMFDataObj;
import com.wowza.wms.amf.AMFObj;
import com.wowza.wms.application.IApplication;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.ClientWriteListener;
import com.wowza.wms.client.IClient;
import com.wowza.wms.module.IModuleCallResult;
import com.wowza.wms.module.IModulePingResult;
import com.wowza.wms.response.ResponseFunctions;
import com.wowza.wms.rtp.model.RTPStream;
import com.wowza.wms.stream.FastPlaySettings;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.vhost.HostPort;
import com.wowza.wms.vhost.IVHost;

public class IClientMock implements IClient {

	private String queryString;
	private boolean shutdownClient;
	private boolean hasSetShutdownClientBeenCalled;

	
	public IClientMock(String queryString) {
		super();
		this.queryString = queryString;
		this.shutdownClient = false;
		this.hasSetShutdownClientBeenCalled = false;
	}

	@Override
	public void acceptConnection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptConnection(String successStr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptConnection(AMFData successObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAcceptConnectionAttribute(String key, AMFDataObj item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAcceptConnectionAttribute(String key, String item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void call(String handlerName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void call(String handlerName, IModuleCallResult resultObj,
			Object... params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearFastPlaySettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fcSubscribe(String streamName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fcSubscribe(String streamName, String mediaCasterType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fcUnSubscribe(String streamName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fcUnSubscribeAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public IApplicationInstance getAppInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IApplication getApplication() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBufferTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getClientId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getConnectTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDateStarted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElapsedTimer getElapsedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FastPlaySettings getFastPlaySettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFlashVer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdleFrequency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getIp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastValidateTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLiveRepeaterCapabilities() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLiveStreamPacketizerList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaximumPendingWriteBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaximumSetBufferTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IOPerformanceCounter getMediaIOPerformanceCounter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getObjectEncoding() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPageUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getPingRoundTripTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPingTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List getPlayStreams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WMSProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getProtocol() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List getPublishStreams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryStr() {
		return queryString;
	}

	@Override
	public RTPStream getRTPStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReferrer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRepeaterOriginUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFObj getRespAMFAudioObj(IMediaStream stream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFObj getRespAMFDataObj(IMediaStream stream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFObj getRespAMFVideoObj(IMediaStream stream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseFunctions getRespFunctions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AMFObj getResponseAMFObj(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HostPort getServerHostPort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSharedObjectReadAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSharedObjectWriteAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStreamAudioSampleAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getStreamFile(String streamName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getStreamFile(String streamName, String streamExt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getStreamFile(String streamName, String streamExt,
			boolean doCreateFolder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStreamReadAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStreamType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStreamVideoSampleAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStreamWriteAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTimeRunning() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getTimeRunningSeconds() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IOPerformanceCounter getTotalIOPerformanceCounter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVHost getVHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientWriteListener getWriteListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAcceptConnection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEncrypted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFlashMediaLiveEncoder() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFlashVersion10() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFlashVersion90115() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFlashVersionH264Capable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLiveRepeater() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isObjectEncodingAMF0() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isObjectEncodingAMF3() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSSL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int ping(IModulePingResult pingResult) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void redirectConnection(String url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void redirectConnection(String url, String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void redirectConnection(String url, String description,
			String errorStr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void redirectConnection(String url, String description,
			AMFData errorObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rejectConnection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rejectConnection(String errorStr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rejectConnection(AMFData errorObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rejectConnection(String description, String errorStr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rejectConnection(String description, AMFData errorObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reparentClient(IVHost vhost) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcceptConnection(boolean acceptConnection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcceptConnectionDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcceptConnectionExObj(AMFDataObj acceptConnectionExObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcceptConnectionObj(AMFData acceptConnectionObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBufferTime(int bufferTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFastPlaySettings(FastPlaySettings fastPlaySettings) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFlashVer(String flashVer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIdleFrequency(int idleFrequency) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLastValidateTime(long lastValidateTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLiveRepeaterCapabilities(int liveRepeaterCapabilities) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLiveStreamPacketizerList(String liveStreamPacketizerList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setObjectEncoding(int objectEncoding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRepeaterOriginUrl(String repeaterOriginUrl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSharedObjectReadAccess(String sharedObjectReadAccess) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSharedObjectWriteAccess(String sharedObjectWriteAccess) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShutdownClient(boolean shutdownClient) {
		this.hasSetShutdownClientBeenCalled = true;
		this.shutdownClient = shutdownClient;
	}

	/* 
	 * Mock object inspection
	 */
	public boolean getShutdownClient() {
		return shutdownClient;
	}

	/* 
	 * Mock object inspection
	 */
	public boolean getHasSetShutdownClientBeenCalled() {
		return hasSetShutdownClientBeenCalled;
	}

	@Override
	public void setStreamAudioSampleAccess(String audioSampleAccess) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStreamReadAccess(String streamReadAccess) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStreamType(String streamType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStreamVideoSampleAccess(String videoSampleAccess) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStreamWriteAccess(String streamWriteAccess) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdownClient() {
		// TODO Auto-generated method stub

	}

	@Override
	public int testFlashVersion(int[] version) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void touch() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValidateFMLEConnections() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setValidateFMLEConnections(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}
