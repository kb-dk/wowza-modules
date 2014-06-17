package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.mcm;

/** Interface for logging events to MCM, and reading them.
 * @deprecated */
public interface MCMPortalInterfaceStatistics {

    public abstract String getStatisticsSession();

    public abstract String getStatisticsObjectSession(String sessionID,
            String mcmObjectID);

    public abstract String logPlayDuration(String sessionID,
            String objectSessionID, long startedAt, long endedAt);

}