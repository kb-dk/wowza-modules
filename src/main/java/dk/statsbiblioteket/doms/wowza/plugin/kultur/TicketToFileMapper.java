package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.doms.wowza.plugin.bes.ObjectStatus;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.QueryUtil;

public class TicketToFileMapper implements IMediaStreamFileMapper {

	private WMSLogger logger;
	private IMediaStreamFileMapper defaultMapper;
	private TicketToolInterface ticketTool;
	private String invalidTicketVideo;
	private Object mediaContentRootFolder;
	private WebResource besRestApi;

	
	public TicketToFileMapper(IMediaStreamFileMapper defaultMapper, TicketToolInterface ticketTool, 
			String invalidTicketVideo, String mediaContentRootFolder, WebResource restApi) {
		super();
		this.defaultMapper = defaultMapper;
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
		this.ticketTool = ticketTool;
		this.invalidTicketVideo = invalidTicketVideo;
		this.mediaContentRootFolder = mediaContentRootFolder;
		this.besRestApi = restApi;
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
    	IClient client = stream.getClient();
        if (client == null) {
        	// This is the case when a live stream is generated. 
        	// Two streams are created, and one streams from VLC to Wowza and has no client.
        	// If omitted, no live stream is played.
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
			logger.error("Exception received.");
			streamingFile = getErrorMediaFile();
			stream.setName(streamingFile.getName());
			logger.warn("Illegally formatted query string [" + clientQuery + "]." +
					" Playing file: " + streamingFile.getAbsolutePath());
		}
		logger.info("Resulting streaming file: " + streamingFile.getAbsolutePath());
		logger.info("Resulting streaming file exist: " + streamingFile.exists());
		return streamingFile;
	}

	private File getErrorMediaFile() {
		return new File(this.invalidTicketVideo); 
	}


	private Ticket getTicket(String queryString) throws IllegallyFormattedQueryStringException {
		logger.info("getTicket: Query: " + queryString);
		String ticketID = QueryUtil.extractTicketID(queryString);
		logger.info("getTicket: query: " + ticketID);
		Ticket streamingTicket = ticketTool.resolveTicket(ticketID);
		logger.info("getTicket: streamingTicket: " + streamingTicket);
		logger.info("queryString     : " + queryString);
		logger.info("ticketID        : " + ticketID);
		return streamingTicket;
	}

	private boolean isClientAllowedStreamingContent(IMediaStream stream, Ticket streamingTicket) {
		String ipOfClient = stream.getClient().getIp();
		boolean isAllowed = (ipOfClient!=null) && (ipOfClient.equals(streamingTicket.getUsername()));
		logger.info("isClientAllowedStreamingContent - ipOfClient: " + ipOfClient);
		logger.info("isClientAllowedStreamingContent - streamingTicket.getUsername(): " + streamingTicket.getUsername());
		logger.info("isClientAllowedStreamingContent - isAllowed: " + isAllowed);
		return isAllowed;
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
		String filenameAndPath = null;
		try {
			logger.info("ObjectStatus: " + besRestApi
					.path("/getobjectstatus")
					.queryParam("programpid", "uuid:" + shardID).toString());
			ObjectStatus objectStatusXml = besRestApi
					.path("/getobjectstatus")
					.queryParam("programpid", "uuid:" + shardID)
					.get(ObjectStatus.class);
			String streamID = objectStatusXml.getStreamId();
			int indexOfColon = streamID.indexOf(":");
			filenameAndPath = streamID.substring(indexOfColon+1, streamID.length());
		}  catch (UniformInterfaceException e) {
			logger.warn("UniformInterfaceException occured. Ticket might be invalidated.", e);
		}
		logger.info("Resolved relative path: " + filenameAndPath);
		return filenameAndPath;
	}

	@Override
	public File streamToFileForWrite(IMediaStream stream) {
		logger.info("streamToFileForWrite(IMediaStream stream):" + stream);
		return defaultMapper.streamToFileForRead(stream);
	}

	@Override
	public File streamToFileForWrite(IMediaStream stream, String name, String ext, String query) {
		logger.info("streamToFileForWrite(IMediaStream stream, String name, String ext, String query)" + stream);
		return defaultMapper.streamToFileForRead(stream, name, ext, query);
	}

}
