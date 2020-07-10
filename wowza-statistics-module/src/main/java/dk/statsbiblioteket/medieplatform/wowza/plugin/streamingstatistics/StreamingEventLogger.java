package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

public class StreamingEventLogger {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String FILENAME_PREFIX = "StreamingStat-";
    private String statLogFileHomeDir;
    private BufferedWriter statLogWriter;
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
            BufferedWriter statLogWriter = getStatLogWriter();
            statLogWriter.write(logString);
            statLogWriter.write(this.newlineString);
            statLogWriter.flush();
        } catch (IOException e) {
            logger.error("An IO-error occured when writing statistics log.", e);
        }
    }

    protected BufferedWriter getStatLogWriter() throws IOException {
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
            this.statLogWriter = Files.newBufferedWriter(currentStatLogFile.toPath(), StandardCharsets.UTF_8, 
                    StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            if (newLogFile) {
                this.statLogWriter.write(StreamingStatLogEntry.getLogStringHeadline());
                this.statLogWriter.write(this.newlineString);
                this.statLogWriter.flush();
            }
        }
        return statLogWriter;
    }

    protected Date getFollowingMidnight(Date date) {
        ZoneId zone = ZoneId.of("Europe/Copenhagen");
        LocalDate localDate = date.toInstant().atZone(zone).toLocalDate();
        LocalDateTime tomorroMidnight = LocalDateTime.of(localDate, LocalTime.MIDNIGHT).plusDays(1);
        return Date.from(tomorroMidnight.atZone(zone).toInstant());
    }

    protected void setDateForNewLogFile(Date dateForNewLogFile) {
        this.dateForNewLogFile = dateForNewLogFile;
    }

    public static String getFilename(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);
        return FILENAME_PREFIX + sdf.format(time) + ".log";
    }
}
