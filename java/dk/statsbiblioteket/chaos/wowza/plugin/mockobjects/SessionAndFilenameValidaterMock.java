package dk.statsbiblioteket.chaos.wowza.plugin.mockobjects;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import dk.statsbiblioteket.chaos.wowza.plugin.authentication.model.SessionAndFilenameValidaterIF;

public class SessionAndFilenameValidaterMock implements
		SessionAndFilenameValidaterIF {

	private String validSessionID;
	private String invalidSessionID;
	private String validObjectID;
	private String invalidObjectID;
	private String validFilename;
	private String invalidFilename;

	public SessionAndFilenameValidaterMock() {
		this.validSessionID = "5F95E509-FD84-4570-9382-FEC5481E342F";
		this.invalidSessionID = "E32262ED-21A8-46CB-BDA3-FF8D284DAC48";
		this.validObjectID = "976";
		this.invalidObjectID = "977";
		this.validFilename = new File("P3_2000_2200_890325_001.mp3").getAbsolutePath();
		this.invalidFilename = new File("P3_2000_2200_890325_001_invalid.mp3").getAbsolutePath();
	}
	
	@Override
	public boolean validateRightsToPlayFile(String sessionID, String objectID,
			String filename) throws MalformedURLException, IOException {
		// Validate input:
		if (!(objectID.equals(validObjectID) || objectID.equals(invalidObjectID) ||
			  objectID.equals("MalformedURLExceptionTrigger") || objectID.equals("IOExceptionTrigger") || objectID.equals("MCMOutputExceptionTrigger"))) {
			throw new IllegalArgumentException("Input not mocked exception");
		}
		if (!(sessionID.equals(validSessionID) || sessionID.equals(invalidSessionID))) {
			throw new IllegalArgumentException("Input not mocked exception");
		}
		if (!(filename.equals(validFilename) || filename.equals(invalidFilename))) {
			throw new IllegalArgumentException("Input not mocked exception. Filename: " + filename);
		}
		// Simulate behavior
		if (objectID.equals("MalformedURLExceptionTrigger")) {
			throw new MalformedURLException("Test MalformedURLException");
		}
		if (objectID.equals("IOExceptionTrigger")) {
			throw new IOException("Test IOException");
		}
		if (objectID.equals("MCMOutputExceptionTrigger")) {
			throw new IOException("Test IOException");
		}
		if (sessionID.equals(validSessionID) && objectID.equals(validObjectID) && filename.equals(validFilename)) {
			return true;
		} else {
			return false;
		}
	}

}
