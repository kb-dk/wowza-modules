package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import java.io.File;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.Utils;

public class TicketToFileMapper implements IMediaStreamFileMapper {

	private WMSLogger logger;
	private TicketToolInterface ticketTool;
	private String invalidTicketVideo;
	
	public TicketToFileMapper(TicketToolInterface ticketTool, String invalidTicketVideo) {
		super();
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
		this.ticketTool = ticketTool;
		this.invalidTicketVideo = invalidTicketVideo;
	}

	@Override
	public File streamToFileForRead(IMediaStream stream) {
		File streamingFile;
		try {
			Ticket streamingTicket = getTicket(stream.getQueryStr());
			if ((streamingTicket != null) &&
				(isClientAllowedStreamingContent(stream, streamingTicket))) {
				streamingFile = getFileToStream(stream, streamingTicket);
			} else {
				streamingFile = new File(this.invalidTicketVideo);
			}
			logger.info("streamingTicket : " + streamingTicket);
			logger.info("streamingFile   : " + streamingFile);
		} catch (IllegallyFormattedQueryStringException e) {
			String mediaContentRoot = stream.getClient().getAppInstance().getStreamStorageDir();
			streamingFile = new File(mediaContentRoot + "/" + invalidTicketVideo);
			logger.warn("Illegally formatted query string [" + stream.getQueryStr() + "]." +
					" Playing file: " + streamingFile.getAbsolutePath());
		}
		logger.info("Resulting streaming file: " + streamingFile.getAbsolutePath());
		return streamingFile;
	}


	private boolean isClientAllowedStreamingContent(IMediaStream stream, Ticket streamingTicket) {
		String ipOfClient = stream.getClient().getIp();
		boolean isAllowed = (ipOfClient!=null) && (ipOfClient.equals(streamingTicket.getUsername()));
		return isAllowed;
	}

	private Ticket getTicket(String queryString) throws IllegallyFormattedQueryStringException {
		String ticketID = Utils.extractTicket(queryString);
		Ticket streamingTicket = ticketTool.resolveTicket(ticketID);
		logger.info("queryString     : " + queryString);
		logger.info("ticketID        : " + ticketID);
		return streamingTicket;
	}

	private File getFileToStream(IMediaStream stream, Ticket streamingTicket) {
		File streamingFile;
		String filenameAndPath = streamingTicket.getResource();
		String mediaContentRoot = stream.getClient().getAppInstance().getStreamStorageDir();
		streamingFile = new File(mediaContentRoot + "/" + filenameAndPath);
		logger.info("filenameAndPath : " + filenameAndPath);
		logger.info("mediaContentRoot: " + mediaContentRoot);
		return streamingFile;
	}

	@Override
	public File streamToFileForRead(IMediaStream arg0, String arg1,
			String arg2, String arg3) {
		return null;
	}

	@Override
	public File streamToFileForWrite(IMediaStream arg0) {
		return null;
	}

	@Override
	public File streamToFileForWrite(IMediaStream arg0, String arg1,
			String arg2, String arg3) {
		return null;
	}

}
