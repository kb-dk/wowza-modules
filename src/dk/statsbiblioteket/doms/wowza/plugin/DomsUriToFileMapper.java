/**
 *
 */
package dk.statsbiblioteket.doms.wowza.plugin;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketChecker;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketCheckerInterface;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.QueryUtil;

import java.io.File;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class decodes the query string of the URL by which we were called, and
 * on the basis of this query string identifies the video to be played, and
 * authorizes the player against the ticket checker.
 *
 * @author heb + jrg + mar + abr
 */
public class DomsUriToFileMapper implements IMediaStreamFileMapper {

    private SimpleDateFormat sdf;

    private String rickrollFilename;

    private TicketCheckerInterface ticketChecker;

    private String storageDir;
    private IMediaStreamFileMapper backupMapper;

    /**
     * Constructor
     *
     * @param storageDir
     */
    public DomsUriToFileMapper(String storageDir,
                               String format,
                               String ticketInvalidErrorFile,
                               String ticketCheckerLocation,
                               IMediaStreamFileMapper backupMapper) {
        this.backupMapper = backupMapper;
        getLogger().info("Entered DomsUriToFileMapper(...)");
        this.sdf = new SimpleDateFormat(format);
        this.rickrollFilename = ticketInvalidErrorFile;
        getLogger().info("Config value sdf: '" + sdf.toString() + "'");
        getLogger().info("Config value rickrollFilename: '" + rickrollFilename
                         + "'");
        getLogger().info("Config value ticketCheckerLocation: '"
                         + ticketCheckerLocation + "'");


        this.storageDir = storageDir;

        ticketChecker = new TicketChecker(ticketCheckerLocation);

        getLogger().info("Creating mapper...");
        getLogger().info("Creating mapper: StorageDir=" + storageDir);
    }

    public DomsUriToFileMapper(String storageDir, String format,
			String ticketInvalidErrorFile, TicketCheckerInterface ticketChecker,
			IMediaStreamFileMapper backupMapper) {
        this.backupMapper = backupMapper;
        getLogger().info("Entered DomsUriToFileMapper(...)");
        this.sdf = new SimpleDateFormat(format);
        this.rickrollFilename = ticketInvalidErrorFile;
        getLogger().info("Config value sdf: '" + sdf.toString() + "'");
        getLogger().info("Config value rickrollFilename: '" + rickrollFilename
                         + "'");

        this.storageDir = storageDir;

        this.ticketChecker = ticketChecker;

        getLogger().info("Creating mapper...");
        getLogger().info("Creating mapper: StorageDir=" + storageDir);
	}

	/**
     * Get the file that should be streamed. Extract its filename from query
     * string, and check whether player is authorized to play it.
     *
     * @param stream The stream which we are about to provide to the client
     * player.
     * @return the file that should be streamed. In case the player was not
     * authorized, an alternative video file, or just null, will be returned.
     */
    /* (non-Javadoc)
      * @see com.wowza.wms.stream.IMediaStreamFileMapper#streamToFileForRead(
      * com.wowza.wms.stream.IMediaStream)
      *
      * This method knows of the Wowza context, such as the content folder where
      * the files are placed
      */
    @Override
    public File streamToFileForRead(IMediaStream stream) {
        getLogger().info("***Entered streamToFileForRead(IMediaStream stream)");
        File fileToStream = null;

        try {
        	/*
        	IClient client = stream.getClient();
            if (client == null) {
                getLogger().info("No client, returning ", stream);
                return null;
            }
            String queryString;
            queryString = URLDecoder.decode(client.getQueryStr(), "UTF-8");
            String shardpid = Utils.extractShardID(queryString);
            String shardurl = Utils.extractShardURL(queryString);
            String ticket = Utils.extractTicket(queryString);
            getLogger().info("queryString: '" + queryString + "'");
            getLogger().info("Shardpid is "+shardpid);
            getLogger().info("Shardurl is "+shardurl);
            getLogger().info("Ticket is "+ticket);
            if (ticketChecker.isTicketValid(ticket,shardurl,client.getIp())) {
            	// Test purpose start
            	getLogger().info("StorageDir: " + stream.getClient().getAppInstance().getStreamStorageDir());
            	getLogger().info("Stream name: " + stream.getName());
            	// Test purpose start
                String filetype = stream.getExt();
                if (filetype.isEmpty()){
                    filetype = "flv";
                }
                String filename = Utils.extractShardID(queryString) + "." + filetype;
                fileToStream = new File(storageDir + "/" + filename);
                getLogger().info("filetype: '" + filetype + "'");
                getLogger().info("filename: '" + filename + "'");
                getLogger().info("file to stream: '" + fileToStream.getAbsolutePath() + "'");
                getLogger().info("Got fileToStream");
            } else {
            	fileToStream = new File(rickrollFilename);
            }
        } catch (IllegallyFormattedQueryStringException e) {
            getLogger().error("Query string has unexptected format. Parsing stopped with message: "+e.toString());
            e.printStackTrace();
            fileToStream = new File(rickrollFilename);
            */
        } catch (Exception e) {
            // TODO better log level
            getLogger().error("Unexpected error "+e.toString()+" occurred.");
            e.printStackTrace();
            fileToStream = new File(rickrollFilename);
		}
        getLogger().info("Playing file: " + fileToStream.getAbsolutePath());
        return fileToStream;
    }

    /**
     * Get the file that should be streamed.
     *
     * TODO javadoc
     *
     * @param stream
     * @param name
     * @param ext
     * @param query
     * @return
     */
    /* (non-Javadoc)
      * @see com.wowza.wms.stream.IMediaStreamFileMapper#streamToFileForRead(
      * com.wowza.wms.stream.IMediaStream, java.lang.String, java.lang.String,
      * java.lang.String)
      *
      * This method knows of the Wowza context, such as the content folder where
      * the files are placed
      */
    @Override
    public File streamToFileForRead(IMediaStream stream, String name,
                                    String ext, String query) {
        getLogger().info("***Entered streamToFileForRead(IMediaStream stream, "
                         + "String name, String ext, String query)");

        return streamToFileForRead(stream);
    }

    @Override
    public File streamToFileForWrite(IMediaStream arg0) {
        getLogger().info("***Entered streamToFileForWrite(IMediaStream arg0)");
        // TODO Auto-generated method stub
        return backupMapper.streamToFileForWrite(arg0);
    }

    @Override
    public File streamToFileForWrite(IMediaStream arg0, String arg1,
                                     String arg2, String arg3) {
        getLogger().info("***Entered streamToFileForWrite(IMediaStream arg0, "
                         + "String arg1, String arg2, String arg3");
        return backupMapper.streamToFileForWrite(arg0, arg1, arg2, arg3);
    }

    /**
     * TODO javadoc
     *
     * @param dateString
     * @throws java.text.ParseException
     */
    protected void validateStringAsDate(String dateString)
            throws ParseException {
        Date date = sdf.parse(dateString);
        String dateReturnString = sdf.format(date);
        if (!dateString.equals(dateReturnString)) {
            throw new ParseException("Date is not valid. Read: " + dateString
                                     + ". " + "Interpreted: " + dateReturnString, 0);
        }
    }

    protected static WMSLogger getLogger()
    {
        return WMSLoggerFactory.getLogger(DomsUriToFileMapper.class);
    }

}