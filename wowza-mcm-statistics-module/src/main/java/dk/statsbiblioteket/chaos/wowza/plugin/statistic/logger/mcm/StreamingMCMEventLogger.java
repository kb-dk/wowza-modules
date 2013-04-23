package dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.mcm;

import java.util.List;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.SessionIDPair;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingEventLoggerIF;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry;
import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;

public class StreamingMCMEventLogger implements StreamingEventLoggerIF {

    private final WMSLogger logger;

    private static StreamingMCMEventLogger instance = null;

    /**
     * Reads db connection information from property file and creates connection
     *
     *
     * @param logger                 The Wowza logger.
     */
    private StreamingMCMEventLogger(WMSLogger logger) {
        this.logger = logger;
        this.logger.info("Statistics logger " + this.getClass().getName() + " has been created.");
    }

    /**
     * Creates the singleton objects. Is robust for multiple concurrent requests for create.
     * Only the first request for create, actually creates the object.
     * @param logger                 The Wowza logger.
     */
    public static synchronized void createInstance(WMSLogger logger) {
        if ((logger == null)) {
            throw new IllegalArgumentException(
                    "A parameter is null. " + "logger=" + logger);
        }
        if (instance == null) {
            instance = new StreamingMCMEventLogger(logger);
        }
    }

    public static synchronized StreamingMCMEventLogger getInstance() {
        return instance;
    }

    @Override
    public SessionIDPair getStreamingLogSessionID(String mcmObjectID) {
        String sessionID = MCMPortalInterfaceStatisticsImpl.getInstance().getStatisticsSession();
        String objectSessionID = MCMPortalInterfaceStatisticsImpl.getInstance()
                .getStatisticsObjectSession(sessionID, mcmObjectID);
        return new SessionIDPair(sessionID, objectSessionID);
    }

    @Override
    public void logEvent(StreamingStatLogEntry logEntry) {
        if (Event.PLAY.equals(logEntry.getEvent()) || Event.PAUSE.equals(logEntry.getEvent())
                || Event.REWIND.equals(logEntry.getEvent()) || Event.STOP.equals(logEntry.getEvent())) {
            logger.info("Streaming statistics logging line: " + logEntry);
            logEventInMCM(logEntry);
        }
    }

    private synchronized void logEventInMCM(StreamingStatLogEntry logEntry) {
        MCMPortalInterfaceStatisticsImpl.getInstance()
                .logPlayDuration(logEntry.getMcmSessionID(), logEntry.getMcmObjectSessionID(), logEntry.getStartedAt(),
                                 logEntry.getEndedAt());
    }

    @Override
    public StreamingStatLogEntry getLogEntryLatest() {
        throw new UnsupportedOperationException("MCM logger does not support queries");
    }

    public List<StreamingStatLogEntry> getLogEntryLatest(int numberOfEntries) {
        throw new UnsupportedOperationException("MCM logger does not support queries");
    }
}
