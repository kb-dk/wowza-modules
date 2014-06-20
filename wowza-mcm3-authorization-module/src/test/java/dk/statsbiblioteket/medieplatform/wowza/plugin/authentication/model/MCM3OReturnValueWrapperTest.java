package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import com.wowza.wms.logging.WMSLoggerFactory;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.MCM3OReturnValueWrapper;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Test return value parsing from MCM3. */
public class MCM3OReturnValueWrapperTest {

    private String filenameOfvalidMCMOutputFull = getClass().getClassLoader().getResource(
            "test_data_MCM3_output_full.xml").getPath();
    private String filenameOfvalidMCMOutputInvalidSession = getClass().getClassLoader().getResource(
        "test_data_MCM3_output_invalid_session.xml").getPath();
    private String filenameOfInvalidMCMOutput = getClass().getClassLoader().getResource(
        "test_data_invalid_output.xml").getPath();

    private Logger logger;

    public MCM3OReturnValueWrapperTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
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
    public void testExtractOutputFilename() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
        MCM3OReturnValueWrapper returnWrapper = new MCM3OReturnValueWrapper(logger, is);
        String returnedValue = returnWrapper.getFilenames().get(0);
        String expectedValue = "P1_0000_000850_00000000_2023_Tysk propaganda-udsendelse - Reportage fra østfronten.mp3";
        assertEquals("Filename", expectedValue, returnedValue);
    }

    @Test
    public void testExtractOutputObjectID() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
        MCM3OReturnValueWrapper returnWrapper = new MCM3OReturnValueWrapper(logger, is);
        String returnedValue = returnWrapper.getObjectID();
        String expectedValue = "84802737-4910-d941-a7f0-14e4bdf1bc4d";
        assertEquals("ObjectID", expectedValue, returnedValue);
    }

    @Test
    public void testExtractOutputInvalidSessionID() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputInvalidSession);
        MCM3OReturnValueWrapper returnWrapper = new MCM3OReturnValueWrapper(logger, is);
        boolean returnedValue = returnWrapper.isSessionValid();
        boolean expectedValue = false; 
        assertEquals("Valid session", expectedValue, returnedValue);
    }

    @Test(expected=MCMOutputException.class)
    public void testExtractOutputBogusXML() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfInvalidMCMOutput);
        new MCM3OReturnValueWrapper(logger, is);
    }

    @Test
    public void testExtractMultipleFilenames() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
        MCM3OReturnValueWrapper returnWrapper = new MCM3OReturnValueWrapper(logger, is);
        List<String> returnedFilenames = returnWrapper.getFilenames();
        String expectedValue1 = "P1_0000_000850_00000000_2023_Tysk propaganda-udsendelse - Reportage fra østfronten.mp3";
        String expectedValue2 = "P1_logo.png";
        assertTrue("Filename", returnedFilenames.contains(expectedValue1));
        assertTrue("Filename", returnedFilenames.contains(expectedValue2));
    }

    private InputStream getTestDataFileAsInputStream(String inputstring) throws FileNotFoundException {
        FileInputStream fis;
        try {
            // If run from ant in command line
            fis = new FileInputStream(inputstring);
        } catch (FileNotFoundException e) {
            // If run from Eclipse
            fis = new FileInputStream("trunk/" + inputstring);
        }
        return fis;
    }
}
