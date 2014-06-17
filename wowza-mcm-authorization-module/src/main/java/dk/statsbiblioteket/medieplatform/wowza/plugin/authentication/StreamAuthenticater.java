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

/** Wowza aciion notifier, that prevents a play action if session is not validated. */
class StreamAuthenticater  implements IMediaStreamActionNotify2 {
    
    /** The session validator to use for validating access. */
    private SessionAndFilenameValidaterIF validater;
    /** Logger-. */
    private WMSLogger wmsLogger;

    /** Initialise with a wowza logger and a session validator.
     *
     * @param logger Used for logging events.
     * @param validater Used for validating a session before playing.
     */
    public StreamAuthenticater(WMSLogger logger, SessionAndFilenameValidaterIF validater) {
        super();
        this.wmsLogger = logger;
        this.validater = validater;
        wmsLogger.info("StreamAuthenticater created");
    }

    /**
     * Prevent playing if session is not valid. Session is validated using values for ObjectID and SessionID given in
     * query string on stream, and stream name. {@link #checkAuthorization(String, String)} is used to check
     * authorization.
     * Stream is shut down if client not allowed to play.
     * Called by wowza on playevent.
     *
     * @param stream The stream requested to play. Used for extracting query parameters and stream name.
     * @param streamName Not used.
     * @param playStart Not used.
     * @param playLen Not used.
     * @param playReset Not used.
     */
    @Override
    public void onPlay(IMediaStream stream, String streamName, double playStart,
            double playLen, int playReset) {
        String queryString = String.valueOf(stream.getClient().getQueryStr());
        boolean isAuthorized = checkAuthorization(queryString, stream.getName());
        if (!isAuthorized) {
            wmsLogger.warn("User not allowed to get content streamed.", stream);
            stream.getClient().setShutdownClient(true);
        }
    }

    /**
     * Extract ObjectID and SessionID from query string and call
     * {@link #checkAuthorization(String, String, String)}.
     * @param queryString Query string to extract SessionID and ObjectID from
     * @param filename File name to pass on.
     * @return Whether stream is valid for playing.
     */
    public boolean checkAuthorization(String queryString, String filename) {
        String sessionID;
        String objectID;
        try {
            sessionID = StringAndTextUtil.extractValueFromQueryStringAndKey("SessionID", queryString);
            objectID = StringAndTextUtil.extractValueFromQueryStringAndKey("ObjectID", queryString);
        } catch (IllegallyFormattedQueryStringException e) {
            wmsLogger.warn("User not allowed to get content streamed, because SessionID or ObjectID was not sent", e);
            return false;
        }
        wmsLogger.info("Object ID (onPlay)   : MCM authenticating: " +
                "Session ID [" + sessionID + "] " +
                "Object ID [" + objectID + "] " +
                "Stream file [" + filename + "] ");
        boolean isAuthorized = checkAuthorization(sessionID, objectID, filename);
        wmsLogger.info("Object ID (onPlay)   : MCM result: [" + sessionID + "] allowed access: " + isAuthorized);
        return isAuthorized;
    }

    /**
     * Check that stream is valid for playing, by calling
     * {@link SessionAndFilenameValidaterIF#validateRightsToPlayFile(String, String, String)}
     *
     * @param sessionID The session ID.
     * @param objectID The object ID.
     * @param filename The filename to play.
     * @return Whether stream is valid for playing.
     */
    protected boolean checkAuthorization(String sessionID, String objectID, String filename) {
        boolean isAuthorized = false;
        if (filename!=null && sessionID!=null && objectID!=null) {
            try {
                isAuthorized = this.validater.validateRightsToPlayFile(sessionID, objectID, filename);
            } catch (MalformedURLException e) {
                wmsLogger.error("URL to MCM is malformed. " + e.toString());
                wmsLogger.error(e.getStackTrace());
            } catch (IOException e) {
                wmsLogger.error("Could not retrieve MCM information. " + e.toString());
                wmsLogger.error(e.getStackTrace());
            } catch (MCMOutputException e) {
                wmsLogger.error("Could not read MCM output. " + e.toString());
                wmsLogger.error(e.getStackTrace());
            }
        } else {
            wmsLogger.error("Arguments missing in order to authenticate stream. " +
                    "(sessionID=" + sessionID + ", objectID=" + objectID + ", filename=" + filename + ")");
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
