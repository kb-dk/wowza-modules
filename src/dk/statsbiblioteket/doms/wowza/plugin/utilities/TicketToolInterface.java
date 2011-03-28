package dk.statsbiblioteket.doms.wowza.plugin.utilities;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.Ticket;

public interface TicketToolInterface {

	public abstract Ticket issueTicket(String username, String resource);

	public abstract Ticket resolveTicket(String ticketID);

}