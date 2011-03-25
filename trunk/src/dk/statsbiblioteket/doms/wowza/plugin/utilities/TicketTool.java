package dk.statsbiblioteket.doms.wowza.plugin.utilities;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import dk.statsbiblioteket.doms.wowza.plugin.Ticket;

public class TicketTool {

    private WebResource restApi;

	public TicketTool(String serviceURL) {
		super();
        Client client = Client.create();
        restApi = client.resource(serviceURL);
	}

	public Ticket issueTicket(String username, String resource) {
        try {                
            Ticket ticketXml = restApi
                    .path("/issueTicket")
                    .queryParam("username", username)
                    .queryParam("resource", resource)
                    .post(Ticket.class);
            return ticketXml;
        }  catch (UniformInterfaceException e) {
        	throw new RuntimeException("Unexpected event", e);
        }
	}
	
	public Ticket resolveTicket(String ticket) {
		try {                
			Ticket ticketXml = restApi
			.path("/resolveTicket")
			.queryParam("ID", ticket)
			.get(Ticket.class);
			return ticketXml;

		}  catch (UniformInterfaceException e) {
        	throw new RuntimeException("Unexpected event", e);
		}
	}

	public static void main(String args[]) {
		String serviceURL = args[0];
		String username = args[1];
		String resource = args[2];
		String streamingURL = args[3];
		String filename = args[4];
		System.out.println("---===<<< Input parameters: >>>===---");
		System.out.println("Ticket server    : " + serviceURL);
		System.out.println("Username         : " + username);
		System.out.println("URL              : " + resource);
		System.out.println("Streaming server : " + streamingURL);
		System.out.println("Filename         : " + filename);
		System.out.println("");
		System.out.print("Retrieving ticket...");
		TicketTool ticketTool = new TicketTool(serviceURL);
		Ticket ticket = ticketTool.issueTicket(username, resource);
		String fileExtension = filename.substring(filename.length()-3);
		System.out.println("[Success]");
		System.out.println("Input parameters for Wowza's test client <Wowza-install-dir>/examples/SimpleVideoStreaming/client/simplevideostreaming.html");
		System.out.println("---------------------------------------------------------------------------------------------------------------------------");
		System.out.println("Server : " + streamingURL + "/?shard=" + ticket.getResource() + "&ticket=" + ticket.getID());
		System.out.println("Stream : " + fileExtension + ":" + filename);
		System.out.println("---------------------------------------------------------------------------------------------------------------------------");
		
	}
}
