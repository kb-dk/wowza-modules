package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOutputException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMSessionAndFilenameValidater;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.SessionAndFilenameValidaterIF;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Call CHAOS to see if session is valid and lookup object from the GUID.
 */
public class ChaosV6SessionAndFilenameValidater extends MCMSessionAndFilenameValidater {

    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private ChaosV6API chaosV6API;

    /**
     * Reads server connection configuration from property-file. Property file
     * is expected to be at "<VHost_HOME>/<propertyFilePath>"
     *
     * Example of content in property file could be:
     *
     * GeneralChaosV6ServerURL=http://api.stage.larm.fm/v6
     * ValidationChaosV6ValidationMethod=LarmFile/CanAccess
     *
     * @throws FileNotFoundException if property file is not found
     * @throws IOException           if reading process failed
     */
    public ChaosV6SessionAndFilenameValidater(WMSLogger logger, String connectionURLString,
                                              String validationMethodAtServer, ChaosV6API chaosV6API) {
        super(logger, connectionURLString, validationMethodAtServer);
        this.chaosV6API = chaosV6API;
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
        boolean isAllowedToStream;
        try (InputStream in = chaosV6API.larmValidateSession(sessionID, objectID, filenameAndPath)) {
            isAllowedToStream = parseResult(in);
        }
        if (!isAllowedToStream) {
            logger.warn("CHAOS validation. Did not validate right to play file.");
        }
        logger.debug("Validated stream using parameters sessionID=\" + sessionID + \", objectID=\"\n"
                             + "                            + objectID + \", filename=\" + filenameAndPath + \"");
        return isAllowedToStream;
    }

    /**
     * Check if the CHAOS result allows the stream to play, by looking for the "WasSuccess"-element in the output.
     * @param in The output from CHAOS.
     * @return Whether the "WasSuccess"-element contains true.
     * @throws MCMOutputException If the output cannot be parsed or does not contain a "WasSuccess"-element.
     */
    protected boolean parseResult(InputStream in) throws MCMOutputException {
        Document dom;
        try {
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            dom = db.parse(in);
        } catch (Exception e) {
            throw new MCMOutputException("Unable to parse CHAOS result", e);
        }
        NodeList nodelist = dom.getDocumentElement().getElementsByTagName("WasSuccess");
        if (nodelist != null && nodelist.getLength() > 0) {
            return Boolean.parseBoolean(nodelist.item(0).getFirstChild().getNodeValue().trim());
        } else {
            throw new MCMOutputException("No 'WasSuccess' element in CHAOS result");
        }
    }
}
