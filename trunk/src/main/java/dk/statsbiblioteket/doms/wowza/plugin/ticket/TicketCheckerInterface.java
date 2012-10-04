package dk.statsbiblioteket.doms.wowza.plugin.ticket;

public interface TicketCheckerInterface {

	/**
	 * TODO javadoc
	 *
	 * @param ticket
	 * @param shardUrl
	 * @param ipOfPlayer
	 * @return
	 */
	public abstract boolean isTicketValid(String ticket, String shardUrl,
			String ipOfPlayer);

}