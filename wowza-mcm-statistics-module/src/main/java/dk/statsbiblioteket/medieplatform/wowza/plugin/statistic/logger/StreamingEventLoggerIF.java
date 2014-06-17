package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger;

/**
 * Interface for logging streaming events.
 */
public interface StreamingEventLoggerIF {

    /**
     * Get the session ID used for logging
     * @param mcmObjectID Given an MCM object ID, return the session ID pair used for generating a log entry.
     * @return The session ID pair.
     */
    public abstract SessionIDPair getStreamingLogSessionID(String mcmObjectID);

    /**
     * Log an event.
     * @param logEntry The event to log.
     */
    public abstract void logEvent(StreamingStatLogEntry logEntry);

    /**
     * Get the latest log event.
     * @return The latest log event.
     */
    public abstract StreamingStatLogEntry getLogEntryLatest();

}