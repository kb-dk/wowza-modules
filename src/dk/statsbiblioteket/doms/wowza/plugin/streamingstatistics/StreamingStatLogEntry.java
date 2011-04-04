package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.wowza.wms.stream.IMediaStream;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketProperty;

public class StreamingStatLogEntry {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    public enum Event{STREAMING_START, PLAY, PAUSE, STOP, SEEK, STREAMING_END};

	// Log information
	private Date timestamp;
	
	// Wowza related information
	private String connectionID;
	private Event event;
	
	// User information
	private String organisationID;
	private String userID;
	
	// Media information
	private String channelID;
	private String programTitle;
	private String programStart;
	
	public StreamingStatLogEntry(IMediaStream stream, Event event, Ticket streamingTicket) {
		this.setTimestamp(new Date());
		this.connectionID = stream.getUniqueStreamIdStr();
		this.event = event;
		Map<String, String> propertyMap = createMap(streamingTicket.getProperty());
		if (propertyMap.get("eduPersonTargetedID")!=null) {
			this.organisationID = propertyMap.get("schacHomeOrganization");
			this.userID = propertyMap.get("eduPersonTargetedID");
		} else {
			// In this case, the user is not authenticated by WAYF
			// The user is assumed located in SB. Logging IP-address
			this.organisationID = "statsbiblioteket.dk";
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
		return connectionID;
	}

	public void setConnectionID(String connectionID) {
		this.connectionID = connectionID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getProgramTitle() {
		return programTitle;
	}

	public void setProgramTitle(String programTitle) {
		this.programTitle = programTitle;
	}

	public String getProgramStart() {
		return programStart;
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

	public String getLogString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(timestamp));
		sb.append(";");
		sb.append(escapeLogString(getConnectionID()));
		sb.append(";");
		sb.append(getEvent());
		sb.append(";");
		sb.append(escapeLogString(getUserID()));
		sb.append(";");
		sb.append(escapeLogString(getOrganisationID()));
		sb.append(";");
		sb.append(escapeLogString(getChannelID()));
		sb.append(";");
		sb.append(escapeLogString(getProgramTitle()));
		sb.append(";");
		sb.append(escapeLogString(getProgramStart()));
		return sb.toString();
	}
	
	protected String escapeLogString(String logLine) {
		return logLine.replaceAll(";", "[semicolon]");
	}
	
}
