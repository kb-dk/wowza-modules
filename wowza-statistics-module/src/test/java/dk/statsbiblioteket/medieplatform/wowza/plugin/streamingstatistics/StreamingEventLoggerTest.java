package dk.statsbiblioteket.medieplatform.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StreamingEventLoggerTest extends TestCase {

    private WMSLogger logger;
    private TicketToolInterface ticketTool;

    public StreamingEventLoggerTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        ticketTool = mock(TicketToolInterface.class);  
    }

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
    }

    @After
    public void tearDown() throws Exception {
        BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testWriteEventLogAppendToExisting() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
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
        File logDir = new File(logFolder);
        if (logDir.isDirectory()) {
            // Check that there is just one file
            String[] children = logDir.list();
            assertTrue(children.length != 3);  // Including dirs . and ..

            // Check that the right date is in the filename
            File file = new File(logDir, children[0]);
            String filename = file.getName();
            Date now = new Date();
            assertTrue(filename.contains(sdf.format(now)));

            // Check that the file contains the right amount of entries
            assertTrue(getAmountOfLinesInFile(file) == 701);
        }
    }

    private int getAmountOfLinesInFile(File file) {
        BufferedReader br;
        try {
            br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return 0;
        }

        int numberOfEntries = 0;
        try {
            while (br.readLine() != null) {
                numberOfEntries++;
            }
        } catch(Exception e) {
            return 0;
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                return 0;
            }
        }
        return numberOfEntries;
    }

    @Test
    public void testGetStatLogWriterChangingLogFile() throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
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
}
