package dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects;

import com.wowza.util.IOPerformanceCounter;
import com.wowza.wms.application.IApplication;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.client.ConnectionCounter;
import com.wowza.wms.client.ConnectionCounterSimple;
import com.wowza.wms.client.IClient;
import com.wowza.wms.client.IClientNotify;
import com.wowza.wms.dvr.DvrApplicationContext;
import com.wowza.wms.dvr.IDvrStreamManager;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerApplicationContext;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.mediacaster.IMediaCasterNotify;
import com.wowza.wms.mediacaster.IMediaCasterNotify2;
import com.wowza.wms.mediacaster.IMediaCasterValidateMediaCaster;
import com.wowza.wms.mediacaster.MediaCasterStreamMap;
import com.wowza.wms.module.IModuleNotify;
import com.wowza.wms.module.ModuleFunctions;
import com.wowza.wms.module.ModuleList;
import com.wowza.wms.rtp.model.RTPSession;
import com.wowza.wms.sharedobject.ISharedObjectNotify;
import com.wowza.wms.sharedobject.ISharedObjects;
import com.wowza.wms.stream.IMediaListProvider;
import com.wowza.wms.stream.IMediaReader;
import com.wowza.wms.stream.IMediaReaderActionNotify;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import com.wowza.wms.stream.IMediaStreamNameAliasProvider;
import com.wowza.wms.stream.IMediaStreamNotify;
import com.wowza.wms.stream.IMediaWriterActionNotify;
import com.wowza.wms.stream.MediaStreamMap;
import com.wowza.wms.stream.livedvr.IDvrStreamManagerActionNotify;
import com.wowza.wms.stream.livedvr.ILiveStreamDvrRecorder;
import com.wowza.wms.stream.livedvr.ILiveStreamDvrRecorderActionNotify;
import com.wowza.wms.stream.livedvr.ILiveStreamDvrRecorderControl;
import com.wowza.wms.stream.livepacketizer.ILiveStreamPacketizer;
import com.wowza.wms.stream.livepacketizer.ILiveStreamPacketizerActionNotify;
import com.wowza.wms.stream.livepacketizer.ILiveStreamPacketizerControl;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoder;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoderControl;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoderNotify;
import com.wowza.wms.stream.livetranscoder.LiveStreamTranscoderApplicationContext;
import com.wowza.wms.stream.publish.Publisher;
import com.wowza.wms.vhost.IVHost;
import edu.emory.mathcs.backport.java.util.concurrent.locks.WMSReadWriteLock;

import java.io.File;
import java.util.List;
import java.util.Map;

public class IApplicationInstanceMock implements IApplicationInstance {

    private String storageDir;

    public IApplicationInstanceMock(String storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public void addClientListener(IClientNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHTTPStreamerSession(IHTTPStreamerSession arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addLiveStreamPacketizerListener(ILiveStreamPacketizerActionNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMediaCasterListener(IMediaCasterNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMediaCasterListener(IMediaCasterNotify2 arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMediaReaderListener(IMediaReaderActionNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMediaStreamListener(IMediaStreamNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMediaWriterListener(IMediaWriterActionNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addModuleListener(IModuleNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPlayStreamByName(IMediaStream arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPublisher(Publisher arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addRTPIncomingDatagramPortAll() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addRTPIncomingDatagramPortRange(int arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addRTPSession(RTPSession arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSharedObjectListener(ISharedObjectNotify arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void broadcastMsg(List<IClient> arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void broadcastMsg(String arg0, Object... arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void broadcastMsg(List<IClient> arg0, String arg1, Object... arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsHTTPStreamer(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsLiveStreamPacketizer(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsDvrRecorder(String s) {
        return false;
    }

    @Override
    public String getVODTimedTextProviderList() {
        return null;
    }

    @Override
    public void setVODTimedTextProviderList(String s) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<String> getVODTimedTextProviderSet() {
        return null;
    }

    @Override
    public WMSProperties getTimedTextProperties() {
        return null;
    }

    @Override
    public String decodeStorageDir(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getAllowDomains() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IApplication getApplication() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getApplicationInstanceTouchTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getApplicationTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IClient getClient(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IClient getClientById(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getClientCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getClientCountTotal() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getClientIdleFrequency() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<IClient> getClients() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSReadWriteLock getClientsLockObj() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionCounter getConnectionCounter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionCounterSimple getConnectionCounter(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContextStr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDateStarted() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IHTTPStreamerApplicationContext getHTTPStreamerApplicationContext(String arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHTTPStreamerList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getHTTPStreamerProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getHTTPStreamerSessionCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHTTPStreamerSessionCount(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHTTPStreamerSessionCount(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHTTPStreamerSessionCount(int arg0, String arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Integer> getHTTPStreamerSessionCountsByName(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IHTTPStreamerSession> getHTTPStreamerSessions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IHTTPStreamerSession> getHTTPStreamerSessions(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IHTTPStreamerSession> getHTTPStreamerSessions(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IHTTPStreamerSession> getHTTPStreamerSessions(int arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IOPerformanceCounter getIOPerformanceCounter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IOPerformanceCounter getIOPerformanceCounter(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getLastTouchTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ILiveStreamPacketizerControl getLiveStreamPacketizerControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLiveStreamPacketizerList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getLiveStreamPacketizerProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getTranscoderProperties() {
        return null;
    }

    @Override
    public int getMaxStorageDirDepth() {
        // TODO Auto-generated method stub
        return 0;
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
    public WMSProperties getMediaCasterProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MediaCasterStreamMap getMediaCasterStreams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMediaCasterValidateMediaCaster getMediaCasterValidator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getMediaReaderProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getMediaWriterProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModuleFunctions getModFunctions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getModuleInstance(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModuleList getModuleList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPingTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPlayStreamCount(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Integer> getPlayStreamCountsByName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IMediaStream> getPlayStreamsByName(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getPublishStreamNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPublisherCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Publisher> getPublishers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRTPAVSyncMethod() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRTPIdleFrequency() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRTPMaxRTCPWaitTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getRTPPlayAuthenticationMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getRTPProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRTPPublishAuthenticationMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRTPSessionCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getRTPSessionCount(String arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Map<String, Integer> getRTPSessionCountsByName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RTPSession> getRTPSessions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RTPSession> getRTPSessions(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRTSPBindIpAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRTSPConnectionAddressType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRTSPConnectionIpAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRTSPMaximumPendingWriteBytes() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getRTSPOriginAddressType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRTSPOriginIpAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRTSPSessionTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getRepeaterOriginUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRepeaterQueryString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRsoStorageDir() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRsoStoragePath() {
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
    public ISharedObjects getSharedObjects() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ISharedObjects getSharedObjects(boolean arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStreamAudioSampleAccess() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStreamCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IMediaStreamFileMapper getStreamFileMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStreamKeyDir() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStreamKeyPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMediaStreamNameAliasProvider getStreamNameAliasProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getStreamProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStreamReadAccess() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStreamStorageDir() {
        return storageDir;
    }

    @Override
    public String getStreamStoragePath() {
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
    public MediaStreamMap getStreams() {
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
    public IVHost getVHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getValidationFrequency() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void incClientCountTotal() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAcceptConnection() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRTPIncomingDatagramPortValid(int arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValidateFMLEConnections() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void notifyLiveStreamPacketizerCreate(ILiveStreamPacketizer arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyLiveStreamPacketizerDestroy(ILiveStreamPacketizer arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyLiveStreamPacketizerInit(ILiveStreamPacketizer arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMediaReaderClose(IMediaReader arg0, IMediaStream arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMediaReaderCreate(IMediaReader arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMediaReaderExtractMetaData(IMediaReader arg0, IMediaStream arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMediaReaderInit(IMediaReader arg0, IMediaStream arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMediaReaderOpen(IMediaReader arg0, IMediaStream arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMediaWriterOnFLVAddMetadata(IMediaStream arg0, Map<String, Object> arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyMediaWriterOnWriteComplete(IMediaStream arg0, File arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void parseAllowDomains(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public String readAppInstConfig(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerPlayRTPSession(RTPSession arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeClientListener(IClientNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeHTTPStreamerSession(IHTTPStreamerSession arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeLiveStreamPacketizerListener(ILiveStreamPacketizerActionNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeMediaCasterListener(IMediaCasterNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeMediaReaderListener(IMediaReaderActionNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeMediaStreamListener(IMediaStreamNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeMediaWriterListener(IMediaWriterActionNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeModuleListener(IModuleNotify arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePlayStreamByName(IMediaStream arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePublisher(Publisher arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeRTPSession(RTPSession arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeSharedObjectListener(ISharedObjectNotify arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean resetMediaCasterStream(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean resetMediaCasterStream(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setAcceptConnection(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAllowDomains(String[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setApplicationInstanceTouchTimeout(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setApplicationTimeout(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClientIdleFrequency(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHTTPStreamerList(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLiveStreamPacketizerControl(ILiveStreamPacketizerControl arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLiveStreamPacketizerList(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaxStorageDirDepth(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaximumPendingWriteBytes(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getMaximumPendingReadBytes() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setMaximumPendingReadBytes(int i) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setMaximumSetBufferTime(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMediaCasterValidator(IMediaCasterValidateMediaCaster arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setName(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPingTimeout(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTPAVSyncMethod(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTPIdleFrequency(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTPMaxRTCPWaitTime(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTPPlayAuthenticationMethod(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTPPublishAuthenticationMethod(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTSPBindIpAddress(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTSPConnectionAddressType(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTSPConnectionIpAddress(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTSPMaximumPendingWriteBytes(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTSPOriginAddressType(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTSPOriginIpAddress(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRTSPSessionTimeout(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRepeaterOriginUrl(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRepeaterQueryString(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRsoStorageDir(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSharedObjectReadAccess(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSharedObjectWriteAccess(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamAudioSampleAccess(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamFileMapper(IMediaStreamFileMapper arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamKeyDir(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamNameAliasProvider(IMediaStreamNameAliasProvider arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamReadAccess(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamStorageDir(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamType(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamVideoSampleAccess(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStreamWriteAccess(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setValidateFMLEConnections(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addLiveStreamTranscoderListener(ILiveStreamTranscoderNotify iLiveStreamTranscoderNotify) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeLiveStreamTranscoderListener(ILiveStreamTranscoderNotify iLiveStreamTranscoderNotify) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyLiveStreamTranscoderCreate(ILiveStreamTranscoder iLiveStreamTranscoder,
                                                 IMediaStream iMediaStream) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyLiveStreamTranscoderDestroy(ILiveStreamTranscoder iLiveStreamTranscoder,
                                                  IMediaStream iMediaStream) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyLiveStreamTranscoderInit(ILiveStreamTranscoder iLiveStreamTranscoder, IMediaStream iMediaStream) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean containsLiveStreamTranscoder(String s) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getLiveStreamTranscoderList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLiveStreamTranscoderList(String s) {
        // TODO Auto-generated method stub
    }

    @Override
    public ILiveStreamTranscoderControl getLiveStreamTranscoderControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLiveStreamTranscoderControl(ILiveStreamTranscoderControl iLiveStreamTranscoderControl) {
        // TODO Auto-generated method stub
    }

    @Override
    public LiveStreamTranscoderApplicationContext getTranscoderApplicationContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WMSProperties getDvrProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DvrApplicationContext getDvrApplicationContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILiveStreamDvrRecorderControl getLiveStreamDvrRecorderControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLiveStreamDvrRecorderControl(ILiveStreamDvrRecorderControl iLiveStreamDvrRecorderControl) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getDvrRecorderList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDvrRecorderList(String s) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addDvrRecorderListener(ILiveStreamDvrRecorderActionNotify iLiveStreamDvrRecorderActionNotify) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeDvrRecorderListener(ILiveStreamDvrRecorderActionNotify iLiveStreamDvrRecorderActionNotify) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyLiveStreamDvrRecorderCreate(ILiveStreamDvrRecorder iLiveStreamDvrRecorder, String s) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyLiveStreamDvrRecorderInit(ILiveStreamDvrRecorder iLiveStreamDvrRecorder, String s) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyLiveStreamDvrRecorderDestroy(ILiveStreamDvrRecorder iLiveStreamDvrRecorder) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addDvrStreamManagerListener(IDvrStreamManagerActionNotify iDvrStreamManagerActionNotify) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeDvrStreamManagerListener(IDvrStreamManagerActionNotify iDvrStreamManagerActionNotify) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyDvrStreamManagerCreate(IDvrStreamManager iDvrStreamManager) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyDvrStreamManagerInit(IDvrStreamManager iDvrStreamManager) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyDvrStreamManagerDestroy(IDvrStreamManager iDvrStreamManager) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getMediaReaderContentType(String s) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IMediaListProvider getMediaListProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMediaListProvider(IMediaListProvider iMediaListProvider) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getMediacasterRTPRTSPRTPTransportMode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setMediacasterRTPRTSPRTPTransportMode(int i) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean[] getProtocolUsage() {
        // TODO Auto-generated method stub
        return new boolean[0];
    }

    @Override
    public void getProtocolUsage(boolean[] booleans) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isDebugAppTimeout() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDebugAppTimeout(boolean b) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setValidationFrequency(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void shutdown(boolean arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdownClient(IClient arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean startMediaCasterStream(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startMediaCasterStream(String arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void stopMediaCasterStream(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void touch() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean writeAppInstConfig(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return false;
    }

}
