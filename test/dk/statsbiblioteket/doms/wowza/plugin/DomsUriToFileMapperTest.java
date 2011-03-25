package dk.statsbiblioteket.doms.wowza.plugin;

import java.io.File;

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
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketCheckerMock;

import junit.framework.TestCase;

public class DomsUriToFileMapperTest extends TestCase {

	private Logger logger;

	
	public DomsUriToFileMapperTest() {
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
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?shard=http://www.statsbiblioteket.dk/doms/shard/uuid:9648bb70-b44c-45ed-b2d4-b08b5419eb62&ticket=127.0.0.1@http://www.statsbiblioteket.dk/doms/shard/uuid:9648bb70-b44c-45ed-b2d4-b08b5419eb62@1566797781";
		String storageDir = "/VHost/storageDir";
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHostroot/data/rickrollfilename.flv";
		DomsUriToFileMapper mapper = new DomsUriToFileMapper(storageDir, "yyyy-MM-dd-HH-mm-ss", ticketInvalidErrorFile, new TicketCheckerMock(), null);
		// Run test
		File result = mapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", storageDir + "/9648bb70-b44c-45ed-b2d4-b08b5419eb62.flv", 
				result.getAbsolutePath());
	}

	@Test
	public void testInvalidSessionCase() {
		// Setup environment
		String name = "name_of_stream";
		String queryString = "rtmp://hypothetical-test-machine:1935/doms?shard=http://www.statsbiblioteket.dk/doms/shard/uuid:9648bb70-b44c-45ed-b2d4-b08b5419eb62&ticket=127.0.0.1@http://www.statsbiblioteket.dk/doms/shard/uuid:9648bb70-b44c-45ed-b2d4-b08b5419eb62@1566797781";
		String storageDir = "/VHost/storageDir";
		IApplicationInstance iAppInstance = new IApplicationInstanceMock(storageDir);
		IClient iClient = new IClientMock(iAppInstance, logger, queryString);
		IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
		String ticketInvalidErrorFile = "/VHostroot/data/rickrollfilename.flv";
		DomsUriToFileMapper mapper = new DomsUriToFileMapper(storageDir, "yyyy-MM-dd-HH-mm-ss", ticketInvalidErrorFile, new TicketCheckerMock(false), null);
		// Run test
		File result = mapper.streamToFileForRead(stream);
		// Validate result
		assertEquals("Expected equal result", ticketInvalidErrorFile, 
				result.getAbsolutePath());
	}
}
