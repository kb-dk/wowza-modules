package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;

import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.SessionIDPair;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingEventLoggerIF;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Action listerner, that logs events in a StreamingEventLogger.
 */
public class StatisticLoggingStreamListener implements IMediaStreamActionNotify2 {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    private WMSLogger logger;
    private StreamingEventLoggerIF streamingEventLogger;
    private SessionIDPair sessionIDPair;
    private String mcmObjectID;
    private int clientID;
    private Date lastStartTime;
    private long lastStartLocation;

    /** Initialise with given values. Also reads object ID from query string.
     *
     * @param logger Wowza logger
     * @param stream Stream to listen for events on
     * @param streamingEventLogger StreamingEventLogger to log events to.
     */
    public StatisticLoggingStreamListener(WMSLogger logger, IMediaStream stream, StreamingEventLoggerIF streamingEventLogger) {
        this.logger = logger;
        this.streamingEventLogger = streamingEventLogger;
        String queryString = getQueryString(stream);
        try {
            this.mcmObjectID = StringAndTextUtil.extractValueFromQueryStringAndKey("ObjectID", queryString);
        } catch (IllegallyFormattedQueryStringException e) {
            this.mcmObjectID = "Unknown";
        }
        this.sessionIDPair = streamingEventLogger.getStreamingLogSessionID(mcmObjectID);
        this.clientID = stream.getClientId();
        this.lastStartTime = null;
        this.lastStartLocation = -1;
    }

    private String getQueryString(IMediaStream stream) {
        if (stream.getClient() != null) {
            return String.valueOf(stream.getClient().getQueryStr());
        } else if (stream.getHTTPStreamerSession() != null) {
            return String.valueOf(stream.getHTTPStreamerSession().getQueryStr());
        } else {
            return "";
        }
    }

    /**
     * Log play event.
     * @param stream Stream to log
     * @param streamName Name of stream to log
     * @param playStart Start time
     * @param playLen Length played
     * @param playReset Unused.
     */
    @Override
    public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen, int playReset) {
        logger.debug("Event triggered [onPlay]: " + stream.getName() + " - " + playStart + ", " + playLen + ", " + playReset); 
        long startedAt = Math.max((long) playStart, 0);
        long endedAt = startedAt;
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, 
                streamName, 
                clientID,
                sessionIDPair.getSessionID(),
                sessionIDPair.getObjectSessionID(),
                startedAt,
                endedAt,
                Event.PLAY);
        streamingEventLogger.logEvent(logEntry);
        this.lastStartTime = new Date();
        this.lastStartLocation = startedAt;
    }

    /**
     * Log pause event.
     * State: Playing
     *  Case 1: Event pausing in client results in: onPause(stream, true, location)
     *  Case 2: Event seeking in client results in: onPause(stream, true, location) - > onPause(stream, false, location) - > onSeek
     *   
     * State: Paused
     *  Case 3: Event resume in client results in: onPause(stream, true, location) - > onSeek
     *  Case 4: Event seeking in client results in: onPause(stream, false, location) - > onSeek
     *
     * @param stream Stream that was paused.
     * @param isPause whether it was a pause event.
     * @param location position in stream.
     */
    @Override
    public void onPause(IMediaStream stream, boolean isPause, double location) {
        logger.debug("Event triggered [onPause]: " + stream.getName() + " - " + isPause + ", " + location); 
        if (isPause && wasPlaying()) {
            // Play paused (and was playing). Case 1 and 2.
            Event event = Event.PAUSE;
            long startedAt = lastStartLocation;
            // Pause is registered at whole seconds, by truncating decimals.
            long endedAt = Math.max((long) location, lastStartLocation);
            StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, 
                    stream.getName(), 
                    clientID,
                    sessionIDPair.getSessionID(),
                    sessionIDPair.getObjectSessionID(),
                    startedAt,
                    endedAt,
                    event);
            logger.debug("LogEntry (onPause): " + logEntry);
            streamingEventLogger.logEvent(logEntry);
            this.lastStartTime = null;
            this.lastStartLocation = -1;
        }
        if (!isPause) {
            // Play resumed. Case 3.
            Event event = Event.PAUSE_RESUME;
            long startedAt = (long) location;
            long endedAt = (long) location;
            StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, 
                    stream.getName(), 
                    clientID,
                    sessionIDPair.getSessionID(),
                    sessionIDPair.getObjectSessionID(),
                    startedAt,
                    endedAt,
                    event);
            logger.debug("LogEntry (onPause): " + logEntry);
            streamingEventLogger.logEvent(logEntry);
            this.lastStartTime = new Date();
            this.lastStartLocation = startedAt;
        }
        // Case 4 is ignored. Seeking while still paused does not result in a logable event. 
        // Will eventually result in Case 3, 4 or STOP event.
    }


    /** See {@link #onPause(IMediaStream, boolean, double)}  */
    @Override
    public void onPauseRaw(IMediaStream stream, boolean isPause, double location) {
        this.onPause(stream, isPause, location);
    }

    /**
     * Log seek event.
     * @param stream      Stream event happened for.
     * @param location    Location in stream.
     */
    @Override
    public void onSeek(IMediaStream stream, double location) {
        logger.debug("Event triggered [onSeek]: " + stream.getName() + " - " + location); 
        if (((long) location == 0) && wasPlaying()) {
            // In a rewind event. No pause events are triggered relating to this event.
            Date now = new Date();
            long playDuration = now.getTime() - this.lastStartTime.getTime();
            long startedAt = this.lastStartLocation;
            long endedAt = startedAt + playDuration;
            StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, 
                    stream.getName(), 
                    clientID,
                    sessionIDPair.getSessionID(),
                    sessionIDPair.getObjectSessionID(),
                    startedAt,
                    endedAt,
                    Event.REWIND);
            streamingEventLogger.logEvent(logEntry);
            this.lastStartTime = new Date();
            this.lastStartLocation = (long) location;
        } else {
            long startedAt = (long) location;
            long endedAt = (long) location;
            StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, 
                    stream.getName(), 
                    clientID,
                    sessionIDPair.getSessionID(),
                    sessionIDPair.getObjectSessionID(),
                    startedAt,
                    endedAt,
                    Event.SEEK);
            streamingEventLogger.logEvent(logEntry);
            if (stream.isPlaying()) {
                this.lastStartTime = new Date();
                this.lastStartLocation = startedAt;
            } else {
                this.lastStartTime = null;
                this.lastStartLocation = -1;
            }
        }
    }

    /**
     * Log stop event. Uses cached value to calculate how long it played.
     * @param stream      Stream event happened for.
     */
    @Override
    public void onStop(IMediaStream stream) {
        logger.debug("Event triggered [onStop]: " + stream.getName() + " - "); 
        long startedAt;
        long endedAt;
        if (wasPlaying()) {
            Date now = new Date();
            long playDuration = now.getTime() - this.lastStartTime.getTime();
            startedAt = this.lastStartLocation;
            endedAt = startedAt + playDuration;
        } else {
            // Was not playing. Logging arbitrary location
            startedAt = 0;
            endedAt = 0;
        }
        StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, 
                stream.getName(), 
                clientID,
                sessionIDPair.getSessionID(),
                sessionIDPair.getObjectSessionID(),
                startedAt,
                endedAt,
                Event.STOP);
        streamingEventLogger.logEvent(logEntry);
        this.lastStartTime = null;
        this.lastStartLocation = -1;
    }

    private boolean wasPlaying() {
        return (this.lastStartTime != null);
    }

    @Override
    public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
        // Irrelevant
    }

    @Override
    public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
        // Irrelevant
    }

    @Override
    public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
        // Irrelevant
    }
}
