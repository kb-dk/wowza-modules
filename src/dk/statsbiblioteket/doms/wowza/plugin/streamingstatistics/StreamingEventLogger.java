package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
        logger.info("Statistics logger " + this.getClass().getName() + " has been created.");
        this.dateForNewLogFile = new Date();
        this.newlineString = System.getProperty( "line.separator" );
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

	public void logUserEventLiveStreamingStarted(IMediaStream stream) {
		logUserEvent(stream, Event.LIVE_STREAMING_START);
	}

	public void logUserEventStreamingStarted(IMediaStream stream) {
		logUserEvent(stream, Event.STREAMING_START);
	}

	public void logUserEventStreamingEnded(IMediaStream stream) {
		// Interested in only certain events
		//logUserEvent(stream, Event.STREAMING_END);
	}

	public void logUserEventPlay(IMediaStream stream) {
		// Interested in only certain events
		//logUserEvent(stream, Event.PLAY);
	}

	public void logUserEventStop(IMediaStream stream) {
		// Interested in only certain events
		//logUserEvent(stream, Event.STOP);
	}

	public void logUserEventPause(IMediaStream stream) {
		// Interested in only certain events
		//logUserEvent(stream, Event.PAUSE);
	}

	public void logUserEventSeek(IMediaStream stream) {
		// Interested in only certain events
		//logUserEvent(stream, Event.SEEK);
	}

	private void logUserEvent(IMediaStream stream, Event event) {
		String clientQueryString = stream.getClient().getQueryStr();
		try {
			Ticket streamingTicket = getTicket(clientQueryString);
			String logString = new StreamingStatLogEntry(logger, stream, event, streamingTicket).getLogString();
			logger.info("Streaming statistics logging line: " + logString);
			writeEventLog(logString);
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
		if ((statLogWriter==null) || (this.dateForNewLogFile.before(now))) {
			if (statLogWriter!=null) {
				statLogWriter.close();
			}
			String filenameWithCorrectDate = "StreamingStat-" + sdf.format(now) + ".log";
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
}
