package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.stream.IMediaStream;
import org.codehaus.jackson.map.ObjectMapper;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StreamingStatLogEntry {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    public enum Event {PLAY, PAUSE, STOP, SEEK}

    // Log information
    private Date timestamp;

    private String streamingURL;
    private Event event;

    private String userAttributesAsJson;  // Contains user roles

    public StreamingStatLogEntry(Event event, Ticket streamingTicket, String streamingURL) {
        this.timestamp = new Date();

        this.streamingURL = streamingURL;
        this.event = event;

        if ((streamingTicket != null)) {
            this.userAttributesAsJson = retrieveTicketInformation(streamingTicket);
        } else {
            this.userAttributesAsJson = null;
        }
    }

    /**
     * Extract information for the log line from a streaming ticket
     * @param streamingTicket The ticket from which to extract information for the log line
     */
    private static String retrieveTicketInformation(Ticket streamingTicket) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(streamingTicket.getUserAttributes());
        } catch (IOException e) {
            //Should never happen; string could not be read
            return null;
        }
    }

    public Event getEvent() {
        return event;
    }

    /**
     * Build the log line from gathered information
     * @return The log line
     */
    public String getLogString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        StringBuilder sb = new StringBuilder();
        sb.append(sdf.format(timestamp));
        sb.append(";");
        sb.append(getEvent());
        sb.append(";");
        sb.append(escapeLogString(streamingURL));
        sb.append(";");
        sb.append(escapeLogString(userAttributesAsJson));
        return sb.toString();
    }

    /**
     * Build the headline for the log
     * @return The headline for the log
     */
    public static String getLogStringHeadline() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp");
        sb.append(";");
        sb.append("Event");
        sb.append(";");
        sb.append("Streaming URL");
        sb.append(";");
        sb.append("User attributes");
        return sb.toString();
    }

    protected String escapeLogString(String logLine) {
        if (logLine == null) {
            return null;
        }
        return logLine.replaceAll(";", "[semicolon]");
    }

    @Override
    public String toString() {
        return "StreamingStatLogEntry: " + getLogString();
    }
}
