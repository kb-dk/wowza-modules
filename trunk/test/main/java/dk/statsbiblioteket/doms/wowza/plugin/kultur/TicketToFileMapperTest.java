package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.doms.wowza.plugin.kultur.TicketToFileMapper;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketProperty;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;

public class TicketToFileMapperTest extends TestCase {

	private Logger logger;
	private String broadcastExtractionServiceURL = "http://iapetus:9311/bes_DEVEL/rest/bes/"; // <---- See property files for recent server
	
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
		String shardID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL, new ArrayList<TicketProperty>());
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        WebResource besRestApi = Client.create().resource(broadcastExtractionServiceURL); // <---- See property files for recent server
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir, besRestApi);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", new File(storageDir + "/../radio_0/files/e/f/8/" + shardID + ".flv").getAbsolutePath(),
				result.getAbsolutePath());
	}
	
	public void testUserNotAllowedToPlayFile() {
		// Setup environment
		String shardID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.2-Invalid-ip";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL, new ArrayList<TicketProperty>());
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        WebResource besRestApi = Client.create().resource(broadcastExtractionServiceURL); // <---- See property files for recent server
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir, besRestApi);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", ticketInvalidErrorFile, 
				result.getAbsolutePath());
	}

	public void testNonExistingTicket() {
		// Setup environment
		String shardID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + "InvalidID";
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        WebResource besRestApi = Client.create().resource(broadcastExtractionServiceURL); // <---- See property files for recent server
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir, besRestApi);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", ticketInvalidErrorFile, 
				result.getAbsolutePath());
	}
	
	public void testGetFileToStreamSucces() {
		// Setup
		String shardID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL, new ArrayList<TicketProperty>());
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        WebResource besRestApi = Client.create().resource(broadcastExtractionServiceURL); // <---- See property files for recent server
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir, besRestApi);
		// Test
		File result = ticketToFileMapper.getFileToStream(stream, ticket);
		// Validate
            assertEquals("Expected equal result", new File(storageDir + "/../radio_0/files/e/f/8/" + shardID + ".flv").getAbsolutePath(),
          				result.getAbsolutePath());
	}
	
	public void testRetrieveMediaFileRelativePath() {
		// Setup
		String shardID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL, new ArrayList<TicketProperty>());
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
        WebResource besRestApi = Client.create().resource(broadcastExtractionServiceURL); // <---- See property files for recent server
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir, besRestApi);
		// Test
		String result = ticketToFileMapper.retrieveMediaFileRelativePath(stream, shardID);
		// Validate
		assertEquals("Expected equal result", "../radio_0/files/e/f/8/0ef8f946-4e90-4c9d-843a-a03504d2ee6c.flv",
				result);
	}
}
