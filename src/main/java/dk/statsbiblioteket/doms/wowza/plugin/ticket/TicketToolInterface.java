package dk.statsbiblioteket.doms.wowza.plugin.ticket;

public interface TicketToolInterface {
    public abstract dk.statsbiblioteket.medieplatform.ticketsystem.Ticket resolveTicket(String ticketID);
}