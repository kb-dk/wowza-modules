package dk.statsbiblioteket.doms.wowza.plugin.ticket;

import java.util.List;

public interface TicketToolInterface {

    public abstract Ticket resolveTicket(String ticketID);

    public abstract Ticket issueTicket(String username, String resource, List<TicketProperty> properties);

}