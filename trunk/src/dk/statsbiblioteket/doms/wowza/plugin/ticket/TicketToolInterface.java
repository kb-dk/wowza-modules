package dk.statsbiblioteket.doms.wowza.plugin.ticket;


public interface TicketToolInterface {

	public abstract Ticket issueTicket(String username, String resource);

	public abstract Ticket resolveTicket(String ticketID);

}