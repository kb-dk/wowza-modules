package dk.statsbiblioteket.chaos.wowza.plugin.authentication.model;

import java.io.IOException;
import java.net.MalformedURLException;

public interface SessionAndFilenameValidaterIF {

	/**
	 * Given the input, validate if the session has the right to play
	 * the stated file.
	 * 
	 * <ol>
	 *   <li>SessionID and ObjectID are used to call validation server</li>
	 *   <li>If the validation server knows the session and object, it returns the 
	 *       file name with the given ObjectID</li>
	 *   <li>If the local filename and the server filename match, access is 
	 *       granted</li>
	 * </ol> 
	 * 
	 * @param sessionID the session id related to the session. This is not the Wowza 
	 *        clientID
	 * @param objectID the object id of the given file to be played
	 * @param filename the file to be streamed
	 * @return <code>true</code> if the session is allowed to play the file. 
	 *         <code>false</code> otherwise
	 * @throws MalformedURLException if the URL to the validation server is malformed.
	 * @throws IOException if something went wrong when connecting to the server. 
	 * @throws MCMOutputException if the output from MCM cannot be read
	 */
	public abstract boolean validateRightsToPlayFile(String sessionID,
			String objectID, String filename) throws MalformedURLException,
			IOException, MCMOutputException;

}