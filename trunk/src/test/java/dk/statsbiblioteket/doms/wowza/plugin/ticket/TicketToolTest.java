package dk.statsbiblioteket.doms.wowza.plugin.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketTool;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;

public class TicketToolTest  extends TestCase {

	private WMSLogger logger;
	
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
		TicketToolInterface ticketTool = new TicketTool("http://alhena:7880/authchecker-service/tickets", logger);
		String username = "aUsername";
		String resource = "anURL";
		Ticket ticket = ticketTool.issueTicket(username, resource, new ArrayList<TicketProperty>());
		logger.debug("Ticket: " + ticket);
		assertEquals("Expected equal result", username, 
				ticket.getUsername());
		assertEquals("Expected equal result", resource, 
				ticket.getResource());
		assertEquals("Expected equal result", resource, 
				ticket.getResource());
		
	}

	@Test
	public void testValidateTicket() {
		// Setup environment
		TicketToolInterface ticketTool = new TicketTool("http://alhena:7880/authchecker-service/tickets", logger);
		String username = "aUsername";
		String url = "anURL";
		Ticket issuedTicket = ticketTool.issueTicket(username, url, new ArrayList<TicketProperty>());
		logger.debug("Issued ticket: " + issuedTicket);
		String ticketID = issuedTicket.getID();
		Ticket resolvedTicket = ticketTool.resolveTicket(ticketID);
		logger.debug("Resolved ticket: " + resolvedTicket);
		assertEquals(issuedTicket, resolvedTicket);
	}

}
