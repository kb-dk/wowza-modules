package dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects;

import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketToolMock implements TicketToolInterface {

    private int counter;
    private HashMap<String, Ticket> ticketMap;

    public TicketToolMock() {
        super();
        this.ticketMap = new HashMap<String, Ticket>();
        this.counter = 0;
    }

    public synchronized Ticket issueTicket(
            String username,
            String resource, List<Property> properties) {

        Map<String, List<String>> propertiesMap = convert(properties);
        Ticket ticket = new Ticket(
                "Stream", username, Arrays.asList(resource), propertiesMap);
        ticketMap.put(ticket.getId(), ticket);
        return ticket;
    }

    private Map<String, List<String>> convert(List<Property> properties) {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        for (Property property : properties) {
            List<String> list = result.get(property.getName());
            if (list ==null){
                list = new ArrayList<String>();
            }
            list.add(property.getValue());
            result.put(property.getName(),list);
        }
        return result;
    }

    @Override
    public synchronized Ticket resolveTicket(String ticketID) {
        Ticket ticket = ticketMap.get(ticketID);
        return ticket;
    }

}
