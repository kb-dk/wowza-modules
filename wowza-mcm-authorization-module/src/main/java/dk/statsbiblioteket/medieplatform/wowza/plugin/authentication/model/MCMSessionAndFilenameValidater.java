package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Check if an MCM SessionID and ObjectID is valid for playing a file.
 */
public class MCMSessionAndFilenameValidater implements SessionAndFilenameValidaterIF {
    /** The wowza logger */
    protected WMSLogger logger;
    /** The connection string to MCM */
    protected String connectionURLString;
    /** The MCM method to call */
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

    /**
     * Validate rights to play file.
     *
     * @see SessionAndFilenameValidaterIF#validateRightsToPlayFile(java.lang.String, java.lang.String, java.lang.String)
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

    /**
     * Helper method to compare local file name with MCM file names.
     * Will accept the requested filename, if any of the MCM file names match the requested filename, with path
     * removed from the Wowza path.
     *
     * @param requestedPathAndFilename The path and file name requested by Wowza
     * @param mcmFilenames List of filenames extracted from MCM
     * @return True, of requested filename with path removed is contained in MCM filenames, false otherwise.
     */
    protected boolean validateFilerequestWithMCMResult(String requestedPathAndFilename, List<String> mcmFilenames) {
        boolean filenameEquals  = false;
        for (String f : mcmFilenames) {
            if (f.equals(requestedPathAndFilename) || cleanFilename(f).equals(cleanFilename(requestedPathAndFilename))) {
                filenameEquals = true;
                break;
            }
        }
        logger.debug("Stream file   : " + requestedPathAndFilename);
        logger.debug("MCM filenames : " + mcmFilenames);
        return filenameEquals;
    }

    /**
     * Remove leading directories and trailing extension from a filename.
     * <p/>
     * This means:
     * <ul>
     *     <li>c:\test\file.mp3 -> file</li>
     *     <li>/usr/local/file.flv -> file</li>
     * </ul>
     *
     * @param f The file to clean
     * @return The file with leading directories and trailing extension removed.
     */
    protected String cleanFilename(String f) {
        // Remove leading directories (unix style)
        f = f.substring(f.lastIndexOf("/") + 1);
        // Remove leading directories (windows style)
        f = f.substring(f.lastIndexOf("\\") + 1);
        // Remove trailing extension
        if (f.contains(".")) {
            f = f.substring(0, f.lastIndexOf('.'));
        }
        return f;
    }

    /**
     * Connect to MCM and read object information with the given sessionID and objectID.
     * @param sessionID SessionID to use when connecting to MCM
     * @param objectID ObjectID to use when connecting to MCM
     * @return The result in a wrapped class,
     * @throws IOException On trouble communicating.
     * @throws MalformedURLException On troulbe with connection URL
     * @throws MCMOutputException On any trouble reading or understanding MCM return values.
     */
    protected MCMOReturnValueWrapper getInputFromMCM(String sessionID, String objectID)
            throws IOException, MalformedURLException, MCMOutputException {
        String urlStringToMCM = connectionURLString + "/" + validationMethodAtServer + "?" + "sessionID=" + sessionID
                + "&" + "objectID=" + objectID + "&" + "includeFiles=true";
        if (logger.isDebugEnabled()) {
            logger.debug("MCM URL:" + urlStringToMCM);
            InputStream inDebug = new URL(urlStringToMCM).openConnection().getInputStream();
            logger.debug("Returned from MCM: " + StringAndTextUtil.convertStreamToString(inDebug));
        }
        InputStream in = new URL(urlStringToMCM).openConnection().getInputStream();
        return new MCMOReturnValueWrapper(logger, in);
    }
}
