package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOReturnValueWrapper;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model.MCMOutputException;

import java.io.InputStream;

public class MCM2OReturnValueWrapper extends MCMOReturnValueWrapper {
    public MCM2OReturnValueWrapper(Logger logger, InputStream inputStreamFromMCM) throws MCMOutputException {
        super(logger, inputStreamFromMCM);
    }

    protected void extractReturnValuesForSession(Element docEle) throws MCMOutputException {
        String returnType = docEle.getNodeName();
        if (returnType.equals("PortalResult")) {
            this.isSessionValid = true;
            this.objectID = extractStringContent(docEle, "GUID");
            // Extract filename and path from MCM output
            this.filenames = extractMultipleElementsStringContent(docEle, "Filename"); 
        } else if (returnType.equals("Exception")) {
            this.isSessionValid = false;
            this.objectID = null;
            this.filenames = null;
            logger.warn("Exception returned from MCM.");
        } else {
            throw new MCMOutputException("Unexpected return value from MCM. Root element was: " + returnType);
        }
    }
}
