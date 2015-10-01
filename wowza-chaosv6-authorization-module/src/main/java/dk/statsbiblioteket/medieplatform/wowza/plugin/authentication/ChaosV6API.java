package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import com.wowza.wms.logging.WMSLogger;

import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOutputException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.StringAndTextUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Methods used when calling the CHAOS v6 API
 */
public class ChaosV6API {

    private String connectionURLString;
    private String validationMethodAtServer;
    private WMSLogger logger;

    public ChaosV6API(String connectionURLString, String validationMethodAtServer, WMSLogger logger) {
        this.connectionURLString = connectionURLString;
        this.validationMethodAtServer = validationMethodAtServer;
        this.logger = logger;
    }

    /**
     * Call chaos to check session is valid. NOTE: In debug mode the call is made twice.
     *
     * @param sessionID CHAOS Session ID
     * @param objectID CHAOS Object ID
     * @param filePath Path of file
     * @return The output from CHAOS.
     *
     * @throws IOException On trouble connection to CHAOS
     * @throws MalformedURLException On bad URL connecting to CHAOS
     */
    public InputStream larmValidateSession(String sessionID, String objectID, String filePath)
            throws IOException, MalformedURLException, MCMOutputException {
        String urlStringToCHAOS = this.connectionURLString + "/" + this.validationMethodAtServer + "?" + "sessionGUID=" + sessionID
                + "&" + "objectId=" + objectID + "&" + "filePath=" + URLEncoder.encode(filePath, "UTF-8");

        InputStream in;
        if (logger.isDebugEnabled()) {
            logger.debug("CHAOS URL:" + urlStringToCHAOS);
            InputStream inDebug = new URL(urlStringToCHAOS).openConnection().getInputStream();
            logger.debug("Returned from CHAOS: " + StringAndTextUtil.convertStreamToString(inDebug));
        }
        in = new URL(urlStringToCHAOS).openConnection().getInputStream();
        return in;
    }
}
