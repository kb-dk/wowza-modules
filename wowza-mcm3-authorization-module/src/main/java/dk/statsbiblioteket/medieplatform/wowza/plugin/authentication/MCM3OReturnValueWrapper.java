package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOReturnValueWrapper;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOutputException;

import java.io.InputStream;

/**
 * Wrapper for a response from MCM3. Will update the state of the return value from the response ML document.
 */
public class MCM3OReturnValueWrapper extends MCMOReturnValueWrapper {
    public MCM3OReturnValueWrapper(Logger logger, InputStream inputStreamFromMCM) throws MCMOutputException {
        super(logger, inputStreamFromMCM);
    }

    /**
     * Get the result from the call.
     * If the call is successful, extracts object ID from the element GUID and file name from the element Filename.
     * @param docEle Returned XML document.
     * @throws MCMOutputException On trouble communicating or parsing.
     */
    @Override
    protected void extractReturnValuesForSession(Element docEle) throws MCMOutputException {
        String returnType = docEle.getNodeName();
        if (returnType.equals("PortalResult")) {
            NodeList error = docEle.getElementsByTagName("Error");
            if (error.getLength() ==  0) {
                this.isSessionValid = true;
                this.objectID = extractStringContent(docEle, "ObjectGuid");
                // Extract filename and path from MCM output
                this.filenames = extractMultipleElementsStringContent(docEle, "Filename");
            } else {
                this.isSessionValid = false;
                this.objectID = null;
                this.filenames = null;
                logger.warn("Error returned from MCM.");
            }
        } else {
            throw new MCMOutputException("Unexpected return value from MCM. Root element was: " + returnType);
        }
    }
}
