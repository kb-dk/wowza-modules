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

import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketToolInterface;

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
		String mediaFile = "a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv";
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		Ticket ticket = ticketToolMock.issueTicket(username, mediaFile);
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(ticketToolMock, ticketInvalidErrorFile);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", storageDir + "/" + mediaFile, 
				result.getAbsolutePath());
	}
	
	public void testUserNotAllowedToPlayFile() {
		// Setup environment
		String mediaFile = "a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv";
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.2-Invalid-ip";
		Ticket ticket = ticketToolMock.issueTicket(username, mediaFile);
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + ticket.getID();
		String storageDir = "/VHost/storageDir";
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(ticketToolMock, ticketInvalidErrorFile);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", ticketInvalidErrorFile, 
				result.getAbsolutePath());
	}

	public void testNonExistingTicket() {
		// Setup environment
		String mediaFile = "a/0/6/3/a0639529-124a-453f-b4ea-59f833b47333.flv";
		TicketToolInterface ticketToolMock = new TicketToolMock();
		String username = "127.0.0.1";
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?ticket=" + "InvalidID";
		String storageDir = "/VHost/storageDir";
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHost/data/rickrollfilename.flv";
		TicketToFileMapper ticketToFileMapper = new TicketToFileMapper(ticketToolMock, ticketInvalidErrorFile);
		// Run test
		File result = ticketToFileMapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", ticketInvalidErrorFile, 
				result.getAbsolutePath());
	}
}
