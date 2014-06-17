package dk.statsbiblioteket.medieplatform.wowza.plugin.ticket;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

/** Resolve a ticket from a ticket ID. */
public interface TicketToolInterface {
    /** Resolve a ticket from a ticket ID.
     *
     * @param ticketID The ID of the ticket.
     * @return The resolved ticket.
     */
    public abstract Ticket resolveTicket(String ticketID);
}