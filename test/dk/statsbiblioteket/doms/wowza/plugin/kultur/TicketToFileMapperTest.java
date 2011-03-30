package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;

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
	public void testStdCase() {
		// Setup environment
		String shardID = "a0639529-124a-453f-b4ea-59f833b47333";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL);
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", storageDir + "/a/0/6/3/" + shardID + ".flv", 
				result.getAbsolutePath());
	}
	
	public void testUserNotAllowedToPlayFile() {
		// Setup environment
		String shardID = "a0639529-124a-453f-b4ea-59f833b47333";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.2-Invalid-ip";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL);
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", ticketInvalidErrorFile, 
				result.getAbsolutePath());
	}

	public void testNonExistingTicket() {
		// Setup environment
		String shardID = "a0639529-124a-453f-b4ea-59f833b47333";
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
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", ticketInvalidErrorFile, 
				result.getAbsolutePath());
	}
	
	public void testGetFileToStreamSucces() {
		// Setup
		String shardID = "a0639529-124a-453f-b4ea-59f833b47333";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL);
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir);
		// Test
		File result = ticketToFileMapper.getFileToStream(stream, ticket);
		// Validate
		assertEquals("Expected equal result", storageDir + "/a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv", 
				result.getAbsolutePath());
	}
	
	public void testRetrieveMediaFileRelativePath() {
		// Setup
		String shardID = "a0639529-124a-453f-b4ea-59f833b47333";
		String shardURL = "http://www.statsbiblioteket.dk/doms/shard/uuid:" + shardID;
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		Ticket ticket = ticketToolMock.issueTicket(username, shardURL);
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IMediaStreamFileMapper defaultMapper = null;;
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(defaultMapper, ticketToolMock, ticketInvalidErrorFile, storageDir);
		// Test
		String result = ticketToFileMapper.retrieveMediaFileRelativePath(stream, shardID);
		// Validate
		assertEquals("Expected equal result", "a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv", 
				result);
	}
}
