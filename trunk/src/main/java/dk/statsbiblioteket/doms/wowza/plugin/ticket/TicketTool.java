package dk.statsbiblioteket.doms.wowza.plugin.ticket;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse.Status;
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
        		query = query.queryParam(prop.getName(), prop.getValue());
        	}
        	//System.out.println("Query parameters: " + query.toString());
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
			logger.info("resolveTicket: Ticket received.");
			return ticketXml;

		}  catch (UniformInterfaceException e) {
			// If the ticket does not exist, i.e. the session has timed out.
			Status responseStatus = e.getResponse().getClientResponseStatus();
			logger.info("The session might have timed out. Ticket service response status: " + responseStatus.getStatusCode());
			return null;
		}
	}

	public static void main(String args[]) {
		String serviceURL = args[0];
		String username = args[1];
		String resource = args[2];
		String organisationID = args[3];
		String userID = args[4];
		String channelID = args[5];
		String programTitle = args[6];
		String programStart = args[7];
		
		String streamingURL = args[8];
		String filename = args[9];
		String fileExtension = filename.substring(filename.length()-3);
		String filenameWithoutExtension = filename.substring(0, filename.length()-4);

		String previewFilename = args[10];
		String previewFileExtension = filename.substring(filename.length()-3);
		String previewFilenameWithoutExtension = filename.substring(0, filename.length()-4);

		String invalidTicketID = "some-invalid-ticket-id";
		
		List<TicketProperty> ticketProperties = new ArrayList<TicketProperty>();
		ticketProperties.add(new TicketProperty("schacHomeOrganization", organisationID));
		ticketProperties.add(new TicketProperty("eduPersonTargetedID", userID));
		ticketProperties.add(new TicketProperty("metaChannelName", channelID));
		ticketProperties.add(new TicketProperty("metaTitle", programTitle));
		ticketProperties.add(new TicketProperty("metaDateTimeStart", programStart));
		
		System.out.println("---===<<< Ticket input parameters: >>>===---");
		System.out.println("Ticket server    : " + serviceURL);
		System.out.println("Username         : " + username);
		System.out.println("Resource         : " + resource);
		for	(Iterator<TicketProperty> i=ticketProperties.iterator();i.hasNext();) {
			System.out.println("Property         : " + i.next().toString());
		}
		System.out.println("---===<<< Client input parameters: >>>===---");
		System.out.println("Streaming server         : " + streamingURL);
		System.out.println("Filename                 : " + filename);
		System.out.println("Filename (no ext)        : " + filenameWithoutExtension);
		System.out.println("File extension           : " + fileExtension);
		System.out.println("Preview filename         : " + previewFilename);
		System.out.println("Preview filename (no ext): " + previewFilenameWithoutExtension);
		System.out.println("Preview file extension   : " + previewFileExtension);
		System.out.println("");
		System.out.print("Retrieving ticket...");
		TicketToolInterface ticketTool = new TicketTool(serviceURL, WMSLoggerFactory.getLogger(TicketTool.class));
		Ticket ticket = ticketTool.issueTicket(username, resource, ticketProperties);
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
		System.out.println("-[kultur_preview]----------------------------------------------------------------------------------------------------------");
		System.out.println("Server : " + streamingURL + "/kultur_preview");
		System.out.println("Stream : " + previewFileExtension + ":" + previewFilename);
		System.out.println("---------------------------------------------------------------------------------------------------------------------------");
		
	}
}
