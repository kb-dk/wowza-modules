package dk.statsbiblioteket.doms.wowza.plugin.domslive.model;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import dk.statsbiblioteket.doms.wowza.plugin.domslive.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.domslive.mockobjects.IMediaStreamMock;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;


public class DomsUriToFileMapperTest {

	private WMSLogger wmsLogger;

	private String validRequestedURI = "DR1_2010-03-14-18-30-00_2010-03-14-19-00-00";
	private String validPath = "<storageDir>";  //"/usr/local/WowzaMediaServer/content";
	private String validFilename = validRequestedURI + ".mp4"; //"DR1_2010-03-14_18-30_2010-03-14_19-00.mp4";


	public DomsUriToFileMapperTest() {
		super();
		this.wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
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
	public void testExtractFilenameValidRequest() throws InvalidURIException {
		DomsUriToFileMapper mapper = new DomsUriToFileMapper("<storageDir>", "");
		String filename = mapper.extractFilename(validRequestedURI, "mp4");
		String expectedResult = validFilename;
		assertEquals("Filename not expected.", expectedResult, filename);
	}

	@Test
	public void testExtractFilenameInvalidURI() {
		DomsUriToFileMapper mapper = new DomsUriToFileMapper("<storageDir>", "");
		String filename;
		try {
			filename = mapper.extractFilename("invalid_URI", "mp4");
			fail("Exception is expected to be thrown");
		} catch (InvalidURIException e) {
			String expectedMessage = "URI is not on the form <channel>_<from-date>_<to-date>";
			assertEquals("Expected to fail with reason:", expectedMessage, e.getMessage());
		}
	}

	@Test
	public void testExtractFilenameInvalidDateInURI() {
		DomsUriToFileMapper mapper = new DomsUriToFileMapper("<storageDir>", "");
		String fromDateString = "2010-01-31-00-00-00";
		String toDateString = "2010-15-32-00-30-00";
		String uri = "DR1_" + fromDateString + "_" + toDateString;
		String filename;
		try {
			filename = mapper.extractFilename(uri, "mp4");
			fail("Exception is expected to be thrown");
		} catch (InvalidURIException e) {
			String expectedMessage = "Elements of the URI are not of the expected format. URI was: DR1_2010-01-31-00-00-00_2010-15-32-00-30-00";
			assertEquals("Expected to fail with reason:", expectedMessage, e.getMessage());
			Throwable cause = e.getCause();
			String expectedCauseMessage = "Date is not valid. Read: 2010-15-32-00-30-00. Interpreted: 2011-04-01-00-30-00";
			assertEquals("Expected to fail with cause:", expectedCauseMessage, cause.getMessage());
		}
	}

	@Test
	public void testValidateStringAsDateValidDate() throws ParseException {
		DomsUriToFileMapper mapper = new DomsUriToFileMapper("<storageDir>", "");
		String inputDateString = "2010-01-31-00-00-00";
		mapper.validateStringAsDate(inputDateString);
		// No exception should be thrown
	}

	@Test
	public void testValidateStringAsDateInvalidDate() {
		DomsUriToFileMapper mapper = new DomsUriToFileMapper("<storageDir>", "");
		String inputDateString = "2010-01-32-00-00-00";
			try {
				mapper.validateStringAsDate(inputDateString);
				fail("Must not accept invalid date: " + inputDateString);
			} catch (ParseException e) {
				String expected = "Date is not valid. Read: 2010-01-32-00-00-00. Interpreted: 2010-02-01-00-00-00";
				assertEquals("Expecting to fail validation.", expected, e.getMessage());
			}

		//
	}

	@Test
	public void testStreamToFileForReadIMediaStream() {
		DomsUriToFileMapper mapper = new DomsUriToFileMapper("<storageDir>", "");
		wmsLogger.info("The input request: " + validRequestedURI);
		IClientMock iClient = new IClientMock(wmsLogger, validRequestedURI);
		IMediaStreamMock stream = new IMediaStreamMock(wmsLogger, "Stream name", iClient);
		wmsLogger.info("Stream query: " + stream.getQueryStr());
		wmsLogger.info("Client query: " + iClient.getQueryStr());
		File fileToStream = mapper.streamToFileForRead(stream);
		assertNotNull("File to stream must not be null", fileToStream);
		String expectedResult = validFilename;
		assertEquals("Unexpected filename.", expectedResult, fileToStream.getName());
	}

	@Test
	public void testStreamToFileForReadIMediaStreamWithArguments() {
		DomsUriToFileMapper mapper = new DomsUriToFileMapper("<storageDir>", "");
		IClientMock iClient = new IClientMock(wmsLogger, validRequestedURI);
		IMediaStreamMock stream = new IMediaStreamMock(wmsLogger, "Stream name", iClient);
		File fileToStream = mapper.streamToFileForRead(stream,
														"name", "ext", "request");
		assertNotNull("File to stream must not be null", fileToStream);
		String expectedResult = validFilename;
		assertEquals("Unexpected filename.", expectedResult, fileToStream.getName());
	}
}