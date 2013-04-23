package dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger;

public class SessionIDPair {
	
	// ID pair for MCM statistics logging
	public String sessionID;
	public String objectSessionID;

	public SessionIDPair(String sessionID, String objectSessionID) {
		this.sessionID = sessionID;
		this.objectSessionID = objectSessionID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public String getObjectSessionID() {
		return objectSessionID;
	}
}
