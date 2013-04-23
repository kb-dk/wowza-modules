package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MCMOReturnValueWrapper {

	protected Logger logger;
	protected boolean isSessionValid;
	protected String objectID;
	protected List<String> filenames;
	
	public MCMOReturnValueWrapper(Logger logger, InputStream inputStreamFromMCM) throws MCMOutputException {
		super();
		this.logger = logger;
		extractReturnValuesFromXML(inputStreamFromMCM);
	}

	protected void extractReturnValuesFromXML(InputStream mcmXMLOutputIS) throws MCMOutputException {
		try {
			Document dom = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(mcmXMLOutputIS);
			Element docEle = dom.getDocumentElement();
			extractReturnValuesForSession(docEle);
		} catch(ParserConfigurationException e) {
			throw new MCMOutputException("Could not parse MCM xml output.", e);
		} catch(SAXException e) {
			throw new MCMOutputException("Could not parse MCM xml output.", e);
		} catch(IOException e) {
			throw new MCMOutputException("Could not retrieve MCM output.", e);
		}
	}

	protected void extractReturnValuesForSession(Element docEle) throws MCMOutputException {
		String returnType = docEle.getNodeName();
		if (returnType.equals("ICollection")) {
			this.isSessionValid = true;
			this.objectID = extractStringContent(docEle, "ObjectID"); 
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

	protected String extractStringContent(Element docEle, String elementName) {
		Element folderPathElement = null;
		NodeList nodelist = docEle.getElementsByTagName(elementName);
		if(nodelist != null && nodelist.getLength() > 0) {
			folderPathElement = (Element)nodelist.item(0);
		}
		String stringContent = folderPathElement.getFirstChild().getNodeValue().trim();
		return stringContent;
	}

	protected List<String> extractMultipleElementsStringContent(Element docEle, String elementName) {
		List<String> extractedValues = new ArrayList<String>();
		NodeList nodelist = docEle.getElementsByTagName(elementName);
		for (int i=0;i<nodelist.getLength();i++) {
			Element element = (Element)nodelist.item(i);
			String stringContent = element.getFirstChild().getNodeValue().trim();
			extractedValues.add(stringContent);
		}
		return extractedValues;
	}
	
	public boolean isSessionValid() {
		return isSessionValid;
	}

	public String getObjectID() {
		return objectID;
	}

	public List<String> getFilenames() {
		return filenames;
	}
	
	public String toString() {
		String s = "";
		s += "ReturnValueWrapper. isValidSession=" + isSessionValid 
				+ ", objectID=" + objectID + ", filenames=" + filenames; 
		return s;
	}
	
}
