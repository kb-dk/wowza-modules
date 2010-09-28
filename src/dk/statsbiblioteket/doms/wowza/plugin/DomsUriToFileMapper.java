/**
 * 
 */
package dk.statsbiblioteket.doms.wowza.plugin;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

/**
 * @author heb
 *
 */
public class DomsUriToFileMapper implements IMediaStreamFileMapper {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private WMSLogger wmsLogger;
	// TODO: Remove default file mapper. Only for development purpose
	private IMediaStreamFileMapper defaultFileMapper;
	private String storageDir;
	
	/**
	 * @param storageDir 
	 * @param defaultFileMapper 
	 * 
	 */
	public DomsUriToFileMapper(String storageDir, WMSLogger wmslogger, IMediaStreamFileMapper defaultFileMapper) {
		this.wmsLogger = wmslogger;
		this.defaultFileMapper = defaultFileMapper;
		this.storageDir = storageDir;
		wmslogger.info("Creating mapper...");
		wmslogger.info("Creating mapper: StorageDir=" + storageDir);
	}

	/* (non-Javadoc)
	 * @see com.wowza.wms.stream.IMediaStreamFileMapper#streamToFileForRead(com.wowza.wms.stream.IMediaStream)
	 * 
	 * This method knows of the Wowza context, such as the content folder where the files are placed
	 */
	@Override
	public File streamToFileForRead(IMediaStream stream) {
		wmsLogger.info("Called streamToFileForRead(IMediaStream stream).");
		File fileToStream;
		try {
			String uriString = stream.getQueryStr();
			wmsLogger.info("Query: " + uriString);
			String filename = extractFilename(uriString);
			fileToStream = new File(storageDir + "/" + filename);
		} catch (InvalidURIException e) {
			// No other means to signal Wowza that the request was wrong.
			fileToStream = null;
		}
		return fileToStream;
	}

	/* (non-Javadoc)
	 * @see com.wowza.wms.stream.IMediaStreamFileMapper#streamToFileForRead(com.wowza.wms.stream.IMediaStream, java.lang.String, java.lang.String, java.lang.String)
	 * 
	 * This method knows of the Wowza context, such as the content folder where the files are placed
	 */
	@Override
	public File streamToFileForRead(IMediaStream stream, String name, String ext, String query) {
		wmsLogger.info("Called streamToFileForRead(IMediaStream stream, String name, String ext, String query).");
		File fileToStream;
		try {
			String uriString = stream.getQueryStr();
			String filename = extractFilename(uriString);
			fileToStream = new File(storageDir + "/" + filename);
		} catch (InvalidURIException e) {
			// No other means to signal Wowza that request was wrong.
			fileToStream = null;
		}
		return fileToStream;
	}

	@Override
	public File streamToFileForWrite(IMediaStream arg0) {
		wmsLogger.info("Called streamToFileForWrite(IMediaStream arg0).");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File streamToFileForWrite(IMediaStream arg0, String arg1,
			String arg2, String arg3) {
		wmsLogger.info("Called streamToFileForWrite(IMediaStream arg0, String arg1, String arg2, String arg3.");
		return null;
	}

	/**
	 * This method knows how to extract a filename from an DOMS-URI
	 * 
	 * @param uriString, the DOMS-URI
	 * @return the filename of the movie clip that represents the program
	 * @throws InvalidURIException if the URI is invalid
	 */
	protected String extractFilename(String uriString) throws InvalidURIException {
		StringTokenizer tokenizer = new StringTokenizer(uriString,"_");
		if (tokenizer.countTokens() != 3) {
			throw new InvalidURIException("URI is not on the form <channel>_<from-date>_<to-date>");
		}
		try {
			String channel = tokenizer.nextToken();
			// no validation on channel yet
			String fromDateString = tokenizer.nextToken();
			validateStringAsDate(fromDateString);
			String toDateString = tokenizer.nextToken();
			validateStringAsDate(toDateString);
			sdf.parse(toDateString);
		} catch (ParseException e) {
			throw new InvalidURIException("Elements of the URI are not of the expected format. URI was: " + uriString, e);
		}
		return uriString + ".mp4";
	}

	protected void validateStringAsDate(String dateString) throws ParseException {
		Date date = sdf.parse(dateString);
		String dateReturnString = sdf.format(date);
		if (!dateString.equals(dateReturnString)) {
			throw new ParseException("Date is not valid. Read: " + dateString + ". " +
					"Interpreted: " + dateReturnString, 0);
		}
	}
}
