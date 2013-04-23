package dk.statsbiblioteket.chaos.wowza.plugin.statistic.logger.mcm;

public interface MCMPortalInterfaceStatistics {

	public abstract String getStatisticsSession();

	public abstract String getStatisticsObjectSession(String sessionID,
			String mcmObjectID);

	public abstract String logPlayDuration(String sessionID,
			String objectSessionID, long startedAt, long endedAt);

}