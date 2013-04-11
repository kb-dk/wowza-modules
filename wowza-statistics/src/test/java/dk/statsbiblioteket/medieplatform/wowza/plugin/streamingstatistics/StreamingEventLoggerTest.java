package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.TicketToolMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StreamingEventLoggerTest extends TestCase {

    private WMSLogger logger;
    private TicketToolMock ticketTool;

    // Default test values
    private String defaultStatLogDir = "/log/dir";
    private String defaultUsername = "127.0.0.1";
    private String defaultResource = "a0639529-124a-453f-b4ea-59f833b47333";
    private String defaultEduPersonTargetedID = "0123456789abcd";

    public StreamingEventLoggerTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        ticketTool = new TicketToolMock();
    }

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
    }

    @After
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testGetTicket() {
        StreamingEventLogger eventLogger = new StreamingEventLogger(ticketTool, logger, defaultStatLogDir);
        String ticketIDOrig = issueStandardTicket();
        String ticketParam = "ticket=" + ticketIDOrig;
        try {
            Ticket ticket = eventLogger.getTicket(ticketParam);
            assertEquals(ticketIDOrig, ticket.getId());
        } catch (IllegallyFormattedQueryStringException e) {
            fail();
        }
    }

    @Test
    public void testWriteEventLogAppendToExisting() {
        String logFolder = "target/tmp/unit-test/" + this.getClass().getSimpleName() + "/logs";
        deleteDir(logFolder);
        createDir(logFolder);
        StreamingEventLogger eventLogger = new StreamingEventLogger(ticketTool, logger, logFolder);
        for (int i = 0; i < 350; i++) {
            eventLogger.writeEventLog("First eventlog number: " + i);
        }
        // Simulate Wowza restart and start new event logger
        eventLogger = new StreamingEventLogger(ticketTool, logger, logFolder);
        for (int i = 0; i < 350; i++) {
            eventLogger.writeEventLog("Second eventlog number: " + i);
        }
        // Check that the file has been created and contains log entries.
    }

    @Test
    public void testGetStatLogWriterChangingLogFile() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String logFolder = "target/tmp/unit-test/" + this.getClass().getSimpleName() + "/logs";
        deleteDir(logFolder);
        createDir(logFolder);
        StreamingEventLogger eventLogger = new StreamingEventLogger(ticketTool, logger, logFolder);
        Writer beforeWriter = eventLogger.getStatLogWriter();
        Writer afterWriter = eventLogger.getStatLogWriter();
        assertTrue("Same writer expected.", beforeWriter.equals(afterWriter));
        eventLogger.setDateForNewLogFile(sdf.parse("2000-01-01"));
        afterWriter = eventLogger.getStatLogWriter();
        assertFalse("New writer expected.", beforeWriter.equals(afterWriter));
    }

    @Test
    public void testGetFollowingMidnight() throws IOException, ParseException {
        // Setup
        String logFolder = "target/tmp/unit-test/" + this.getClass().getSimpleName() + "/logs";
        deleteDir(logFolder);
        createDir(logFolder);
        StreamingEventLogger eventLogger = new StreamingEventLogger(ticketTool, logger, logFolder);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date someDate = sdf.parse("2011-01-14 13:20");
        // Test
        Date followingMidnight = eventLogger.getFollowingMidnight(someDate);
        assertTrue("Evaluating the following midnight.", sdf.format(followingMidnight).equals("2011-01-15 00:00"));
    }

    private void createDir(String folderPath) {
        File targetFolder = new File(folderPath);
        if (!(targetFolder.isDirectory())) {
            targetFolder.mkdirs();
        }
    }

    private boolean deleteDir(String folderPath) {
        File dir = new File(folderPath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]).getAbsolutePath());
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    private String issueStandardTicket() {
        List<Property> props = new ArrayList<Property>();
        Property prop = new Property();
        prop.setName("eduPersonTargetedID");
        prop.setValue(defaultEduPersonTargetedID);
        props.add(prop);
        String ticketID = ticketTool.issueTicket(defaultUsername, defaultResource, props).getId();
        return ticketID;
    }

}
