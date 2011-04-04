package dk.statsbiblioteket.doms.wowza.plugin.ticket;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Checksums;


public class TicketTool implements TicketToolInterface {

	private WMSLogger logger;
    private WebResource restApi;

	public TicketTool(String serviceURL, WMSLogger logger) {
		super();
        Client client = Client.create();
        restApi = client.resource(serviceURL);
        this.logger = logger;
	}

	@Override
	public Ticket issueTicket(String username, String resource, List<TicketProperty> properties) {
        try {                
        	WebResource query = restApi
            .path("/issueTicket")
            .queryParam("username", username)
            .queryParam("resource", resource);
        	for (Iterator<TicketProperty> i = properties.iterator(); i.hasNext();) {
        		TicketProperty prop = i.next();
        		query.queryParam(prop.getName(), prop.getValue());
        	}
            Ticket ticketXml = query.post(Ticket.class);
            return ticketXml;
        }  catch (UniformInterfaceException e) {
        	throw new RuntimeException("Unexpected event", e);
        }
	}

	/* (non-Javadoc)
	 * @see dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketToolInterface#resolveTicket(java.lang.String)
	 */
	@Override
	public Ticket resolveTicket(String ticketID) {
		try {                
			Ticket ticketXml = restApi
			.path("/resolveTicket")
			.queryParam("ID", ticketID)
			.get(Ticket.class);
			return ticketXml;

		}  catch (UniformInterfaceException e) {
			logger.warn("UniformInterfaceException occured. Ticket might be invalidated.", e);
			return null;
		}
	}

	public static void main(String args[]) {
		String serviceURL = args[0];
		String username = args[1];
		String resource = args[2];
		String streamingURL = args[3];
		String filename = args[4];
		String fileExtension = filename.substring(filename.length()-3);
		String filenameWithoutExtension = filename.substring(0, filename.length()-4);
		System.out.println("---===<<< Ticket input parameters: >>>===---");
		System.out.println("Ticket server    : " + serviceURL);
		System.out.println("Username         : " + username);
		System.out.println("Resource         : " + resource);
		System.out.println("---===<<< Client input parameters: >>>===---");
		System.out.println("Streaming server : " + streamingURL);
		System.out.println("Filename         : " + filename);
		System.out.println("Filename (no ext): " + filenameWithoutExtension);
		System.out.println("File extension   : " + fileExtension);
		System.out.println("");
		System.out.print("Retrieving ticket...");
		TicketToolInterface ticketTool = new TicketTool(serviceURL, WMSLoggerFactory.getLogger(TicketTool.class));
		Ticket ticket = ticketTool.issueTicket(username, resource, new ArrayList<TicketProperty>());
		System.out.println("[Success]");
		System.out.println(ticket.toString());
		System.out.println("");
		System.out.println("");
		System.out.println("Input parameters for Wowza's test client <Wowza-install-dir>/examples/SimpleVideoStreaming/client/simplevideostreaming.html");
		System.out.println("");
		System.out.println("-[kultur]------------------------------------------------------------------------------------------------------------------");
		System.out.println("Server : " + streamingURL + "/kultur?ticket=" + ticket.getID());
		System.out.println("Stream : " + fileExtension + ":" + filename);
		System.out.println("---------------------------------------------------------------------------------------------------------------------------");
		System.out.println("");
		System.out.println("-[kultur_live]-------------------------------------------------------------------------------------------------------------");
		System.out.println("Server : " + streamingURL + "/kultur_live?ticket=" + ticket.getID());
		System.out.println("Stream : " + "stream" + ":" + Bytes.toHex(Checksums.md5(ticket.getID())) + ".stream");
		System.out.println("---------------------------------------------------------------------------------------------------------------------------");
		
	}
}
