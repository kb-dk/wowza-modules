package dk.statsbiblioteket.doms.wowza.plugin.utilities;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.doms.wowza.plugin.Ticket;

public class TicketToolTest  extends TestCase {

	private Logger logger;
	
	public TicketToolTest() {
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
	public void testGenerateTicket() {
		// Setup environment
		TicketTool ticketTool = new TicketTool("http://alhena:7880/authchecker-service/");
		String username = "aUsername";
		String resource = "anURL";
		Ticket ticket = ticketTool.issueTicket(username, resource);
		logger.debug("Ticket: " + ticket);
		assertEquals("Expected equal result", username, 
				ticket.getUsername());
		assertEquals("Expected equal result", resource, 
				ticket.getResource());
		
	}

	@Test
	public void testValidateTicket() {
		// Setup environment
		TicketTool ticketTool = new TicketTool("http://alhena:7880/authchecker-service/");
		String username = "aUsername";
		String url = "anURL";
		Ticket issuedTicket = ticketTool.issueTicket(username, url);
		logger.debug("Issued ticket: " + issuedTicket);
		String ticketID = issuedTicket.getID();
		Ticket resolvedTicket = ticketTool.resolveTicket(ticketID);
		logger.debug("Resolved ticket: " + resolvedTicket);
		assertEquals(issuedTicket, resolvedTicket);
	}

}
