package dk.statsbiblioteket.medieplatform.wowza.plugin.kultur;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.model.Resource;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.QueryUtil;

import java.io.File;
import java.util.List;

/**
 * This class is used to validate the ticket and let the user see the correct file
 */
public class TicketToFileMapper implements IMediaStreamFileMapper {

    private final WMSLogger logger;
    private String presentationType;
    private final IMediaStreamFileMapper defaultMapper;
    private final TicketToolInterface ticketTool;
    private final String invalidTicketVideo;
    private final ContentResolver contentResolver;

    public TicketToFileMapper(String presentationType, IMediaStreamFileMapper defaultMapper, TicketToolInterface ticketTool,
                              String invalidTicketVideo, ContentResolver contentResolver) {
        super();
        this.presentationType = presentationType;
        this.defaultMapper = defaultMapper;
        this.contentResolver = contentResolver;
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        this.ticketTool = ticketTool;
        this.invalidTicketVideo = invalidTicketVideo;
    }

    @Override
    public File streamToFileForRead(IMediaStream stream) {
        logger.trace("streamToFileForRead(IMediaStream stream=" + stream + ")");
        String name = stream.getName();
        String ext = stream.getExt();
        String query = stream.getQueryStr();
        return streamToFileForRead(stream, name, ext, query);
    }

    /**
     * This method is invoked when Wowza tries to figure out which file to play
     * @param stream the stream requested
     * @param name the name of the file to play
     * @param ext the extension of the file
     * @param streamQuery ?
     * @return the file to play
     */
    @Override
    public File streamToFileForRead(IMediaStream stream, String name, String ext, String streamQuery) {
        logger.trace(
                "streamToFileForRead(IMediaStream stream=" + stream + ", String name=" + name + ", String ext=" + ext
                        + ", String streamQuery=" + streamQuery + ")");
        IClient client = stream.getClient();
        if (client == null) {
            logger.debug("No client, returning ", stream);
            return null;
        }
        String clientQuery = stream.getClient().getQueryStr();
        File streamingFile;
        try {
            Ticket streamingTicket = QueryUtil.getTicket(clientQuery, ticketTool);
            logger.debug("Ticket received: " + (streamingTicket != null ? streamingTicket.getId() : "null"));
            if (
                    streamingTicket != null &&
                    isClientAllowed(stream, streamingTicket) &&
                            ticketForThisPresentationType(streamingTicket) &&
                    doesTicketAllowThisStream(name,streamingTicket)
                    ) {
                logger.debug("Streaming allowed for IMediaStream stream=" + stream + ", String name=" + name
                                     + ", String ext=" + ext + ", String streamQuery=" + streamQuery + ")");
                streamingFile = getFileToStream(name);

            } else {
                logger.debug("Client not allowed to get content streamed for IMediaStream stream=" + stream
                                     + ", String name=" + name + ", String ext=" + ext + ", String streamQuery="
                                     + streamQuery + ")");
                streamingFile = getErrorMediaFile();
                stream.setName(streamingFile.getName());
            }
        } catch (IllegallyFormattedQueryStringException e) {
            streamingFile = getErrorMediaFile();
            stream.setName(streamingFile.getName());
            logger.warn("Illegally formatted query string [" + clientQuery + "]." +
                    " Playing file: " + streamingFile.getAbsolutePath(), e);
        }
        logger.debug("Resulting streaming file: " + streamingFile.getAbsolutePath());
        logger.info(
                "streamToFileForRead(IMediaStream stream=" + stream + ", String name=" + name + ", String ext=" + ext
                        + ", String streamQuery=" + streamQuery + "). Resulting straming file: '" + streamingFile.getAbsolutePath() + "'");
        return streamingFile;
    }

    private boolean ticketForThisPresentationType(Ticket streamingTicket) {
        return streamingTicket.getType().equals(presentationType);
    }

    private boolean doesTicketAllowThisStream(String name, Ticket streamingTicket) {
        name = clean(name);
        boolean ticketForThis = false;
        for (String resource : streamingTicket.getResources()) {
            if (resource.contains(name)){
                ticketForThis = true;
                break;
            }
        }
        return ticketForThis;
    }

    /**
     * This method checks if the ticket is given to the same IP address as the client
     * @param stream the stream
     * @param streamingTicket the ticket
     * @return true if the ip is the same for the ticket and the user
     */
    private boolean isClientAllowed(IMediaStream stream, Ticket streamingTicket) {
        String ipOfClient = stream.getClient().getIp();

        boolean isAllowed = (ipOfClient != null) && (ipOfClient.equals(streamingTicket.getUserIdentifier()));
        logger.debug("isClientAllowed - ipOfClient: " + ipOfClient + ", streamingTicket.getUserIdentifier(): " + streamingTicket.getUserIdentifier() + ", isAllowed: " + isAllowed);
        return isAllowed;
    }

    private String clean(String name) {
        if (name.contains(".")){
            name = name.substring(0,name.indexOf("."));
        }
        if (name.contains(":")) {
            name = name.substring(name.lastIndexOf(':') + 1);
        }

        return name;
    }

    private File getErrorMediaFile() {
        return new File(this.invalidTicketVideo);
    }

    /**
     * This method retrieves the filename from the ticket, by querying the content resolver to get the
     * streaming resource
     * @param name the filename
     * @return the file to stream
     */
    protected File getFileToStream(String name) {
        // Extract
        name = clean(name);
        logger.debug("Looking up '" + name + "'");
        List<Resource> resources = contentResolver.getContent(name).getResources();
        if (resources != null) {
            for (Resource resource : resources) {
                if (resource.getType().equals(presentationType)) {
                    String pathname = resource.getUris().get(0).getPath();
                    logger.debug("Found '" + pathname + "' for '" + name + "'");
                    return new File(pathname);
                }
            }
        }
        logger.debug("Content not found for: '" + name + "'");
        return new File(getErrorMediaFile().getPath());
    }

    @Override
    public File streamToFileForWrite(IMediaStream stream) {
        logger.trace("streamToFileForWrite(IMediaStream stream):" + stream);
        return defaultMapper.streamToFileForRead(stream);
    }

    @Override
    public File streamToFileForWrite(IMediaStream stream, String name, String ext, String query) {
        logger.trace("streamToFileForWrite(IMediaStream stream, String name, String ext, String query)" + stream);
        return defaultMapper.streamToFileForRead(stream, name, ext, query);
    }

}
