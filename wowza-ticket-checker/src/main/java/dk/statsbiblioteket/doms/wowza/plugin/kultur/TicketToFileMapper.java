package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.QueryUtil;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.model.Resource;

import java.io.File;
import java.util.List;

/**
 * This class is used to validate the ticket and let the user see the correct file
 */
public class TicketToFileMapper implements IMediaStreamFileMapper {

    private final WMSLogger logger;
    private final IMediaStreamFileMapper defaultMapper;
    private final TicketToolInterface ticketTool;
    private final String invalidTicketVideo;
    private final ContentResolver contentResolver;

    public TicketToFileMapper(IMediaStreamFileMapper defaultMapper, TicketToolInterface ticketTool,
                              String invalidTicketVideo, ContentResolver contentResolver) {
        super();
        this.defaultMapper = defaultMapper;
        this.contentResolver = contentResolver;
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        this.ticketTool = ticketTool;
        this.invalidTicketVideo = invalidTicketVideo;
    }

    @Override
    public File streamToFileForRead(IMediaStream stream) {
        logger.info("streamToFileForRead(IMediaStream stream)");
        String name = stream.getName();
        String ext = stream.getExt();
        String query = stream.getQueryStr();
        return streamToFileForRead(stream, name, ext, query);
    }

    /**
     * This method is invoked when Wowza tries to figure out which file to play
     * @param stream the stream requested
     * @param name the name of the stream?
     * @param ext ?
     * @param streamQuery ?
     * @return the file to play
     */
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
        logger.info(
                "streamToFileForRead(IMediaStream stream, String name, String ext, String query) - stream.Client().getQueryStr()  :"
                        + stream.getClient().getQueryStr());
        String clientQuery = stream.getClient().getQueryStr();
        File streamingFile;
        try {
            dk.statsbiblioteket.medieplatform.ticketsystem.Ticket streamingTicket = getTicket(clientQuery);
            logger.info("Ticket received: " + streamingTicket);
            if (
                    streamingTicket != null &&
                    isClientAllowedStreamingContent(stream, streamingTicket) &&
                    doesTicketAllowThisStream(stream,streamingTicket)
                    ) {
                logger.info("Streaming allowed");

                //TODO play the file the user requested, not the one in the ticket
                streamingFile = getFileToStream(streamingTicket);

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

    private boolean doesTicketAllowThisStream(IMediaStream stream, dk.statsbiblioteket.medieplatform.ticketsystem.Ticket streamingTicket) {


        //TODO implement this method
        return true;  //To change body of created methods use File | Settings | File Templates.
    }

    private File getErrorMediaFile() {
        return new File(this.invalidTicketVideo);
    }

    /**
     * This method gets the ticketID from the querystring and resolves it through the ticket-system
     * @param queryString
     * @return an Unmarshalled ticket
     * @throws IllegallyFormattedQueryStringException
     */
    private dk.statsbiblioteket.medieplatform.ticketsystem.Ticket getTicket(String queryString) throws IllegallyFormattedQueryStringException {
        logger.info("getTicket: Query: " + queryString);
        String ticketID = QueryUtil.extractTicketID(queryString);
        logger.info("getTicket: query: " + ticketID);
        dk.statsbiblioteket.medieplatform.ticketsystem.Ticket streamingTicket = ticketTool.resolveTicket(ticketID);
        logger.info("getTicket: streamingTicket: " + streamingTicket);
        logger.info("queryString     : " + queryString);
        logger.info("ticketID        : " + ticketID);
        return streamingTicket;
    }

    /**
     * This method checks if the ticket is given to the same IP address as the client
     * @param stream the stream
     * @param streamingTicket the ticket
     * @return true if the ip is the same for the ticket and the user
     */
    private boolean isClientAllowedStreamingContent(IMediaStream stream, dk.statsbiblioteket.medieplatform.ticketsystem.Ticket streamingTicket) {
        String ipOfClient = stream.getClient().getIp();
        boolean isAllowed = (ipOfClient != null) && (ipOfClient.equals(streamingTicket.getUserIdentifier()));
        logger.info("isClientAllowedStreamingContent - ipOfClient: " + ipOfClient);
        logger.info(
                "isClientAllowedStreamingContent - streamingTicket.getUsername(): " + streamingTicket.getUserIdentifier());
        logger.info("isClientAllowedStreamingContent - isAllowed: " + isAllowed);
        return isAllowed;
    }

    /**
     * This method retrieves the filename from the ticket, by querying the content resolver to get the
     * streaming resource
     * @param streamingTicket the ticket
     * @return the file to stream
     */
    protected File getFileToStream(dk.statsbiblioteket.medieplatform.ticketsystem.Ticket streamingTicket) {
        // Extract
        //FIXME: this thing just takes the first entry in the list
        String programID = streamingTicket.getResources().get(0);
        if (programID.contains(":")) {
            programID = programID.substring(programID.lastIndexOf(':') + 1);
        }
        String filenameAndPath = getErrorMediaFile().getPath();
        logger.info("Looking up '" + programID + "'");
        List<Resource> resources = contentResolver.getContent(programID).getResources();
        if (resources != null) {
            for (Resource resource : resources) {
                if (resource.getType().equals("streaming")) {
                    filenameAndPath = resource.getUris().get(0).getPath();
                }
            }
        }
        File streamingFile = new File(filenameAndPath);
        logger.info("filenameAndPath : " + filenameAndPath);
        return streamingFile;
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
