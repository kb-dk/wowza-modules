package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketProperty;

public class StreamingStatLogEntry {

	// Format of log line is "Timestamp;Connection ID;Event;User ID;Organization ID;Channel ID;Program title;Program start"
    private static Pattern logLinePattern = Pattern.compile("([^;]*);([^;]*);([^;]*);([^;]*);([^;]*);([^;]*);([^;]*);([^;]*);([^$]*)");

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
	private WMSLogger logger;
    
    public enum Event{LIVE_STREAMING_START, STREAMING_START, PLAY, PAUSE, STOP, SEEK, STREAMING_END};

	// Log information
	private Date timestamp;
	
	// Wowza related information
	private String connectionID;
	private Event event;
	
	// Ticket information
	private boolean wasTicketAttached;
	
	// User information
	private String organisationID;
	private String userRole;
	private String userID;
	
	// Media information
	private String channelID;
	private String programTitle;
	private String programStart;

	private static final String invalidSessionOrganizationID = "no organization info";
	private static final String invalidSessionUserRole = "no role info";
	private static final String invalidSessionUserID = "no user info";
	private static final String invalidSessionChannelID = "SB rick roll video";
	private static final String invalidSessionProgramTitle = "SB rick roll video";
	private static final String invalidSessionProgramStart = "SB rick roll video";
	
	public StreamingStatLogEntry(WMSLogger logger, String logLine) throws InvalidLogLineParseException, HeadlineEncounteredException {
		this.logger = logger;
        extractLogEntry(logLine);
	}

	public StreamingStatLogEntry(WMSLogger logger, IMediaStream stream, Event event, Ticket streamingTicket) {
		this.logger = logger;
		this.setTimestamp(new Date());
		this.connectionID = stream.getUniqueStreamIdStr();
		this.event = event;
		this.wasTicketAttached = (streamingTicket != null);
		if	(this.wasTicketAttached) {
			retrieveTicketInformation(streamingTicket);
		} else {
			this.organisationID = null;
			this.userID = null;
			this.userRole = null;
			this.channelID = null;
			this.programTitle = null;
			this.programStart = null;
		}
	}

	private void retrieveTicketInformation(Ticket streamingTicket) {
		Map<String, String> propertyMap = createMap(streamingTicket.getProperty());
		if (propertyMap.get("eduPersonTargetedID")!=null) {
			this.organisationID = propertyMap.get("schacHomeOrganization");
			this.userRole = propertyMap.get("eduPersonScopedAffiliation");
			this.userID = propertyMap.get("eduPersonTargetedID");
		} else {
			// In this case, the user is not authenticated by WAYF
			// The user is assumed located in SB. Logging IP-address
			// SB users does have a role attached
			this.organisationID = "statsbiblioteket.dk";
			this.userRole = propertyMap.get("eduPersonScopedAffiliation");
			this.userID = streamingTicket.getUsername();
		}
		this.channelID = propertyMap.get("metaChannelName");
		this.programTitle = propertyMap.get("metaTitle");
		this.programStart = propertyMap.get("metaDateTimeStart");
	}

	protected Map<String, String> createMap(List<TicketProperty> properties) {
		Map<String, String> propertyMap = new HashMap<String, String>();
		for (Iterator<TicketProperty> i = properties.iterator(); i.hasNext();) {
			TicketProperty prop = i.next();
			propertyMap.put(prop.getName(), prop.getValue());
		}
		return propertyMap;
	}

	protected void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getConnectionID() {
		return transformNullValue(connectionID);
	}

	public void setConnectionID(String connectionID) {
		this.connectionID = connectionID;
	}

	public String getOrganisationID() {
		return transformNullValue(organisationID);
	}

	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	public String getUserRole() {
		return transformNullValue(userRole);
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getUserID() {
		return transformNullValue(userID);
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getChannelID() {
		return transformNullValue(channelID);
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getProgramTitle() {
		return transformNullValue(programTitle);
	}

	public void setProgramTitle(String programTitle) {
		this.programTitle = programTitle;
	}

	public String getProgramStart() {
		return transformNullValue(programStart);
	}

	public void setProgramStart(String programStart) {
		this.programStart = programStart;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String transformNullValue(String inputString) {
		String returnString;
		if (inputString == null) {
			returnString = "-";
		} else {
			returnString = inputString;
		}
		return returnString;
	}
	
	public String getLogString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(timestamp));
		sb.append(";");
		sb.append(escapeLogString(getConnectionID()));
		sb.append(";");
		sb.append(getEvent());
		sb.append(";");
		if (wasTicketAttached) {
			sb.append(escapeLogString(getUserID()));
			sb.append(";");
			sb.append(escapeLogString(getUserRole()));
			sb.append(";");
			sb.append(escapeLogString(getOrganisationID()));
			sb.append(";");
			sb.append(escapeLogString(getChannelID()));
			sb.append(";");
			sb.append(escapeLogString(getProgramTitle()));
			sb.append(";");
			sb.append(escapeLogString(getProgramStart()));
		} else {
			sb.append(invalidSessionUserID);
			sb.append(";");
			sb.append(invalidSessionUserRole);
			sb.append(";");
			sb.append(invalidSessionOrganizationID);
			sb.append(";");
			sb.append(invalidSessionChannelID);
			sb.append(";");
			sb.append(invalidSessionProgramTitle);
			sb.append(";");
			sb.append(invalidSessionProgramStart);
		}
		return sb.toString();
	}
	
	public static String getLogStringHeadline() {
		StringBuilder sb = new StringBuilder();
		sb.append("Timestamp");
		sb.append(";");
		sb.append("Connection ID");
		sb.append(";");
		sb.append("Event");
		sb.append(";");
		sb.append("User ID");
		sb.append(";");
		sb.append("User Role");
		sb.append(";");
		sb.append("Organization ID");
		sb.append(";");
		sb.append("Channel ID");
		sb.append(";");
		sb.append("Program title");
		sb.append(";");
		sb.append("Program start");
		return sb.toString();
	}
	
	protected String escapeLogString(String logLine) {
		return logLine.replaceAll(";", "[semicolon]");
	}

	protected void extractLogEntry(String logLine) throws InvalidLogLineParseException, HeadlineEncounteredException {
		Matcher matcher = logLinePattern.matcher(logLine);
        if (!matcher.find()) {
        	throw new InvalidLogLineParseException("Log line does not match the expected pattern. Was: " + logLine);
        }
		try {
			
			this.setTimestamp(sdf.parse(matcher.group(1)));
			this.connectionID = matcher.group(2);
			this.event = getEventFromString(matcher.group(3));
			this.userID = matcher.group(4);
			this.userRole = matcher.group(5);
			this.organisationID = matcher.group(6);
			this.channelID = matcher.group(7);
			this.programTitle = matcher.group(8);
			this.programStart = matcher.group(9);
			this.wasTicketAttached = ((this.channelID != null) && (!this.channelID.equals(invalidSessionChannelID)));
		} catch (ParseException e) {
			if (getLogStringHeadline().equals(logLine)) {
				throw new HeadlineEncounteredException("Log line could not be parsed. Was headline: " + logLine);
			}
        	throw new InvalidLogLineParseException("Timestamp in log line does not match the expected pattern. Was: " + logLine);
		}
	}
	
	private Event getEventFromString(String eventString) {
		Event result = null;
		if (Event.LIVE_STREAMING_START.toString().equals(eventString)) {
			result = Event.LIVE_STREAMING_START;
		} else if (Event.STREAMING_START.toString().equals(eventString)) {
			result = Event.STREAMING_START;
		} else if (Event.PLAY.toString().equals(eventString)) {
			result = Event.PLAY;
		} else if (Event.PAUSE.toString().equals(eventString)) {
			result = Event.PAUSE;
		} else if (Event.STOP.toString().equals(eventString)) {
			result = Event.STOP;
		} else if (Event.SEEK.toString().equals(eventString)) {
			result = Event.SEEK;
		} else if (Event.STREAMING_END.toString().equals(eventString)) {
			result = Event.STREAMING_END;
		}
		if (result == null) {
        	throw new IllegalArgumentException("Event in log line does not match the expected pattern. Was: " + eventString);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((channelID == null) ? 0 : channelID.hashCode());
		result = prime * result
				+ ((connectionID == null) ? 0 : connectionID.hashCode());
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result
				+ ((programStart == null) ? 0 : programStart.hashCode());
		result = prime * result
				+ ((programTitle == null) ? 0 : programTitle.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		result = prime * result
				+ ((userRole == null) ? 0 : userRole.hashCode());
		result = prime * result + (wasTicketAttached ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StreamingStatLogEntry other = (StreamingStatLogEntry) obj;
		if (channelID == null) {
			if (other.channelID != null)
				return false;
		} else if (!channelID.equals(other.channelID))
			return false;
		if (connectionID == null) {
			if (other.connectionID != null)
				return false;
		} else if (!connectionID.equals(other.connectionID))
			return false;
		if (event != other.event)
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (programStart == null) {
			if (other.programStart != null)
				return false;
		} else if (!programStart.equals(other.programStart))
			return false;
		if (programTitle == null) {
			if (other.programTitle != null)
				return false;
		} else if (!programTitle.equals(other.programTitle))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		if (userRole == null) {
			if (other.userRole != null)
				return false;
		} else if (!userRole.equals(other.userRole))
			return false;
		if (wasTicketAttached != other.wasTicketAttached)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StreamingStatLogEntry [timestamp=" + timestamp
				+ ", connectionID=" + connectionID + ", event=" + event
				+ ", wasTicketAttached=" + wasTicketAttached
				+ ", organisationID=" + organisationID + ", userRole="
				+ userRole + ", userID=" + userID + ", channelID=" + channelID
				+ ", programTitle=" + programTitle + ", programStart="
				+ programStart + "]";
	}

	
}
