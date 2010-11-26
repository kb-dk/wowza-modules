/**
 *
 */
package dk.statsbiblioteket.doms.wowza.plugin.domslive.model;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import com.wowza.wms.client.IClient;
import dk.statsbiblioteket.doms.wowza.plugin.domslive.ConfigReader;
import dk.statsbiblioteket.doms.wowza.plugin.domslive.TicketChecker;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class decodes the query string of the URL by which we were called, and
 * on the basis of this query string identifies the video to be played, and
 * authorizes the player against the ticket checker.
 *
 * @author heb + jrg + mar
 */
public class DomsUriToFileMapper implements IMediaStreamFileMapper {

    private static SimpleDateFormat sdf;

    private static String rickrollFilename;
    private static String ticketCheckerLocation;

    TicketChecker ticketChecker;

    private String storageDir;

    /**
     * Constructor
     *
     * @param storageDir
     * @param applicationDir
     */
    public DomsUriToFileMapper(String storageDir,
                               String applicationDir) {
        getLogger().info("Entered DomsUriToFileMapper(...)");
        String propertyFileLocation = applicationDir
                                      + "/conf/domslive/doms-wowza-plugin.properties";

        // Current working directory is /
        getLogger().info("propertyFileLocation: '" + propertyFileLocation + "'");
        ConfigReader cr = new ConfigReader(propertyFileLocation);
        sdf = new SimpleDateFormat(cr.get("sdf"));
        rickrollFilename = cr.get("rickrollFilename");
        ticketCheckerLocation = cr.get("ticketCheckerLocation");
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
        File fileToStream;

        try {
            // Extract filename from the query string and filetype from the
            // connection string.


            IClient client = stream.getClient();
            if (client == null){
                getLogger().info("No client, returning ",stream);
                return null;
            }
            getLogger().info("onResolveFile (pageurl)     : " + client.getPageUrl());
            getLogger().info("onResolveFile (querystring)  : " + client.getQueryStr());
            getLogger().info("onResolveFile (uri)     : " + client.getUri());
            getLogger().info("onResolveFile (referrer)     : " + client.getReferrer());


            String filetype = stream.getExt();
            getLogger().info("filetype: '" + filetype + "'");
            String queryString = URLDecoder.decode(
                    client.getQueryStr(), "UTF-8");
            String filename = extractFilename(queryString, filetype);


            getLogger().info("queryString: '" + queryString + "'");
            getLogger().info("filename: '" + filename + "'");

            fileToStream = new File(storageDir + "/" + filename);
            getLogger().info("Got fileToStream");
            // Authorization check will throw InvalidURIException if not
            // authorized.
            //checkAuthorization(stream);

        } catch (InvalidURIException e) {
            // No other means to signal Wowza that the request was wrong.
            //fileToStream = null;
            fileToStream = new File(storageDir + "/" + rickrollFilename);
        } catch (Exception e) {
            // TODO better log level
            getLogger().error("Unexpected error "+e.toString()+" occurred '"+e.getMessage()+"'");
            fileToStream = new File(storageDir + "/" + rickrollFilename);
        }

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
        return null;
    }

    @Override
    public File streamToFileForWrite(IMediaStream arg0, String arg1,
                                     String arg2, String arg3) {
        getLogger().info("***Entered streamToFileForWrite(IMediaStream arg0, "
                         + "String arg1, String arg2, String arg3");
        return null;
    }

    /**
     * This method knows how to extract a filename from a DOMS-PID in
     * a query string.
     *
     * @param queryString the query string of a stream, of the format:
     * "shard=http://www.statsbiblioteket.dk/doms/shard/<DOMS-PID>&ticket=<TICKET-ID>"
     * @param filetype The type of the file for which we want the name
     * @return the filename of the movie clip that represents the program
     * @throws dk.statsbiblioteket.doms.wowza.plugin.model.InvalidURIException if the URI is invalid
     */
    protected String extractFilename(String queryString, String filetype)
            throws InvalidURIException {
        getLogger().info("***Entered extractFilename('" + queryString + "')");
        String shardId;
        String filenameExtension;

        // Create a pattern to match a correct query string
        Pattern queryPattern = Pattern.compile(
                "shard=http://www.statsbiblioteket.dk/doms/shard/uuid:([^&]*)"
                + "&ticket=([^&]*)");

        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();

        // Extract
        if (matchFound) {
            shardId = matcher.group(1);
        } else {
            getLogger().info("Query string did not match required format, "
                             + "throwing exception");
            throw new InvalidURIException("Query string is not of the expected"
                                          + " format.");
        }

        if (filetype.isEmpty()){
            filenameExtension = "flv";
        } else{
            filenameExtension = filetype;
        }

        return shardId + "." + filenameExtension;
    }

    /**
     * Checks (with ticket issuer) that the ticket and shard url received from
     * player, along with the players ip, allow the stream to be played.
     *
     * @param stream The stream that we need to authorize for playing.
     * @throws dk.statsbiblioteket.doms.wowza.plugin.model.InvalidURIException In case the stream was not allowed to be
     * played.
     */
    private void checkAuthorization(IMediaStream stream)
            throws InvalidURIException, UnsupportedEncodingException {
        getLogger().info("***Entered checkAuthorization(stream)");
        String queryString = URLDecoder.decode(
                stream.getClient().getQueryStr(), "UTF-8");
        String ipOfClientPlayer = stream.getClient().getIp();
        String ticket;
        String shardUrl;
        boolean ticketIsValid;

        getLogger().info("queryString: " + queryString);

        ticket = getTicketFromQueryString(queryString);
        shardUrl = getShardUrlFromQueryString(queryString);

        // Call ticket issuer via REST client with ipOfClientPlayer, ticket,
        // shardUrl
        ticketIsValid = ticketChecker.isTicketValid(ticket, shardUrl,
                                                    ipOfClientPlayer);

        if (!ticketIsValid) {
            getLogger().info("Ticket is invalid. ipOfClientPlayer='"
                             + ipOfClientPlayer + "', ticket='" + ticket
                             + "', shardUrl='" + shardUrl + "'");
            throw new InvalidURIException("Ticket is not valid");
        }
    }

    /**
     * TODO javadoc
     *
     * @param queryString
     * @return
     * @throws dk.statsbiblioteket.doms.wowza.plugin.model.InvalidURIException
     */
    public String getTicketFromQueryString(String queryString)
            throws InvalidURIException {
        getLogger().info("***Entered getTicketFromQueryString('"
                         + queryString + "')");
        String ticketId;

        // Create a pattern to match a correct query string
        Pattern queryPattern = Pattern.compile(
                "shard=(http://www.statsbiblioteket.dk/doms/shard/uuid:[^&]*)"
                + "&ticket=([^&]*)");

        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();

        // Extract
        if (matchFound) {
            ticketId = matcher.group(2);
        } else {
            getLogger().info("Query string did not match required format, "
                             + "throwing exception");
            throw new InvalidURIException("Query string is not of the expected"
                                          + " format.");
        }

        return ticketId;
    }

    /**
     * TODO javadoc
     *
     * @param queryString
     * @return
     * @throws dk.statsbiblioteket.doms.wowza.plugin.model.InvalidURIException
     */
    public String getShardUrlFromQueryString(String queryString)
            throws InvalidURIException {
        getLogger().info("***Entered getShardUrlFromQueryString('"
                         + queryString + "')");
        String shardPid;

        // Create a pattern to match a correct query string
        Pattern queryPattern = Pattern.compile(
                "shard=(http://www.statsbiblioteket.dk/doms/shard/uuid:[^&]*)"
                + "&ticket=([^&]*)");

        // Match
        Matcher matcher = queryPattern.matcher(queryString);
        boolean matchFound = matcher.find();

        // Extract
        if (matchFound) {
            shardPid = matcher.group(1);
        } else {
            getLogger().info("Query string did not match required format, "
                             + "throwing exception");
            throw new InvalidURIException("Query string is not of the expected"
                                          + " format.");
        }

        return shardPid;
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