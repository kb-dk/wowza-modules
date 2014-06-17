package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.DirectoryBasedContentResolver;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IMediaStreamMock;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests the content resolver mapper.
 */
public class ContentResolverMapperTest {

    public static final String RTMP_HYPOTHETICAL_URL = "rtmp://hypothetical-test-machine:1935/mediestream";
    private Logger logger;

    String storageDir = new File(
            getClass().getClassLoader().getResource("streamingDir/README.streamingDir").getPath()).getParent()
            .toString();
    IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);



    String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
    String name = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c.flv";


    public ContentResolverMapperTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
    }

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
    }

    @After
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testStdCase() throws IOException {
        // Setup environment
        String queryString = RTMP_HYPOTHETICAL_URL;
        IMediaStreamFileMapper defaultMapper = null;


         //rtmp://iapetus.statsbiblioteket.dk:1937/mediestream?ticket=[ticketId]/flv:853a0b31-c944-44a5-8e42-bc9b5bc697be.flv
        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("Stream", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        ContentResolverMapper contentResolverMapper = new ContentResolverMapper("Stream", defaultMapper, contentResolver);
        // Run test
        File result = contentResolverMapper.streamToFileForRead(stream);
        // Validate result
        assertEquals("Expected equal result", new File(storageDir + "/0/e/f/8/" + programID + ".flv").getAbsolutePath(),
                     result.getAbsolutePath());
    }

    @Test
    public void testGetFileToStreamSucces() {
        // Setup
        String queryString = RTMP_HYPOTHETICAL_URL;
        IMediaStreamFileMapper defaultMapper = null;

        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("Stream", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        ContentResolverMapper contentResolverMapper = new ContentResolverMapper("Stream", defaultMapper, contentResolver);
        // Test
        File result = contentResolverMapper.streamToFileForRead(stream);
        // Validate
        assertEquals("Expected equal result", new File(storageDir + "/0/e/f/8/" + programID + ".flv").getAbsolutePath(),
                     result.getAbsolutePath());
    }

    @Test
    public void testRetrieveMediaFileRelativePath() {
        // Setup
        String queryString = RTMP_HYPOTHETICAL_URL;
        IMediaStreamFileMapper defaultMapper = null;


        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("Stream", new File(storageDir), 4,
                                                                            "%s\\.flv", "%s");
        ContentResolverMapper contentResolverMapper = new ContentResolverMapper("Stream", defaultMapper, contentResolver);
        // Test

        String result = contentResolverMapper.streamToFileForRead(stream)
                .getPath();
        // Validate
        assertEquals("Expected equal result", "0/e/f/8/0ef8f946-4e90-4c9d-843a-a03504d2ee6c.flv", result);
    }
}
