package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.util.Date;

public class ConnectionInformation {

	// Connection information
	public String connectionID;
	
	// User information
	public String organisationID;
	public String userID;
	
	// Media information
	public String channelID;
	public String channelName;
	public String programTitle;
	public Date programStart;
	
	// Event information
	public enum EventType {START_PLAY, PAUSE, SEEK, STOP_PLAY}
	public EventType event;
}
