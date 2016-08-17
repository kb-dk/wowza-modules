package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.MediaStreamActionNotify3Base;

/**
 * Action notifier that kills a stream that is not allowed by the ticket.
 */
class StreamAuthenticator extends MediaStreamActionNotify3Base {
    
    private TicketChecker ticketChecker;

    /**
     * Initialise the notifier.
     *
     * @param ticketChecker The ticket checker to use when checking tickets.
     */
    public StreamAuthenticator(TicketChecker ticketChecker) {
        super();
        this.ticketChecker = ticketChecker;
    }

    /**
     * Check the stream name against a ticket extracted from the stream.
     * Close the stream if the ticket does not allow this stream.
     * Called when a stream gets a play event.
     *
     * @param stream The stream that is checked by the ticket checker.
     * @param streamName Name of stream. Not used.
     * @param playStart Play start. Not used.
     * @param playLen Play length. Not used.
     * @param playReset Play reset. Not used.
     */
    public void onPlay(IMediaStream stream, String streamName, double playStart,
            double playLen, int playReset) {
        if (!ticketChecker.checkTicket(stream, stream.getClient())) {
            stream.getClient().rejectConnection("Streaming not allowed");
            stream.sendStreamNotFound("Streaming not allowed");
            stream.getClient().setShutdownClient(true);
            stream.getClient().shutdownClient();
        }
    }
}
