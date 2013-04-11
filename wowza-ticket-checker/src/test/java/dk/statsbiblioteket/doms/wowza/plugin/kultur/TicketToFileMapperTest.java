package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.DirectoryBasedContentResolver;
import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TicketToFileMapperTest {

    public static final String RTMP_HYPOTHETICAL_TEST_MACHINE_1935_DOMS_TICKET = "rtmp://hypothetical-test-machine:1935/doms?ticket=";
    private Logger logger;

    String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
    TicketToolMock ticketToolMock;

    String storageDir = new File(
            getClass().getClassLoader().getResource("streamingDir/README.streamingDir").getPath()).getParent()
            .toString();
    IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);



    String goodIP = "127.0.0.1";
    String badIP = "127.0.0.2-Invalid-ip";
    String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";

    String name = "name_of_stream";


    public TicketToFileMapperTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
    }

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        ticketToolMock = new TicketToolMock();
    }

    @After
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testStdCase() throws IOException {
        // Setup environment
        Ticket ticket = ticketToolMock.issueTicket(goodIP, programID, new ArrayList<Property>());
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_DOMS_TICKET + ticket.getId();
        IMediaStreamFileMapper defaultMapper = null;



        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Run test
        File result = ticketToFileMapper.streamToFileForRead(stream);
        // Validate result
        assertEquals("Expected equal result", new File(storageDir + "/0/e/f/8/" + programID + ".flv").getAbsolutePath(),
                     result.getAbsolutePath());
    }

    @Test
    public void testUserNotAllowedToPlayFile() {
        // Setup environment
        Ticket ticket = ticketToolMock.issueTicket(badIP, programID, new ArrayList<Property>());
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_DOMS_TICKET + ticket.getId();
        IMediaStreamFileMapper defaultMapper = null;


        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Run test
        File result = ticketToFileMapper.streamToFileForRead(stream);
        // Validate result
        assertEquals("Expected equal result", ticketInvalidErrorFile, result.getAbsolutePath());
    }

    @Test
    public void testNonExistingTicket() {
        // Setup environment
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_DOMS_TICKET + "InvalidID";
        IMediaStreamFileMapper defaultMapper = null;


        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Run test
        File result = ticketToFileMapper.streamToFileForRead(stream);
        // Validate result
        assertEquals("Expected equal result", ticketInvalidErrorFile, result.getAbsolutePath());
    }

    @Test
    public void testGetFileToStreamSucces() {
        // Setup
        Ticket ticket = ticketToolMock.issueTicket(goodIP, programID, new ArrayList<Property>());
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_DOMS_TICKET + ticket.getId();
        IMediaStreamFileMapper defaultMapper = null;

        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Test
        File result = ticketToFileMapper.getFileToStream(ticket);
        // Validate
        assertEquals("Expected equal result", new File(storageDir + "/0/e/f/8/" + programID + ".flv").getAbsolutePath(),
                     result.getAbsolutePath());
    }

    @Test
    public void testRetrieveMediaFileRelativePath() {
        // Setup
        Ticket ticket = ticketToolMock.issueTicket(goodIP, programID, new ArrayList<Property>());
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_DOMS_TICKET + ticket.getId();
        IMediaStreamFileMapper defaultMapper = null;


        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Test
        String result = ticketToFileMapper
                .getFileToStream(new Ticket("Streame", goodIP, Arrays.asList(programID),null))
                .getPath();
        // Validate
        assertEquals("Expected equal result", "0/e/f/8/0ef8f946-4e90-4c9d-843a-a03504d2ee6c.flv", result);
    }
}
