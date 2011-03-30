package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.QueryUtil;

public class TicketToFileMapper implements IMediaStreamFileMapper {

	private WMSLogger logger;
	private TicketToolInterface ticketTool;
	private String invalidTicketVideo;
	private Object mediaContentRootFolder;
	
	public TicketToFileMapper(TicketToolInterface ticketTool, String invalidTicketVideo, String mediaContentRootFolder) {
		super();
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
		this.ticketTool = ticketTool;
		this.invalidTicketVideo = invalidTicketVideo;
		this.mediaContentRootFolder = mediaContentRootFolder;
	}

	@Override
	public File streamToFileForRead(IMediaStream stream) {
		logger.info("streamToFileForRead(IMediaStream stream)");
		String name = stream.getName();
		String ext = stream.getExt();
		String query = stream.getQueryStr();
		return streamToFileForRead(stream, name, ext, query);
	}

	@Override
	public File streamToFileForRead(IMediaStream stream, String name, String ext, String streamQuery) {
		logger.info("streamToFileForRead(IMediaStream stream, String name, String ext, String query)");
		logger.info("streamToFileForRead(IMediaStream stream, String name, String ext, String query) - stream :" + stream);
		logger.info("streamToFileForRead(IMediaStream stream, String name, String ext, String query) - name   :" + name);
		logger.info("streamToFileForRead(IMediaStream stream, String name, String ext, String query) - ext    :" + ext);
		logger.info("streamToFileForRead(IMediaStream stream, String name, String ext, String query) - query  :" + streamQuery);
    	IClient client = stream.getClient();
        if (client == null) {
            logger.info("No client, returning ", stream);
            return null;
        }
		logger.info("streamToFileForRead(IMediaStream stream, String name, String ext, String query) - stream.Client().getQueryStr()  :" + stream.getClient().getQueryStr());
		String clientQuery = stream.getClient().getQueryStr();
		File streamingFile;
		try {
			Ticket streamingTicket = getTicket(clientQuery);
			logger.info("Ticket received: " + streamingTicket);
			if ((streamingTicket != null) && (isClientAllowedStreamingContent(stream, streamingTicket))) {
				logger.info("Streaming allowed");
				streamingFile = getFileToStream(stream, streamingTicket);
			} else {
				logger.info("Client not allowed to get content streamed");
				streamingFile = getErrorMediaFile();
				stream.setName(streamingFile.getName());
			}
		} catch (IllegallyFormattedQueryStringException e) {
			logger.info("Exception received.");
			streamingFile = getErrorMediaFile();
			stream.setName(streamingFile.getName());
			logger.warn("Illegally formatted query string [" + clientQuery + "]." +
					" Playing file: " + streamingFile.getAbsolutePath());
		}
		logger.info("Resulting streaming file: " + streamingFile.getAbsolutePath());
		return streamingFile;
	}

	private File getErrorMediaFile() {
		return new File(this.invalidTicketVideo); 
	}


	private boolean isClientAllowedStreamingContent(IMediaStream stream, Ticket streamingTicket) {
		String ipOfClient = stream.getClient().getIp();
		boolean isAllowed = (ipOfClient!=null) && (ipOfClient.equals(streamingTicket.getUsername()));
		logger.debug("isClientAllowedStreamingContent - ipOfClient: " + ipOfClient);
		logger.debug("isClientAllowedStreamingContent - streamingTicket.getUsername(): " + streamingTicket.getUsername());
		logger.debug("isClientAllowedStreamingContent - isAllowed: " + isAllowed);
		return isAllowed;
	}

	private Ticket getTicket(String queryString) throws IllegallyFormattedQueryStringException {
		String ticketID = QueryUtil.extractTicket(queryString);
		Ticket streamingTicket = ticketTool.resolveTicket(ticketID);
		logger.info("queryString     : " + queryString);
		logger.info("ticketID        : " + ticketID);
		return streamingTicket;
	}

	protected File getFileToStream(IMediaStream stream, Ticket streamingTicket) {
		String shardURL = streamingTicket.getResource();
	    Pattern queryPattern = Pattern.compile(
	            "http://www.statsbiblioteket.dk/doms/shard/uuid:([^&]*)");
        Matcher matcher = queryPattern.matcher(shardURL);
        if (!matcher.find()) {
        	throw new RuntimeException("Resource (shardURL did not match the " +
        			"expected format. Was " + shardURL);
        }
        // Extract
        String shardID = matcher.group(1);
        String filenameAndPath = retrieveMediaFileRelativePath(stream, shardID); 
		File streamingFile = new File(mediaContentRootFolder + "/" + filenameAndPath);
		logger.info("filenameAndPath : " + filenameAndPath);
		logger.info("mediaContentRoot: " + mediaContentRootFolder);
		return streamingFile;
	}

	protected String retrieveMediaFileRelativePath(IMediaStream stream, String shardID) {
		// TODO Implement BES call to retrieve media file location
		logger.info("Call BES instead of extracting information from id.");
		String folder1 = shardID.substring(0, 1);
        String folder2 = shardID.substring(1, 2);
        String folder3 = shardID.substring(2, 3);
        String folder4 = shardID.substring(3, 4);
		String filenameAndPath = folder1 + "/" + folder2 + "/" + 
								folder3 + "/" + folder4 + "/" + shardID + ".flv";
		logger.info("Resolved relative path: " + filenameAndPath);
		return filenameAndPath;
	}

	@Override
	public File streamToFileForWrite(IMediaStream stream) {
		logger.info("streamToFileForWrite(IMediaStream stream):" + stream);
		return null;
	}

	@Override
	public File streamToFileForWrite(IMediaStream stream, String arg1, String arg2, String arg3) {
		logger.info("streamToFileForWrite(IMediaStream stream, String arg1, String arg2, String arg3)" + stream);
		return null;
	}

}
