package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.stream.IMediaStream;
import org.codehaus.jackson.map.ObjectMapper;

import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StreamingStatLogEntry {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    // LIVE_STREAMING_START is for streaming a live-recorded (or not recorded) signal, and not used by SB
    public enum Event {LIVE_STREAMING_START, STREAMING_START, PLAY, PAUSE, STOP, SEEK, STREAMING_END}

    // Log information
    private Date timestamp;

    private String streamingURL;
    private Event event;

    // Ticket information
    private boolean wasTicketAttached;
    private String userAttributesAsJson;  // Contains user roles

    public StreamingStatLogEntry(IMediaStream stream, Event event,
                                 Ticket streamingTicket) {
        this.setTimestamp(new Date());


        // TODO the below needs testing to see if we got it right
        this.streamingURL = stream.getClient().getUri() + '?' + stream.getClient().getQueryStr() + '/' +  stream.getExt()
                + ':' + stream.getName() + ':';

        this.event = event;

        this.wasTicketAttached = (streamingTicket != null);
        if (this.wasTicketAttached) {
            retrieveTicketInformation(streamingTicket);
        } else {
            this.userAttributesAsJson = null;
        }

    }

    /**
     * Extract information for the log line from a streaming ticket
     * @param streamingTicket The ticket from which to extract information for the log line
     */
    private void retrieveTicketInformation(dk.statsbiblioteket.medieplatform.ticketsystem.Ticket streamingTicket) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            userAttributesAsJson = mapper.writeValueAsString(streamingTicket.getUserAttributes());
        } catch (IOException e) {
            userAttributesAsJson = null;
        }
    }

    protected void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Event getEvent() {
        return event;
    }

    /**
     * Build the log line from gathered information
     * @return The log line
     */
    public String getLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sdf.format(timestamp));
        sb.append(";");
        sb.append(getEvent());
        sb.append(";");
        sb.append(escapeLogString(streamingURL));
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
        sb.append("Connection ID");
        sb.append(";");
        sb.append("Event");
        sb.append(";");
        sb.append("User ID");
        sb.append(";");
        sb.append("User Role");
        sb.append(";");
        sb.append("Organization ID");
        sb.append(";");
        sb.append("Channel ID");
        sb.append(";");
        sb.append("Program UUID");
        sb.append(";");
        sb.append("Program title");
        sb.append(";");
        sb.append("Program start");
        return sb.toString();
    }

    protected String escapeLogString(String logLine) {
        return logLine.replaceAll(";", "[semicolon]");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + (wasTicketAttached ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StreamingStatLogEntry other = (StreamingStatLogEntry) obj;
        if (event != other.event) {
            return false;
        }
        if (timestamp == null) {
            if (other.timestamp != null) {
                return false;
            }
        } else if (!timestamp.equals(other.timestamp)) {
            return false;
        }
        if (wasTicketAttached != other.wasTicketAttached) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StreamingStatLogEntry [timestamp=" + timestamp + ", event=" + event + ", wasTicketAttached="
                + wasTicketAttached + "]";
    }


}
