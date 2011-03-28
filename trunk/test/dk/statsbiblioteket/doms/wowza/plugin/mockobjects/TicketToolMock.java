package dk.statsbiblioteket.doms.wowza.plugin.mockobjects;

import dk.statsbiblioteket.doms.wowza.plugin.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketToolInterface;

public class TicketToolMock implements TicketToolInterface {

	@Override
	public Ticket issueTicket(String username, String resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Ticket resolveTicket(String ticketID) {
		// TODO Auto-generated method stub
		return null;
	}

}
