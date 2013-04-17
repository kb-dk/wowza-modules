package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

public interface TicketToolInterface {
    public abstract Ticket resolveTicket(String ticketID);
}