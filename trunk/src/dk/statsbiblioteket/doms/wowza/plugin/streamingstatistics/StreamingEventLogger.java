package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;

import dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.QueryUtil;

public class StreamingEventLogger {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
	private String statLogFileHomeDir;
	private File currentStatLogFile;
	private static int bufferSize = 1024;
	private BufferedWriter statLogWriter;
	private Date dateForNewLogFile;
	
	private WMSLogger logger;
	private TicketToolInterface ticketTool;

	private static StreamingEventLogger instance = null;

	protected StreamingEventLogger(TicketToolInterface ticketTool, WMSLogger logger, String statLogFileHomeDir) {
		super();
		this.logger = logger;
        this.ticketTool = ticketTool;
        this.statLogFileHomeDir = statLogFileHomeDir;
        this.statLogWriter = null;
        logger.info("Statistics logger " + this.getClass().getName() + " has been created.");
        this.dateForNewLogFile = new Date();
	}

	/**
	 * Creates the singleton objects. Is robust for multiple concurrent requests for create.
	 * Only the first request for create, actually creates the object.
	 * 
	 * @param ticketTool must not be null
	 * @param logger must not be null
	 * @param statLogFileHomeDir must not be null
	 */
	public static synchronized void createInstance(TicketToolInterface ticketTool, WMSLogger logger, String statLogFileHomeDir) {
		if ((ticketTool == null) || (logger == null) || (statLogFileHomeDir == null)) {
			throw new IllegalArgumentException("A parameter is null. " +
					"ticketTool=" + ticketTool + " " +
					"logger=" + logger + " " +
					"statLogFileHomeDir=" + statLogFileHomeDir);
		}
		if(instance == null) {
			instance = new StreamingEventLogger(ticketTool, logger, statLogFileHomeDir);
		} else if (!instance.statLogFileHomeDir.equals(statLogFileHomeDir)) {
			logger.warn("Modules don't agree on location of streaming statistics log files. " +
					instance.statLogFileHomeDir + " vs. " + statLogFileHomeDir);
		}
	}

	public static synchronized StreamingEventLogger getInstance() {
		return instance;
	}

	public void logUserEventStreamingStarted(IMediaStream stream) {
		logUserEvent(stream, Event.STREAMING_START);
	}

	public void logUserEventStreamingEnded(IMediaStream stream) {
		logUserEvent(stream, Event.STREAMING_END);
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

	private void logUserEvent(IMediaStream stream, Event event) {
		String clientQueryString = stream.getClient().getQueryStr();
		try {
			Ticket streamingTicket = getTicket(clientQueryString);
			writeEventLog(new StreamingStatLogEntry(stream, event, streamingTicket).getLogString());
		} catch (IllegallyFormattedQueryStringException e) {
			logger.warn("No logging was performed. Query string of client dos not match expected format." +
					" Was " + clientQueryString);
		}
	}

	protected Ticket getTicket(String queryString) throws IllegallyFormattedQueryStringException {
		Ticket streamingTicket = null;
		if (ticketTool != null) {
			// This check is a security precaution. TicketTool is created in onAppStart and not in constructor 
			String ticketID = QueryUtil.extractTicketID(queryString);
			streamingTicket = ticketTool.resolveTicket(ticketID);
			logger.info("queryString     : " + queryString);
			logger.info("ticketID        : " + ticketID);
		}
		return streamingTicket;
	}
	
	protected synchronized void writeEventLog(String logString) {
		try {
			BufferedWriter statLogWriter = getStatLogWriter();
			statLogWriter.write(logString);
			statLogWriter.newLine();
		} catch (IOException e) {
			logger.error("An IO-error occured when writing statistics log.", e);
		}
	}

	protected BufferedWriter getStatLogWriter() throws IOException {
		Date now = new Date();
		if ((statLogWriter==null) || (this.dateForNewLogFile.before(now))) {
			if (statLogWriter!=null) {
				statLogWriter.close();
			}
			String filenameWithCorrectDate = "StreamingStat-" + sdf.format(now) + ".log";
			this.currentStatLogFile = new File(this.statLogFileHomeDir, filenameWithCorrectDate);
			this.logger.info("Creating log file: " + currentStatLogFile.getAbsolutePath());
			this.dateForNewLogFile = getFollowingMidnight(now);
			this.statLogWriter = new BufferedWriter(new FileWriter(currentStatLogFile, true), bufferSize);
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

	public void close() {
		if (statLogWriter!=null) {
			try {
				statLogWriter.close();
			} catch (IOException e) {
				logger.error("An IO-error occured when closing statistics log: " + currentStatLogFile.getAbsolutePath(), e);
			}
		}
	}

	protected void setDateForNewLogFile(Date dateForNewLogFile) {
		this.dateForNewLogFile = dateForNewLogFile;
	}
}
