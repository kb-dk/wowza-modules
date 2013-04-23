package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
