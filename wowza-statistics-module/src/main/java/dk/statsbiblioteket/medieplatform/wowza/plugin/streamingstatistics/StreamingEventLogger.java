package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

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

    public void logUserEventPlay(String queryString, String streamingUrl) {
        logUserEvent(Event.PLAY, queryString, streamingUrl);
    }

    public void logUserEventStop(String queryString, String streamingUrl) {
        logUserEvent(Event.STOP, queryString, streamingUrl);
    }

    public void logUserEventPause(String queryString, String streamingUrl) {
        logUserEvent(Event.PAUSE, queryString, streamingUrl);
    }

    public void logUserEventSeek(String queryString, String streamingUrl) {
        logUserEvent(Event.SEEK, queryString, streamingUrl);
    }

    /**
     * Log the given event
     * @param event The event to log
     * @param queryString Query string to read parameters for
     * @param streamingURL The URL or this stream
     */
    private void logUserEvent(Event event, String queryString, String streamingURL) {
        if (queryString == null)  {
            logger.warn("No logging was performed. Query string of client could not be found.");
            return;
        }
        try {
            Ticket streamingTicket = StringAndTextUtil.getTicket(queryString, ticketTool);
            String logString = new StreamingStatLogEntry(event, streamingTicket, streamingURL).getLogString();
            writeEventLog(logString);
        } catch (IllegallyFormattedQueryStringException e) {
            logger.warn("No logging was performed. Query string of client does not match expected format. Was "
                    + queryString);
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
