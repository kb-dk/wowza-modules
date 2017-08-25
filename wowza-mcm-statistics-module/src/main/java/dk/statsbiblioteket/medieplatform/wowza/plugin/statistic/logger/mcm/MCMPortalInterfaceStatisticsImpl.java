package dk.statsbiblioteket.medieplatform.wowza.plugin.statistic.logger.mcm;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Functionality for logging events to MCM, and reading them.
 * @deprecated */
public class MCMPortalInterfaceStatisticsImpl implements MCMPortalInterfaceStatistics {

    private static MCMPortalInterfaceStatistics instance = null;
    private final WMSLogger logger;

    private String mcmConnectionURLString;
    private String mcmStatisticsMethodCreateStatSession;
    private String clientSettingID;
    private String repositoryID;
    private String mcmStatisticsMethodCreateStatObjectSession;
    private String objectTypeID;
    private String channelTypeID;
    private String channelIdentifier;
    private String objectTitle;
    private String eventTypeID;
    private String objectCollectionID;
    private String mcmStatisticsMethodCreateDurationSession;

    private MCMPortalInterfaceStatisticsImpl(WMSLogger logger, String mcmConnectionURLString,
                                             String mcmStatisticsMethodCreateStatSession, String clientSettingID,
                                             String repositoryID, String mcmStatisticsMethodCreateStatObjectSession,
                                             String objectTypeID, String channelTypeID, String channelIdentifier,
                                             String objectTitle, String eventTypeID, String objectCollectionID,
                                             String mcmStatisticsMethodCreateDurationSession) {
        super();
        this.logger = logger;
        this.mcmConnectionURLString = mcmConnectionURLString;
        this.mcmStatisticsMethodCreateStatSession = mcmStatisticsMethodCreateStatSession;
        this.clientSettingID = clientSettingID;
        this.repositoryID = repositoryID;
        this.mcmStatisticsMethodCreateStatObjectSession = mcmStatisticsMethodCreateStatObjectSession;
        this.objectTypeID = objectTypeID;
        this.channelTypeID = channelTypeID;
        this.channelIdentifier = channelIdentifier;
        this.objectTitle = objectTitle;
        this.eventTypeID = eventTypeID;
        this.objectCollectionID = objectCollectionID;
        this.mcmStatisticsMethodCreateDurationSession = mcmStatisticsMethodCreateDurationSession;
    }

    /**
     * Creates the singleton objects. Is robust for multiple concurrent requests for create.
     * Only the first request for create, actually creates the object.
     * @param logger logger 
     * @param vHostHomeDirPath vHostHomeDirPath
     * @param mcmConnectionURLString mcmConnectionURLString
     * @param mcmStatisticsMethodCreateStatSession mcmStatisticsMethodCreateStatSession 
     * @param clientSettingID clientSettingID
     * @param repositoryID repositoryID
     * @param mcmStatisticsMethodCreateStatObjectSession mcmStatisticsMethodCreateStatObjectSession
     * @param objectTypeID objectTypeID
     * @param channelTypeID channelTypeID
     * @param channelIdentifier channelIdentifier
     * @param objectTitle objectTitle
     * @param eventTypeID eventTypeID
     * @param objectCollectionID objectCollectionID
     * @param mcmStatisticsMethodCreateDurationSession mcmStatisticsMethodCreateDurationSession
     * @throws java.io.FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    public static synchronized void createInstance(WMSLogger logger, String vHostHomeDirPath,
                                                   String mcmConnectionURLString,
                                                   String mcmStatisticsMethodCreateStatSession, String clientSettingID,
                                                   String repositoryID,
                                                   String mcmStatisticsMethodCreateStatObjectSession,
                                                   String objectTypeID, String channelTypeID, String channelIdentifier,
                                                   String objectTitle, String eventTypeID, String objectCollectionID,
                                                   String mcmStatisticsMethodCreateDurationSession) throws FileNotFoundException, IOException {
        if ((logger == null) || (vHostHomeDirPath == null)) {
            throw new IllegalArgumentException("A parameter is null. " +
                    "logger=" + logger + " " +
                    "vHostHomeDirPath=" + vHostHomeDirPath);
        }
        if (instance == null) {
            logger.debug("Creating MCMPortalInterfaceStatistics");
            instance = new MCMPortalInterfaceStatisticsImpl(logger, mcmConnectionURLString,
                                                            mcmStatisticsMethodCreateStatSession, clientSettingID,
                                                            repositoryID, mcmStatisticsMethodCreateStatObjectSession,
                                                            objectTypeID, channelTypeID, channelIdentifier, objectTitle,
                                                            eventTypeID, objectCollectionID,
                                                            mcmStatisticsMethodCreateDurationSession);
        }
    }

    public static synchronized void createInstanceForTestPurpose(MCMPortalInterfaceStatistics testInstance) {
        instance = testInstance;
    }
    
    public static synchronized MCMPortalInterfaceStatistics getInstance() {
        return instance;
    }

    @Override
    public String getStatisticsSession() {
        String urlStringToMCM = mcmConnectionURLString + "/" + mcmStatisticsMethodCreateStatSession + "?"
            + "clientSettingID=" + clientSettingID + "&"
            + "repositoryID=" + repositoryID;
        try {
            InputStream in = new URL(urlStringToMCM).openConnection().getInputStream();
            String returnValueFromMCM = StringAndTextUtil.convertStreamToString(in);
            String sessionID = null;
            String[] parts = returnValueFromMCM.split("<SessionID>");
            if (parts.length > 0) {
                parts = parts[1].split("</SessionID>");
                sessionID = parts[0];
            }
            logger.info("Session id: " + sessionID);
            if (sessionID==null) {
                throw new RuntimeException("Could not infer sessionID from return value: " + returnValueFromMCM);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("MCM URL:" + urlStringToMCM);
                logger.debug("Returned from MCM: " + returnValueFromMCM);
            }
            return sessionID;
        } catch (IOException e) {
            throw new RuntimeException("Error contacting server: " + urlStringToMCM, e);
        }
    }

    @Override
    public String getStatisticsObjectSession(String sessionID, String mcmObjectID) {
        String urlStringToMCM = mcmConnectionURLString + "/" + mcmStatisticsMethodCreateStatObjectSession + "?"
            + "objectTypeID=" + objectTypeID + "&"
            + "channelTypeID=" + channelTypeID + "&"
            + "channelIdentifier=" + channelIdentifier + "&"
            + "objectTitle=" + objectTitle + "&"
            + "eventTypeID=" + eventTypeID + "&"
            + "objectCollectionID=" + objectCollectionID + "&"
            + "objectIdentifier=" + mcmObjectID + "&"
            + "sessionID=" + sessionID;
        try {
            InputStream in = new URL(urlStringToMCM).openConnection().getInputStream();
            String returnValueFromMCM = StringAndTextUtil.convertStreamToString(in);
            String objectSessionID = returnValueFromMCM; 
            logger.info("MCM returnvalue objectSessionID: " + objectSessionID);
            if (logger.isDebugEnabled()) {
                logger.debug("MCM URL:" + urlStringToMCM);
                logger.debug("Returned from MCM: " + returnValueFromMCM);
            }
            return objectSessionID;
        } catch (IOException e) {
            throw new RuntimeException("Error contacting server: " + urlStringToMCM, e);
        }
    }

    @Override
    public String logPlayDuration(String sessionID, String objectSessionID, long startedAd, long endedAt) {
        String urlStringToMCM = mcmConnectionURLString + "/" + mcmStatisticsMethodCreateDurationSession + "?"
            + "sessionID=" + sessionID + "&"
            + "objectSessionID=" + objectSessionID + "&"
            + "startedAt=" + startedAd + "&"
            + "endedAt=" + endedAt;
        try {
            InputStream in = new URL(urlStringToMCM).openConnection().getInputStream();
            String returnValueFromMCM = StringAndTextUtil.convertStreamToString(in);
            if (logger.isDebugEnabled()) {
                logger.debug("MCM URL:" + urlStringToMCM);
                logger.debug("Returned from MCM: " + returnValueFromMCM);
            }
            return returnValueFromMCM;
        } catch (IOException e) {
            throw new RuntimeException("Error contacting server: " + urlStringToMCM, e);
        }
    }
}
