package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

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

public class MCMOReturnValueWrapperTest {

	private String filenameOfvalidMCMOutputFull = getClass().getClassLoader().getResource(
		"test_data_MCM_output_full.xml").getPath();
	private String filenameOfvalidMCMOutputMultipleFiles = getClass().getClassLoader().getResource(
		"test_data_MCM_output_multiple_files.xml").getPath();
	private String filenameOfvalidMCMOutputInvalidSession = getClass().getClassLoader().getResource(
		"test_data_MCM_output_invalid_session.xml").getPath();
	private String filenameOfInvalidMCMOutput = getClass().getClassLoader().getResource(
		"test_data_invalid_output.xml").getPath();

	private Logger logger;

	public MCMOReturnValueWrapperTest() {
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
		MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
		String returnedValue = returnWrapper.getFilenames().get(0);
		String expectedValue = "P1_0000_0200_910201_001.mp3"; 
		assertEquals("Filename", expectedValue, returnedValue);
	}

	@Test
	public void testExtractOutputObjectID() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
		MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
		String returnedValue = returnWrapper.getObjectID();
		String expectedValue = "643703"; 
		assertEquals("ObjectID", expectedValue, returnedValue);
	}

	@Test
	public void testExtractOutputInvalidSessionID() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputInvalidSession);
		MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
		boolean returnedValue = returnWrapper.isSessionValid();
		boolean expectedValue = false; 
		assertEquals("Valid session", expectedValue, returnedValue);
	}

	@Test(expected=MCMOutputException.class)
	public void testExtractOutputBogusXML() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfInvalidMCMOutput);
		new MCMOReturnValueWrapper(logger, is);
	}

	@Test
	public void testExtractMultipleFilenames() throws FileNotFoundException, MCMOutputException {
		InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputMultipleFiles);
		MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
		List<String> returnedFilenames = returnWrapper.getFilenames();
		String expectedValue1 = "P2_1800_2000_890121_001.mp3"; 
		String expectedValue2 = "P2_2000_2200_890121_001.mp3";
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
