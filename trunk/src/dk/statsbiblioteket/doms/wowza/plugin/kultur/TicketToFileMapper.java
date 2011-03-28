package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import java.io.File;

import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.doms.wowza.plugin.Ticket;
import dk.statsbiblioteket.doms.wowza.plugin.TicketCheckerInterface;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.TicketToolInterface;

public class TicketToFileMapper implements IMediaStreamFileMapper {

	private TicketCheckerInterface ticketChecker;
	private TicketToolInterface ticketTool;
	
	public TicketToFileMapper(TicketCheckerInterface ticketChecker, TicketToolInterface ticketTool) {
		super();
		this.ticketChecker = ticketChecker;
		this.ticketTool = ticketTool;
	}

	@Override
	public File streamToFileForRead(IMediaStream stream) {
		String queryString = stream.getQueryStr();
		String ticketID = null;
		Ticket streamingTicket = ticketTool.resolveTicket(ticketID);
		return null;
	}

	@Override
	public File streamToFileForRead(IMediaStream arg0, String arg1,
			String arg2, String arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File streamToFileForWrite(IMediaStream arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File streamToFileForWrite(IMediaStream arg0, String arg1,
			String arg2, String arg3) {
		// TODO Auto-generated method stub
		return null;
	}

}
