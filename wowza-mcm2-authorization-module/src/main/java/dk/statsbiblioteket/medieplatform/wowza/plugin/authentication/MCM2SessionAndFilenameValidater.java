package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOReturnValueWrapper;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOutputException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMSessionAndFilenameValidater;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Call MCM to see if session is valid and lookup object from the GUID.
 */
public class MCM2SessionAndFilenameValidater extends MCMSessionAndFilenameValidater {
    /**
     * Reads server connection configuration from property-file. Property file
     * is expected to be at "<VHost_HOME>/<propertyFilePath>"
     *
     * Example of content in property file could be:
     *
     * GeneralMCM2ServerURL=api.test.chaos-systems.com/
     * ValidationMCM2ValidationMethod=Object/Get
     *
     * @throws FileNotFoundException if property file is not found
     * @throws IOException           if reading process failed
     */
    public MCM2SessionAndFilenameValidater(WMSLogger logger, IApplicationInstance appInstance,
                                           String connectionURLString, String validationMethodAtServer)
            throws FileNotFoundException, IOException {
        super();
        this.logger = logger;
        this.connectionURLString = connectionURLString;
        this.validationMethodAtServer = validationMethodAtServer;
    }

    public MCM2SessionAndFilenameValidater(WMSLogger logger, String connectionURLString,
                                           String validationMethodAtServer) {
        super(logger, connectionURLString, validationMethodAtServer);
    }

    /**
     * Get session validation information from MCM2.
     * @param sessionID MCM2 Session ID
     * @param objectID MCM2 Object GUID
     * @return Object containing information about session validity and file names for GUID.
     * @throws IOException On trouble connection to MCM2
     * @throws MalformedURLException On bad URL connecting to MCM2
     * @throws MCMOutputException On trouble reading MCM2 output.
     */
    @Override
    protected MCMOReturnValueWrapper getInputFromMCM(String sessionID, String objectID)
            throws IOException, MalformedURLException, MCMOutputException {
        String urlStringToMCM = connectionURLString + "/" + validationMethodAtServer + "?" + "sessionGUID=" + sessionID
                + "&" + "query=GUID:" + objectID + "&" + "includeFiles=true" + "&" + "pageSize=1";
        InputStream in = new URL(urlStringToMCM).openConnection().getInputStream();
        if (logger.isDebugEnabled()) {
            logger.debug("MCM URL:" + urlStringToMCM);
            InputStream inDebug = new URL(urlStringToMCM).openConnection().getInputStream();
            logger.debug("Returned from MCM: " + StringAndTextUtil.convertStreamToString(inDebug));
        }
        return new MCM2OReturnValueWrapper(logger, in);
    }
}
