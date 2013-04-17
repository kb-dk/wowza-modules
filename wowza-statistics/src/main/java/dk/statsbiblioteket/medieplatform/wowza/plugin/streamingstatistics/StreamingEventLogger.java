package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.QueryUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StreamingEventLogger {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    public static final String filenamePrefix = "StreamingStat-";
    private String statLogFileHomeDir;
    private File currentStatLogFile;
    private FileWriter statLogWriter;
    private Date dateForNewLogFile;

    private WMSLogger logger;
    private TicketToolInterface ticketTool;
    private String newlineString;

    private static StreamingEventLogger instance = null;

    protected StreamingEventLogger(TicketToolInterface ticketTool, WMSLogger logger, String statLogFileHomeDir) {
        super();
        this.logger = logger;
        this.ticketTool = ticketTool;
        this.statLogFileHomeDir = statLogFileHomeDir;
        this.statLogWriter = null;
        logger.info("Statistics logger " + this.getClass().getName() + " has been created, logging files to '"
                            + statLogFileHomeDir + "'.");
        this.dateForNewLogFile = new Date();
        this.newlineString = System.getProperty("line.separator");
    }

    /**
     * Creates the singleton object. Is robust for multiple concurrent requests for create.
     * Only the first request for create, actually creates the object.
     *
     * @param ticketTool         The ticket tool used for resolving tickets. Must not be null.
     * @param logger             The Wowza logger to log events with. Must not be null.
     * @param statLogFileHomeDir The directory to write logs to. Must not be null.
     */
    public static synchronized void createInstance(TicketToolInterface ticketTool, WMSLogger logger,
                                                   String statLogFileHomeDir) {
        if ((ticketTool == null) || (logger == null) || (statLogFileHomeDir == null)) {
            throw new IllegalArgumentException("A parameter is null. " +
                                                       "ticketTool=" + ticketTool + " " +
                                                       "logger=" + logger + " " +
                                                       "statLogFileHomeDir=" + statLogFileHomeDir);
        }
        if (instance == null) {
            instance = new StreamingEventLogger(ticketTool, logger, statLogFileHomeDir);
        } else if (!instance.statLogFileHomeDir.equals(statLogFileHomeDir)) {
            logger.warn("Modules don't agree on location of streaming statistics log files. " +
                                instance.statLogFileHomeDir + " vs. " + statLogFileHomeDir);
        }
    }

    /**
     * Get the singleton instance. Must ONLY be called after {@link #createInstance} has been called to initialize the
     * interface.
     *
     * @return The singleton instance
     *
     * @throws IllegalStateException If the singleton is not initialized.
     */
    public static synchronized StreamingEventLogger getInstance() {
        return instance;
    }

    public void logUserEventPlay(IMediaStream stream) {
        // Interested in only certain events
        logUserEvent(stream, Event.PLAY);
    }

    public void logUserEventStop(IMediaStream stream) {
        // Interested in only certain events
        logUserEvent(stream, Event.STOP);
    }

    public void logUserEventPause(IMediaStream stream) {
        // Interested in only certain events
        logUserEvent(stream, Event.PAUSE);
    }

    public void logUserEventSeek(IMediaStream stream) {
        // Interested in only certain events
        logUserEvent(stream, Event.SEEK);
    }

    /**
     * Log the given event
     * @param stream Stream from which to get the ticket needed for creating the log line
     * @param event The event to log
     */
    private void logUserEvent(IMediaStream stream, Event event) {
        String clientQueryString = stream.getClient().getQueryStr();
        try {
            Ticket streamingTicket = QueryUtil.getTicket(clientQueryString, ticketTool);
            String logString = new StreamingStatLogEntry(stream, event, streamingTicket).getLogString();
            //logger.info("Streaming statistics logging line: " + logString);
            writeEventLog(logString);
        } catch (IllegallyFormattedQueryStringException e) {
            logger.warn("No logging was performed. Query string of client dos not match expected format." +
                                " Was " + clientQueryString);
        }
    }

    protected synchronized void writeEventLog(String logString) {
        try {
            Writer statLogWriter = getStatLogWriter();
            statLogWriter.write(logString);
            statLogWriter.write(this.newlineString);
            this.statLogWriter.flush();
        } catch (IOException e) {
            logger.error("An IO-error occured when writing statistics log.", e);
        }
    }

    protected Writer getStatLogWriter() throws IOException {
        Date now = new Date();
        if ((statLogWriter == null) || (this.dateForNewLogFile.before(now))) {
            if (statLogWriter != null) {
                statLogWriter.close();
            }
            String filenameWithCorrectDate = getFilename(now);
            this.currentStatLogFile = new File(this.statLogFileHomeDir, filenameWithCorrectDate);
            this.logger.info("Creating log file: " + currentStatLogFile.getAbsolutePath());
            this.dateForNewLogFile = getFollowingMidnight(now);
            this.statLogWriter = new FileWriter(currentStatLogFile, true);
            this.statLogWriter.write(StreamingStatLogEntry.getLogStringHeadline());
            this.statLogWriter.write(this.newlineString);
            this.statLogWriter.flush();
        }
        return statLogWriter;
    }

    protected Date getFollowingMidnight(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected void setDateForNewLogFile(Date dateForNewLogFile) {
        this.dateForNewLogFile = dateForNewLogFile;
    }

    public static String getFilename(Date time) {
        return filenamePrefix + sdf.format(time) + ".log";
    }
}
