package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketProperty;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.DirectoryBasedContentResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TicketToFileMapperTest extends TestCase {

    private Logger logger;

    public TicketToFileMapperTest() {
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
        String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
        TicketToolInterface ticketToolMock = new TicketToolMock();
        String username = "127.0.0.1";
        Ticket ticket = ticketToolMock.issueTicket(username, programID, new ArrayList<TicketProperty>());
        String name = "name_of_stream";
        String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
        String storageDir = new File(
                getClass().getClassLoader().getResource("streamingDir/README.streamingDir").getPath()).getParent()
                .toString();
        IMediaStreamFileMapper defaultMapper = null;
        ;
        IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
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

    public void testUserNotAllowedToPlayFile() {
        // Setup environment
        String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
        TicketToolInterface ticketToolMock = new TicketToolMock();
        String username = "127.0.0.2-Invalid-ip";
        Ticket ticket = ticketToolMock.issueTicket(username, programID, new ArrayList<TicketProperty>());
        String name = "name_of_stream";
        String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
        String storageDir = new File(
                getClass().getClassLoader().getResource("streamingDir/README.streamingDir").getPath()).getParent()
                .toString();
        IMediaStreamFileMapper defaultMapper = null;
        ;
        IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Run test
        File result = ticketToFileMapper.streamToFileForRead(stream);
        // Validate result
        assertEquals("Expected equal result", ticketInvalidErrorFile, result.getAbsolutePath());
    }

    public void testNonExistingTicket() {
        // Setup environment
        String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
        TicketToolInterface ticketToolMock = new TicketToolMock();
        String username = "127.0.0.1";
        String name = "name_of_stream";
        String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + "InvalidID";
        String storageDir = new File(
                getClass().getClassLoader().getResource("streamingDir/README.streamingDir").getPath()).getParent()
                .toString();
        IMediaStreamFileMapper defaultMapper = null;
        ;
        IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "file://" + storageDir + "/%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Run test
        File result = ticketToFileMapper.streamToFileForRead(stream);
        // Validate result
        assertEquals("Expected equal result", ticketInvalidErrorFile, result.getAbsolutePath());
    }

    public void testGetFileToStreamSucces() {
        // Setup
        String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
        TicketToolInterface ticketToolMock = new TicketToolMock();
        String username = "127.0.0.1";
        Ticket ticket = ticketToolMock.issueTicket(username, programID, new ArrayList<TicketProperty>());
        String name = "name_of_stream";
        String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
        String storageDir = new File(
                getClass().getClassLoader().getResource("streamingDir/README.streamingDir").getPath()).getParent()
                .toString();
        IMediaStreamFileMapper defaultMapper = null;
        ;
        IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
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

    public void testRetrieveMediaFileRelativePath() {
        // Setup
        String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
        TicketToolInterface ticketToolMock = new TicketToolMock();
        String username = "127.0.0.1";
        Ticket ticket = ticketToolMock.issueTicket(username, programID, new ArrayList<TicketProperty>());
        String name = "name_of_stream";
        String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
        String storageDir = new File(
                getClass().getClassLoader().getResource("streamingDir/README.streamingDir").getPath()).getParent()
                .toString();
        IMediaStreamFileMapper defaultMapper = null;
        ;
        IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        ContentResolver contentResolver = new DirectoryBasedContentResolver("streaming", new File(storageDir), 4,
                                                                            "%s\\.flv", "%s");
        TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock,
                                                                       ticketInvalidErrorFile, contentResolver);
        // Test
        String result = ticketToFileMapper
                .getFileToStream(new Ticket("test", programID, "test", Collections.<TicketProperty>emptyList()))
                .getPath();
        // Validate
        assertEquals("Expected equal result", "0/e/f/8/0ef8f946-4e90-4c9d-843a-a03504d2ee6c.flv", result);
    }
}
