package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MCM2SessionAndFilenameValidaterTest {

	private Logger logger;

	public MCM2SessionAndFilenameValidaterTest() {
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
		MCM2SessionAndFilenameValidater validater = new MCM2SessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer");
		boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);
		
		assertEquals("Filename validation:", true, doesValidate);
	}

	@Test
	public void testValidateFilerequestWithMCMResultWindowsStylePath() throws MCMOutputException, IOException {
		WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
		String pathAndFilename = "Kulturarv_MP3\\Batch01\\Disc02\\mp3_128kbps\\P1_0000_0200_910201_001.mp3";
		List<String> validatingFilenames = new ArrayList<String>();
		validatingFilenames.add("P1_0000_0200_910201_001.mp3");
		MCM2SessionAndFilenameValidater validater = new MCM2SessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer");
		boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);
		assertEquals("Filename validation:", true, doesValidate);
	}
}
