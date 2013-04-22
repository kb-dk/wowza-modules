package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.QueryUtil;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StreamingEventLogger {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String FILENAME_PREFIX = "StreamingStat-";
    private String statLogFileHomeDir;
    private FileWriter statLogWriter;
    private Date dateForNewLogFile;

    private WMSLogger logger;
    private TicketToolInterface ticketTool;
    private String newlineString;

    public StreamingEventLogger(TicketToolInterface ticketTool, WMSLogger logger, String statLogFileHomeDir) {
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

    public void logUserEventPlay(IMediaStream stream) {
        logUserEvent(stream, Event.PLAY);
    }

    public void logUserEventStop(IMediaStream stream) {
        logUserEvent(stream, Event.STOP);
    }

    public void logUserEventPause(IMediaStream stream) {
        logUserEvent(stream, Event.PAUSE);
    }

    public void logUserEventSeek(IMediaStream stream) {
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
            writeEventLog(logString);
        } catch (IllegallyFormattedQueryStringException e) {
            logger.warn("No logging was performed. Query string of client dos not match expected format. Was "
                    + clientQueryString);
        }
    }

    protected synchronized void writeEventLog(String logString) {
        try {
            Writer statLogWriter = getStatLogWriter();
            statLogWriter.write(logString);
            statLogWriter.write(this.newlineString);
            statLogWriter.flush();
        } catch (IOException e) {
            logger.error("An IO-error occured when writing statistics log.", e);
        }
    }

    protected Writer getStatLogWriter() throws IOException {
        File currentStatLogFile;
        Date now = new Date();
        if ((statLogWriter == null) || (this.dateForNewLogFile.before(now))) {
            if (statLogWriter != null) {
                statLogWriter.close();
            }
            String filenameWithCorrectDate = getFilename(now);
            currentStatLogFile = new File(this.statLogFileHomeDir, filenameWithCorrectDate);
            this.logger.info("Creating log file: " + currentStatLogFile.getAbsolutePath());
            this.dateForNewLogFile = getFollowingMidnight(now);
            boolean newLogFile = !currentStatLogFile.exists();
            this.statLogWriter = new FileWriter(currentStatLogFile, true);
            if (newLogFile) {
                this.statLogWriter.write(StreamingStatLogEntry.getLogStringHeadline());
                this.statLogWriter.write(this.newlineString);
                this.statLogWriter.flush();
            }
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
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return FILENAME_PREFIX + sdf.format(time) + ".log";
    }
}
