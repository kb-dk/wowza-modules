package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.chaos.wowza.plugin.util.PropertiesUtil;
import dk.statsbiblioteket.chaos.wowza.plugin.util.StringAndTextUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MCM2SessionAndFilenameValidater extends MCMSessionAndFilenameValidater {
    private static final String propertyMCMServerURLKey = "GeneralMCM2ServerURL";
    private static final String propertyMCMValidationMethod = "ValidationMCM2ValidationMethod";


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
    public MCM2SessionAndFilenameValidater(WMSLogger logger, IApplicationInstance appInstance)
            throws FileNotFoundException, IOException {
        super();
        this.logger = logger;
        String vhostDir = appInstance.getVHost().getHomePath();
        logger.info("MCMSessionAndFilenameValidater - VHost home path: " + vhostDir);
        PropertiesUtil.loadProperties(this.logger, vhostDir,
                                      new String[]{propertyMCMServerURLKey, propertyMCMValidationMethod});
        this.connectionURLString = PropertiesUtil.getProperty(propertyMCMServerURLKey);
        this.validationMethodAtServer = PropertiesUtil.getProperty(propertyMCMValidationMethod);
    }

    /**
     * Reads server connection configuration from property-file. Property file
     * is expected to be at "<vhosDir>/conf/chaos/chaos-streaming-server-plugin.properties"
     *
     * Example of content in property file could be:
     *
     * GeneralMCM2ServerURL=api.test.chaos-systems.com/
     * ValidationMCM2ValidationMethod=Object/Get
     *
     * @throws FileNotFoundException if property file is not found
     * @throws IOException           if reading process failed
     */
    public MCM2SessionAndFilenameValidater(WMSLogger wmsLogger, String wowzaSystemDir)
            throws FileNotFoundException, IOException {
        super();
        this.logger = wmsLogger;
        PropertiesUtil.loadProperties(logger, wowzaSystemDir,
                                      new String[]{propertyMCMServerURLKey, propertyMCMValidationMethod});
        this.connectionURLString = PropertiesUtil.getProperty(propertyMCMServerURLKey);
        this.validationMethodAtServer = PropertiesUtil.getProperty(propertyMCMValidationMethod);
    }

    public MCM2SessionAndFilenameValidater(WMSLogger logger, String connectionURLString,
                                           String validationMethodAtServer) {
        super(logger, connectionURLString, validationMethodAtServer);
    }

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
