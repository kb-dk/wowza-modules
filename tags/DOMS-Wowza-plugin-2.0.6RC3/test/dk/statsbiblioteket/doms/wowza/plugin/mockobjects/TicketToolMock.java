package dk.statsbiblioteket.doms.wowza.plugin.mockobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketProperty;
import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketToolInterface;

public class TicketToolMock implements TicketToolInterface {

	private int counter;
	private HashMap<String, Ticket> ticketMap;
		
	public TicketToolMock() {
		super();
		this.ticketMap = new HashMap<String, Ticket>();
		this.counter = 0;
	}

	@Override
	public synchronized Ticket issueTicket(String username, String resource, List<TicketProperty> properties) {
		String id = "ticket-id-" + counter++;
		Ticket ticket = new Ticket(id, resource, username, properties);
		ticketMap.put(id, ticket);
		return ticket;
	}

	@Override
	public synchronized Ticket resolveTicket(String ticketID) {
		Ticket ticket = ticketMap.get(ticketID);
		return ticket;
	}

}
