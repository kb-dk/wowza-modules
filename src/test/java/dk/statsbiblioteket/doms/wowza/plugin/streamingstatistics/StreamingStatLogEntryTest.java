package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.doms.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics.StreamingStatLogEntry.Event;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketProperty;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;

import junit.framework.TestCase;

public class StreamingStatLogEntryTest  extends TestCase {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
	private WMSLogger logger;
	private TicketToolInterface ticketTool;
	
	// Default test values
	private static final String defaultQueryString = "ticket=abcd";
	private static final String defaulStreamName = "default-stream-name";
	private static final String defaultUsername = "127.0.0.1";
	private static final String defaultResource = "http://www.statsbiblioteket.dk/doms/shard/uuid:a0639529-124a-453f-b4ea-59f833b47333";
	private static final String defaultEduPersonTargetedID = "0123456789abcd";
	private static final String defaulStorageDir = "/vhost/storage/dir";
	private IMediaStreamMock defaultStream;
	private IClientMock defaultIClient;
	private IApplicationInstanceMock defaultIAppInstance;

	public StreamingStatLogEntryTest() {
		super();
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
		this.ticketTool = new TicketToolMock();
		this.defaultIAppInstance = new IApplicationInstanceMock(defaulStorageDir);
		this.defaultIClient = new IClientMock(defaultIAppInstance, logger, defaultQueryString);
		this.defaultStream = new IMediaStreamMock(logger, defaulStreamName, defaultIClient);
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
	public void testConstructorValueMappingWithWAYF() {
		// Setup environment
		List<TicketProperty> properties = new ArrayList<TicketProperty>();
		// Setup user info
		properties.add(new TicketProperty("schacHomeOrganization", "au.dk"));
		properties.add(new TicketProperty("eduPersonTargetedID", "1x1"));
		// Setup program info
		properties.add(new TicketProperty("metaChannelName", "tv2news"));
		properties.add(new TicketProperty("metaTitle", "Nyheder"));
		properties.add(new TicketProperty("metaDateTimeStart", "2007-03-04T00:00:00+0100"));
		Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties );
		Event logEvent = Event.STREAMING_START;
		// Test
		StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, defaultStream, logEvent, ticket);
		// Validate
		assertEquals("StreamingStatLogEntry value", Event.STREAMING_START, logEntry.getEvent());
		assertEquals("StreamingStatLogEntry value", defaultStream.getUniqueStreamIdStr(), logEntry.getConnectionID());
		assertEquals("StreamingStatLogEntry value", "au.dk", logEntry.getOrganisationID());
		assertEquals("StreamingStatLogEntry value", "1x1", logEntry.getUserID());
		assertEquals("StreamingStatLogEntry value", "tv2news", logEntry.getChannelID());
		assertEquals("StreamingStatLogEntry value", "Nyheder", logEntry.getProgramTitle());
		assertEquals("StreamingStatLogEntry value", "2007-03-04T00:00:00+0100", logEntry.getProgramStart());
	}

	@Test
	public void testConstructorValueMappingWithoutWAYF() {
		// Setup environment
		List<TicketProperty> properties = new ArrayList<TicketProperty>();
		// Setup program info
		properties.add(new TicketProperty("metaChannelName", "tv2news"));
		properties.add(new TicketProperty("metaTitle", "Nyheder"));
		properties.add(new TicketProperty("metaDateTimeStart", "2007-03-04T00:00:00+0100"));
		Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties );
		Event logEvent = Event.STREAMING_START;
		// Test
		StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, defaultStream, logEvent, ticket);
		// Validate
		assertEquals("StreamingStatLogEntry value", Event.STREAMING_START, logEntry.getEvent());
		assertEquals("StreamingStatLogEntry value", defaultStream.getUniqueStreamIdStr(), logEntry.getConnectionID());
		assertEquals("StreamingStatLogEntry value", "statsbiblioteket.dk", logEntry.getOrganisationID());
		assertEquals("StreamingStatLogEntry value", defaultUsername, logEntry.getUserID());
		assertEquals("StreamingStatLogEntry value", "tv2news", logEntry.getChannelID());
		assertEquals("StreamingStatLogEntry value", "Nyheder", logEntry.getProgramTitle());
		assertEquals("StreamingStatLogEntry value", "2007-03-04T00:00:00+0100", logEntry.getProgramStart());
	}

	@Test
	public void testGetLogString() throws ParseException {
		// Setup environment
		List<TicketProperty> properties = new ArrayList<TicketProperty>();
		// Setup user info
		properties.add(new TicketProperty("schacHomeOrganization", "au.dk"));
		properties.add(new TicketProperty("eduPersonScopedAffiliation", "some role"));
		properties.add(new TicketProperty("eduPersonTargetedID", "1x1"));
		// Setup program info
		properties.add(new TicketProperty("metaChannelName", "tv2news"));
		properties.add(new TicketProperty("metaTitle", "Nyheder"));
		properties.add(new TicketProperty("metaDateTimeStart", "2007-03-04T00:00:00+0100"));
		Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties );
		Event logEvent = Event.STREAMING_START;
		String timestamp = "2010-11-15 17:31:05.749";
		// Test
		StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, defaultStream, logEvent, ticket);
		logEntry.setTimestamp(sdf.parse(timestamp));
		// Validate
		String expectedLogString = "2010-11-15 17:31:05.749;uniqueStreamIdStr;STREAMING_START;1x1;some role;au.dk;tv2news;Nyheder;2007-03-04T00:00:00+0100";
		assertEquals("Log entry", expectedLogString, logEntry.getLogString());
	}
	
	@Test
	public void testGetLogStringWithEscapeCharacters() throws ParseException {
		// Setup environment
		List<TicketProperty> properties = new ArrayList<TicketProperty>();
		// Setup user info
		properties.add(new TicketProperty("schacHomeOrganization", "au.dk"));
		properties.add(new TicketProperty("eduPersonScopedAffiliation", "some role"));
		properties.add(new TicketProperty("eduPersonTargetedID", "1x1"));
		// Setup program info
		properties.add(new TicketProperty("metaChannelName", null));
		properties.add(new TicketProperty("metaTitle", "Nyheder;"));
		properties.add(new TicketProperty("metaDateTimeStart", "2007-03-04T00:00:00+0100"));
		Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties );
		Event logEvent = Event.STREAMING_START;
		String timestamp = "2010-11-15 17:31:05.749";
		// Test
		StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, defaultStream, logEvent, ticket);
		logEntry.setTimestamp(sdf.parse(timestamp));
		// Validate
		String expectedLogString = "2010-11-15 17:31:05.749;uniqueStreamIdStr;STREAMING_START;1x1;some role;au.dk;-;Nyheder[semicolon];2007-03-04T00:00:00+0100";
		assertEquals("Log entry", expectedLogString, logEntry.getLogString());
	}
	
	@Test
	public void testCreateMap() {
		List<TicketProperty> properties = new ArrayList<TicketProperty>();
		String name = "schacHomeOrganization"; 
		String value = "au.dk";
		properties.add(new TicketProperty(name, value));
		Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties );
		StreamingStatLogEntry logEntry = new StreamingStatLogEntry(logger, defaultStream, Event.STREAMING_START, ticket);
		Map<String, String> map = logEntry.createMap(properties);
		assertEquals(value, map.get(name));
	}

	@Test
	public void testExtractLogEntry() throws InvalidLogLineParseException, HeadlineEncounteredException {
		List<TicketProperty> properties = new ArrayList<TicketProperty>();
		// Setup user info
		properties.add(new TicketProperty("schacHomeOrganization", "au.dk"));
		properties.add(new TicketProperty("eduPersonScopedAffiliation", "some role"));
		properties.add(new TicketProperty("eduPersonTargetedID", "1x1"));
		// Setup program info
		properties.add(new TicketProperty("metaChannelName", "tv2news"));
		properties.add(new TicketProperty("metaTitle", "Nyheder"));
		properties.add(new TicketProperty("metaDateTimeStart", "2007-03-04T00:00:00+0100"));
		Ticket ticket = ticketTool.issueTicket(defaultUsername, defaultResource, properties );
		Event logEvent = Event.STREAMING_START;
		// Test
		StreamingStatLogEntry originalLogEntry = new StreamingStatLogEntry(logger, defaultStream, logEvent, ticket);
		String logEntryString = originalLogEntry.getLogString();
		logger.info("Logline as string is: " + logEntryString);
		StreamingStatLogEntry resultingLogEntry = new StreamingStatLogEntry(logger, logEntryString);
		// Validate
		assertEquals("StreamingStatLogEntry value", Event.STREAMING_START, resultingLogEntry.getEvent());
		assertEquals("StreamingStatLogEntry value", defaultStream.getUniqueStreamIdStr(), resultingLogEntry.getConnectionID());
		assertEquals("StreamingStatLogEntry value", "au.dk", resultingLogEntry.getOrganisationID());
		assertEquals("StreamingStatLogEntry value", "1x1", resultingLogEntry.getUserID());
		assertEquals("StreamingStatLogEntry value", "some role", resultingLogEntry.getUserRole());
		assertEquals("StreamingStatLogEntry value", "tv2news", resultingLogEntry.getChannelID());
		assertEquals("StreamingStatLogEntry value", "Nyheder", resultingLogEntry.getProgramTitle());
		assertEquals("StreamingStatLogEntry value", "2007-03-04T00:00:00+0100", resultingLogEntry.getProgramStart());
		assertEquals("StreamingStatLogEntry objects", originalLogEntry, resultingLogEntry);
	}
	
	@Test
	public void testExtractLogEntryThrowingHeadlineEncounteredException() throws InvalidLogLineParseException {
		// Test
		String logEntryString = StreamingStatLogEntry.getLogStringHeadline();
		logger.info("Logline as string is: " + logEntryString);
		StreamingStatLogEntry logEntry;
		try {
			logEntry = new StreamingStatLogEntry(logger, logEntryString);
			fail();
		} catch (HeadlineEncounteredException e) {
			//Expected behavior
		}
	}
	
	@Test
	public void testExtractLogEntryThrowingInvalidLogLineParseException() throws HeadlineEncounteredException {
		// Test
		String logEntryString = "Some unparseable text";
		logger.info("Logline as string is: " + logEntryString);
		StreamingStatLogEntry logEntry;
		try {
			logEntry = new StreamingStatLogEntry(logger, logEntryString);
			fail();
		} catch (InvalidLogLineParseException e) {
			//Expected behavior
		}
	}
	
	private String issueStandardTicket() {
		List<TicketProperty> props = new ArrayList<TicketProperty>();
		TicketProperty prop = new TicketProperty();
		prop.setName("eduPersonTargetedID");
		prop.setValue(defaultEduPersonTargetedID);
		props.add(prop);
		String ticketID = ticketTool.issueTicket(defaultUsername, defaultResource, props).getID();
		return ticketID;
	}

	@Override
	public String toString() {
		return "StreamingStatLogEntryTest [logger=" + logger + ", ticketTool="
				+ ticketTool + ", defaultStream=" + defaultStream
				+ ", defaultIClient=" + defaultIClient
				+ ", defaultIAppInstance=" + defaultIAppInstance + "]";
	}

}
