package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MCMSessionAndFilenameValidater implements SessionAndFilenameValidaterIF {
    protected WMSLogger logger;
    protected String connectionURLString;
    protected String validationMethodAtServer;

    /**
     * Only to be called by subclasses, that must initialize logger, connectionURLString and validationMethodAtServer.
     */
    protected MCMSessionAndFilenameValidater() {
    }

    /**
     * Reads server connection configuration from arguments.
     *
     * Example of server-arguments could be:
     *
     * urlString: "http://web.server00.geckon.com/portal/api/portalservice.svc"
     * method: "Object_Get"
     */
    public MCMSessionAndFilenameValidater(WMSLogger logger, String connectionURLString,
                                          String validationMethodAtServer) {
        super();
        this.logger = logger;
        this.connectionURLString = connectionURLString;
        this.validationMethodAtServer = validationMethodAtServer;
        if (connectionURLString == null || validationMethodAtServer == null) {
            throw new RuntimeException(
                    "Missing properties." + " At least one of the properties not set: " + "connectionURL="
                            + this.connectionURLString + " and " + "validationMethodAtServer="
                            + this.validationMethodAtServer);
        }
    }

    /* (non-Javadoc)
      * @see dk.statsbiblioteket.larm.wowza.plugin.authentication.model.SessionAndFilenameValidater#validateRightsToPlayFile(java.lang.String, java.lang.String, java.lang.String)
      */
    @Override
    public boolean validateRightsToPlayFile(String sessionID, String objectID, String filenameAndPath)
            throws MalformedURLException, IOException, MCMOutputException {
        if (sessionID == null || objectID == null || filenameAndPath == null) {
            throw new IllegalArgumentException(
                    "WARNING: At least one of the arguments is null: " + "(sessionID=" + sessionID + ", objectID="
                            + objectID + ", filename=" + filenameAndPath + ")");
        }
        MCMOReturnValueWrapper returnValues = getInputFromMCM(sessionID, objectID);
        boolean isSessionValid = returnValues.isSessionValid();
        boolean objectIDEquals = objectID.equals(returnValues.getObjectID());
        boolean filenameEquals = validateFilerequestWithMCMResult(filenameAndPath, returnValues.getFilenames());
        boolean isAllowedToStream = isSessionValid && objectIDEquals && filenameEquals;
        if (!isAllowedToStream) {
            logger.warn("MCM validation. Did not validate right to play file. MCM return: " + returnValues
                                + ". Fail criteria " + "- Is session valid: " + isSessionValid + ". Equal object ids: "
                                + objectIDEquals + ". Equal filenames: " + filenameEquals);
        }
        logger.debug("MCM return values " + returnValues.toString());
        logger.debug("Stream filename : " + filenameAndPath);
        logger.debug("MCM filenames   : " + returnValues.getFilenames());
        return isAllowedToStream;
    }

    protected boolean validateFilerequestWithMCMResult(String requestedPathAndFilename, List<String> mcmFilenames) {
        // Defualt with unix style path separator
        String filename = getFilenameUsingSeparator(requestedPathAndFilename, "/");
        boolean filenameEquals = mcmFilenames.contains(filename);
        if (!filenameEquals) {
            // Try with windows style path separator
            filename = getFilenameUsingSeparator(requestedPathAndFilename, "\\");
            filenameEquals = mcmFilenames.contains(filename);
        }
        logger.debug("Stream file   : " + filename);
        logger.debug("MCM filenames : " + mcmFilenames);
        return filenameEquals;
    }

    protected String getFilenameUsingSeparator(String requestedPathAndFilename, String separator) {
        int sep = requestedPathAndFilename.lastIndexOf(separator);
        String filename = requestedPathAndFilename.substring(sep + 1);
        logger.debug("Path separator: " + separator);
        return filename;
    }

    protected MCMOReturnValueWrapper getInputFromMCM(String sessionID, String objectID)
            throws IOException, MalformedURLException, MCMOutputException {
        String urlStringToMCM = connectionURLString + "/" + validationMethodAtServer + "?" + "sessionID=" + sessionID
                + "&" + "objectID=" + objectID + "&" + "includeFiles=true";
        InputStream in = new URL(urlStringToMCM).openConnection().getInputStream();
        if (logger.isDebugEnabled()) {
            logger.debug("MCM URL:" + urlStringToMCM);
            InputStream inDebug = new URL(urlStringToMCM).openConnection().getInputStream();
            logger.debug("Returned from MCM: " + StringAndTextUtil.convertStreamToString(inDebug));
        }
        return new MCMOReturnValueWrapper(logger, in);
    }
}
