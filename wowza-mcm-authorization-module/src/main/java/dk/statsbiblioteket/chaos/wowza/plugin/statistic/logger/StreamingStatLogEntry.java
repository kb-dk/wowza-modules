package dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.StreamingStatLogEntry.Event;

public class StreamingStatLogEntry {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
	private WMSLogger logger;
    
    public enum Event{PLAY, PAUSE, PAUSE_RESUME, STOP, SEEK, REWIND};

	// Log information
	private Date timestamp;
	private long eventID;
	private String streamName;
	private Event event;
	private int userID;
	private String mcmSessionID;
	private String mcmObjectSessionID;
	private long startedAt;
	private long endedAt;


	/**
	 * This constructor is used when creating a log entry.
	 * 
	 * Note the eventID is first set just before the log entry is persisted
	 *
	 * @param logger
	 * @param endedAt
	 * @param startedAt
	 * @param stream
	 * @param event
	 */
	public StreamingStatLogEntry(WMSLogger logger, String streamName, int clientID, String mcmSessionID, String mcmObjectSessionID, long startedAt, long endedAt, Event event) {
		this.logger = logger;
		this.timestamp = new Date();
		this.eventID = -1; // unknown until saved in db
		this.streamName = streamName;
		this.event = event;
		this.userID = clientID;
		this.mcmSessionID = mcmSessionID;
		this.mcmObjectSessionID = mcmObjectSessionID;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
		this.logger.debug("Created log entry object: " + this.toString());
	}

	public StreamingStatLogEntry(WMSLogger logger, long eventID, Date timestamp, String streamName, int userID, String mcmSessionID, String mcmObjectSessionID, long startedAt, long endedAt, Event event) {
		this(logger, streamName, userID, mcmSessionID, mcmObjectSessionID, startedAt, endedAt, event);
		this.eventID = eventID;
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getTimestampAsString() {
		return sdf.format(timestamp);
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public long getEventID() {
		if (eventID == -1) {
			throw new IllegalStateException("EventID is null in log entry. Log entry was: " + this.toString());
		}
		return eventID;
	}

	public void setEventID(long eventID) {
		this.eventID = eventID;
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getMcmSessionID() {
		return mcmSessionID;
	}

	public void setMcmSessionID(String mcmSessionID) {
		this.mcmSessionID = mcmSessionID;
	}

	public String getMcmObjectSessionID() {
		return mcmObjectSessionID;
	}

	public void setMcmObjectSessionID(String mcmObjectID) {
		this.mcmObjectSessionID = mcmObjectID;
	}

	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}

	public long getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(long endedAt) {
		this.endedAt = endedAt;
	}

	public static Event getEventFromString(String eventString) {
		Event result = null;
		if (Event.PLAY.toString().equals(eventString)) {
			result = Event.PLAY;
		} else if (Event.PAUSE.toString().equals(eventString)) {
			result = Event.PAUSE;
		} else if (Event.PAUSE_RESUME.toString().equals(eventString)) {
			result = Event.PAUSE_RESUME;
		} else if (Event.REWIND.toString().equals(eventString)) {
			result = Event.REWIND;
		} else if (Event.STOP.toString().equals(eventString)) {
			result = Event.STOP;
		} else if (Event.SEEK.toString().equals(eventString)) {
			result = Event.SEEK;
		}
		if (result == null) {
        	throw new IllegalArgumentException("Event in log line does not match the expected pattern. Was: " + eventString);
		}
		return result;
	}

	@Override
	public String toString() {
		return "StreamingStatLogEntry [timestamp=" + timestamp + ", eventID="
				+ eventID + ", streamName=" + streamName + ", event=" + event
				+ ", userID=" + userID + ", mcmSessionID=" + mcmSessionID
				+ ", mcmObjectSessionID=" + mcmObjectSessionID + ", startedAt="
				+ startedAt + ", endedAt=" + endedAt + "]";
	}
}
