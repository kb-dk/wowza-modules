package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

public interface TicketToolInterface {
    public abstract dk.statsbiblioteket.medieplatform.ticketsystem.Ticket resolveTicket(String ticketID);
}