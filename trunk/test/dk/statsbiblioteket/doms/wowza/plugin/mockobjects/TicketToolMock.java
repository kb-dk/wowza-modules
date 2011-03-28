package dk.statsbiblioteket.doms.wowza.plugin.mockobjects;

import java.util.HashMap;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketToolInterface;

public class TicketToolMock implements TicketToolInterface {

	private int counter;
	private HashMap<String, Ticket> ticketMap;
		
	public TicketToolMock() {
		super();
		this.ticketMap = new HashMap<String, Ticket>();
		this.counter = 0;
	}

	@Override
	public Ticket issueTicket(String username, String resource) {
		String id = "ticket-id-" + counter++;
		Ticket ticket = new Ticket(id, resource, username);
		ticketMap.put(id, ticket);
		return ticket;
	}

	@Override
	public Ticket resolveTicket(String ticketID) {
		Ticket ticket = ticketMap.get(ticketID);
		return ticket;
	}

}
