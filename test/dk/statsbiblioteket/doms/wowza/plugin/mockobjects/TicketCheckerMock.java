package dk.statsbiblioteket.doms.wowza.plugin.mockobjects;

import dk.statsbiblioteket.doms.wowza.plugin.ticket.TicketCheckerInterface;

public class TicketCheckerMock implements TicketCheckerInterface {

	private boolean validateSession;
	
	public TicketCheckerMock() {
		super();
		this.validateSession = true;
	}

	public TicketCheckerMock(boolean validateSession) {
		super();
		this.validateSession = validateSession;
	}

	@Override
	public boolean isTicketValid(String ticket, String shardUrl, String ipOfPlayer) {
		return validateSession;
	}

}
