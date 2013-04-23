package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

public class MCMSessionAndFilenameValidaterTest {

	private Logger logger;
	
	public MCMSessionAndFilenameValidaterTest() {
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
	public void testValidateFilerequestWithMCMResultUnixStylePath() throws MCMOutputException, IOException {
		WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
		String pathAndFilename = "Kulturarv_MP3/Batch01/Disc02/mp3_128kbps/P1_0000_0200_910201_001.mp3";
		List<String> validatingFilenames = new ArrayList<String>();
		validatingFilenames.add("P1_0000_0200_910201_001.mp3");
		MCMSessionAndFilenameValidater validater = new MCMSessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer");
		boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);
		
		assertEquals("Filename validation:", true, doesValidate);
	}

	@Test
	public void testValidateFilerequestWithMCMResultWindowsStylePath() throws MCMOutputException, IOException {
		WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
		String pathAndFilename = "Kulturarv_MP3\\Batch01\\Disc02\\mp3_128kbps\\P1_0000_0200_910201_001.mp3";
		List<String> validatingFilenames = new ArrayList<String>();
		validatingFilenames.add("P1_0000_0200_910201_001.mp3");
		MCMSessionAndFilenameValidater validater = new MCMSessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer");
		boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);
		assertEquals("Filename validation:", true, doesValidate);
	}
}
