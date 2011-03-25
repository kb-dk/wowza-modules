package dk.statsbiblioteket.doms.wowza.plugin.kultur;

import java.io.File;

import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.doms.wowza.plugin.TicketCheckerInterface;

public class TicketToFileMapper implements IMediaStreamFileMapper {

	private TicketCheckerInterface ticketChecker;  
	
	public TicketToFileMapper(TicketCheckerInterface ticketChecker) {
		super();
		this.ticketChecker = ticketChecker;
	}

	@Override
	public File streamToFileForRead(IMediaStream stream) {
		
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
