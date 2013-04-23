package dk.statsbiblioteket.chaos.wowza.plugin.authentication;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;

import dk.statsbiblioteket.chaos.wowza.plugin.authentication.StreamAuthenticater;
import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.chaos.wowza.plugin.mockobjects.SessionAndFilenameValidaterMock;

public class StreamAuthenticaterTest {

	private WMSLogger logger;
	private StreamAuthenticater streamAuthenticater;
	
	private String validSessionID;
	private String invalidSessionID;
	private String validObjectID;
	private String invalidObjectID;
	private String validFilename;
	private String invalidFilename;

	public StreamAuthenticaterTest() throws FileNotFoundException, IOException {
		super();
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
		this.streamAuthenticater = new StreamAuthenticater(logger, new SessionAndFilenameValidaterMock());
		this.validSessionID = "5F95E509-FD84-4570-9382-FEC5481E342F";
		this.invalidSessionID = "E32262ED-21A8-46CB-BDA3-FF8D284DAC48";
		this.validObjectID = "976";
		this.invalidObjectID = "977";
		this.validFilename = new File("P3_2000_2200_890325_001.mp3").getAbsolutePath();
		this.invalidFilename = new File("P3_2000_2200_890325_001_invalid.mp3").getAbsolutePath();
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
	public void testCheckAuthorizationEverythingOK() {
		
		boolean result = streamAuthenticater.checkAuthorization(validSessionID, validObjectID, validFilename, null);
		assertEquals("Input expected to be valid.", true, result);
	}

	@Test
	public void testCheckAuthorizationInvalidSessionID() {
		boolean result = streamAuthenticater.checkAuthorization(invalidSessionID, validObjectID, validFilename, null);
		assertEquals("Input is not expected to be valid.", false, result);
	}

	@Test
	public void testCheckAuthorizationInvalidObjectID() {
		boolean result = streamAuthenticater.checkAuthorization(validSessionID, invalidObjectID, validFilename, null);
		assertEquals("Input is not expected to be valid.", false, result);
	}

	@Test
	public void testCheckAuthorizationInvalidFilename() {
		boolean result = streamAuthenticater.checkAuthorization(validSessionID, validObjectID, invalidFilename, null);
		assertEquals("Input is not expected to be valid.", false, result);
	}

	@Test
	public void testOnPlayValidInput() {
		String queryString = "ObjectID=" + validObjectID + "&SessionID=" + validSessionID;
		IClientMock iClientM = new IClientMock(queryString);
		IMediaStream iMediaStream = new IMediaStreamMock(validFilename, iClientM); 
		streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
		assertEquals("Unexpected return value.", false, iClientM.getHasSetShutdownClientBeenCalled());
	}

	@Test
	public void testOnPlayWrongSession() {
		String queryString = "ObjectID=" + validObjectID + "&SessionID=" + invalidSessionID;
		IClientMock iClientM = new IClientMock(queryString);
		IMediaStream iMediaStream = new IMediaStreamMock(validFilename, iClientM); 
		streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
		assertEquals("Unexpected return value.", true, iClientM.getHasSetShutdownClientBeenCalled());
	}

	@Test
	public void testOnPlayWrongObject() {
		String queryString = "ObjectID=" + invalidObjectID + "&SessionID=" + validSessionID;
		IClientMock iClientM = new IClientMock(queryString);
		IMediaStream iMediaStream = new IMediaStreamMock(validFilename, iClientM); 
		streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
		assertEquals("Unexpected return value.", true, iClientM.getHasSetShutdownClientBeenCalled());
	}

	@Test
	public void testOnPlayWrongFilename() {
		String queryString = "ObjectID=" + invalidObjectID + "&SessionID=" + validSessionID;
		IClientMock iClientM = new IClientMock(queryString);
		IMediaStream iMediaStream = new IMediaStreamMock(invalidFilename, iClientM); 
		streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
		assertEquals("Unexpected return value.", true, iClientM.getHasSetShutdownClientBeenCalled());
	}

	@Test
	public void testOnPlayMalformedURLException() {
		String queryString = "ObjectID=" + "MalformedURLExceptionTrigger" + "&SessionID=" + validSessionID;
		IClientMock iClientM = new IClientMock(queryString);
		IMediaStream iMediaStream = new IMediaStreamMock(validFilename, iClientM); 
		streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
		assertEquals("Unexpected return value.", true, iClientM.getHasSetShutdownClientBeenCalled());
	}

	@Test
	public void testOnPlayIOException() {
		String queryString = "ObjectID=" + "IOExceptionTrigger" + "&SessionID=" + validSessionID;
		IClientMock iClientM = new IClientMock(queryString);
		IMediaStream iMediaStream = new IMediaStreamMock(validFilename, iClientM); 
		streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
		assertEquals("Unexpected return value.", true, iClientM.getHasSetShutdownClientBeenCalled());
	}

	@Test
	public void testOnPlayMCMOutputException() {
		String queryString = "ObjectID=" + "MCMOutputExceptionTrigger" + "&SessionID=" + validSessionID;
		IClientMock iClientM = new IClientMock(queryString);
		IMediaStream iMediaStream = new IMediaStreamMock(validFilename, iClientM); 
		streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
		assertEquals("Unexpected return value.", true, iClientM.getHasSetShutdownClientBeenCalled());
	}
}
