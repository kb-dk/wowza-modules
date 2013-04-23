package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

import com.wowza.wms.logging.WMSLoggerFactory;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MCM2OReturnValueWrapperTest {

	private static String filenameOfvalidMCMOutputFull =
		"src/test/resources/test_data_MCM2_output_full.xml";
	private static String filenameOfvalidMCMOutputInvalidSession =
		"src/test/resources/test_data_MCM_output_invalid_session.xml";
	private static String filenameOfInvalidMCMOutput =
		"src/test/resources/test_data_invalid_output.xml";

	private Logger logger;

	public MCM2OReturnValueWrapperTest() {
		super();
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
	}
	
	@Before
	public void setUp() throws Exception {
		org.apache.log4j.BasicConfigurator.configure();
	}

	@After
	public void tearDown() throws Exception {
		org.apache.log4j.BasicConfigurator.resetConfiguration();
	}

	@Test
	public void testExtractOutputFilename() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
		MCM2OReturnValueWrapper returnWrapper = new MCM2OReturnValueWrapper(logger, is);
		String returnedValue = returnWrapper.getFilenames().get(0);
		String expectedValue = "P3_2000_2200_890622_001.mp3";
		assertEquals("Filename", expectedValue, returnedValue);
	}

	@Test
	public void testExtractOutputObjectID() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
		MCM2OReturnValueWrapper returnWrapper = new MCM2OReturnValueWrapper(logger, is);
		String returnedValue = returnWrapper.getObjectID();
		String expectedValue = "368c6661-3db2-0344-bdf8-0f7d9d98f36f";
		assertEquals("ObjectID", expectedValue, returnedValue);
	}

	@Test
	public void testExtractOutputInvalidSessionID() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputInvalidSession);
		MCM2OReturnValueWrapper returnWrapper = new MCM2OReturnValueWrapper(logger, is);
		boolean returnedValue = returnWrapper.isSessionValid();
		boolean expectedValue = false; 
		assertEquals("Valid session", expectedValue, returnedValue);
	}

	@Test(expected=MCMOutputException.class)
	public void testExtractOutputBogusXML() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfInvalidMCMOutput);
		new MCM2OReturnValueWrapper(logger, is);
	}

	@Test
	public void testExtractMultipleFilenames() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
		MCM2OReturnValueWrapper returnWrapper = new MCM2OReturnValueWrapper(logger, is);
		List<String> returnedFilenames = returnWrapper.getFilenames();
		String expectedValue1 = "P3_2000_2200_890622_001.mp3";
		String expectedValue2 = "P3_logo.png";
		assertTrue("Filename", returnedFilenames.contains(expectedValue1));
		assertTrue("Filename", returnedFilenames.contains(expectedValue2));
	}

	private InputStream getTestDataFileAsInputStream(String inputstring) throws FileNotFoundException {
		FileInputStream fis;
		try {
			// If run from ant in command line
			fis = new FileInputStream(inputstring);
		} catch (FileNotFoundException e) {
			// If run from Eclipse
			fis = new FileInputStream("trunk/" + inputstring);
		}
		return fis;
	}
}
