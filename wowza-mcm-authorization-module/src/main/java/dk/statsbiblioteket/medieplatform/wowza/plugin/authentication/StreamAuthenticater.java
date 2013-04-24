package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;

import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOutputException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.SessionAndFilenameValidaterIF;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

class StreamAuthenticater  implements IMediaStreamActionNotify2 {
    
    private SessionAndFilenameValidaterIF validater; 
    private WMSLogger wmsLogger;
    
    public StreamAuthenticater(WMSLogger logger, SessionAndFilenameValidaterIF validater) throws FileNotFoundException, IOException {
        super();
        this.wmsLogger = logger;
        this.validater = validater;
        wmsLogger.info("StreamAuthenticater created");
    }

    public void onPlay(IMediaStream stream, String streamName, double playStart,
            double playLen, int playReset) {
        String queryString = String.valueOf(stream.getClient().getQueryStr());
        String sessionID;
        String objectID;
        try {
            sessionID = StringAndTextUtil.extractValueFromQueryStringAndKey("SessionID", queryString);
            objectID = StringAndTextUtil.extractValueFromQueryStringAndKey("ObjectID", queryString);
        } catch (IllegallyFormattedQueryStringException e) {
            wmsLogger.warn("User not allowed to get content streamed, because SessionID or ObjectID was not sent", e);
            stream.getClient().setShutdownClient(true);
            return;
        }
        String filename = stream.getName(); // getStreamFileForRead().getAbsolutePath();
        wmsLogger.info("Object ID (onPlay)   : MCM authenticating: " +
                "Session ID [" + sessionID + "] " +
                "Object ID [" + objectID + "] " +
                "Stream file [" + filename + "] ");
        boolean isAuthorized = checkAuthorization(sessionID, objectID, filename,
                stream);
        wmsLogger.info("Object ID (onPlay)   : MCM result: [" + sessionID + "] allowed access: " + isAuthorized);
        if (!isAuthorized) {
            wmsLogger.warn("User not allowed to get content streamed.", stream);
            stream.getClient().setShutdownClient(true);
        }
    }

    protected boolean checkAuthorization(String sessionID, String objectID,
            String filename, IMediaStream stream) {
        boolean isAuthorized = false;
        if (filename!=null && sessionID!=null && objectID!=null) {
            try {
                isAuthorized = this.validater.validateRightsToPlayFile(sessionID, objectID, filename);
            } catch (MalformedURLException e) {
                wmsLogger.error("URL to MCM is malformed. " + e.getMessage(), stream);
                wmsLogger.error(e.getStackTrace());
            } catch (IOException e) {
                wmsLogger.error("Could not retrieve MCM information. " + e.getMessage(), stream);
                wmsLogger.error(e.getStackTrace());
            } catch (MCMOutputException e) {
                wmsLogger.error("Could not read MCM output. " + e.getMessage(), stream);
                wmsLogger.error(e.getStackTrace());
            }
        } else {
            wmsLogger.error("Arguments missing in order to authenticate stream. " +
                    "(sessionID=" + sessionID + ", objectID=" + objectID + ", filename=" + filename + ")", stream);
        }
        
        return isAuthorized;
    }

    @Override
    public void onPause(IMediaStream stream, boolean isPause, double location) {
        wmsLogger.info("StreamAuthenticator method: onPause");
    }

    @Override
    public void onPublish(IMediaStream stream, String streamName,
            boolean isRecord, boolean isAppend) {
        wmsLogger.info("StreamAuthenticator method: onPublish");
    }

    @Override
    public void onSeek(IMediaStream stream, double location) {
        wmsLogger.info("StreamAuthenticator method: onSeek");
    }

    @Override
    public void onStop(IMediaStream stream) {
        wmsLogger.info("StreamAuthenticator method: onStop");
    }

    @Override
    public void onUnPublish(IMediaStream stream, String streamName,
            boolean isRecord, boolean isAppend) {
        wmsLogger.info("StreamAuthenticator method: onUnPublish");
    }

    @Override
    public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
        wmsLogger.info("StreamAuthenticator method: onMetaData");
    }

    @Override
    public void onPauseRaw(IMediaStream stream, boolean isPause, double location) {
        wmsLogger.info("StreamAuthenticator method: onPauseRaw");
    }
}
