package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

import com.wowza.wms.logging.WMSLoggerFactory;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


/**
 * Tests for parsing XML results in the return value wrapper.
 */
public class MCMOReturnValueWrapperTest {

    private String filenameOfvalidMCMOutputFull = getClass().getClassLoader().getResource(
        "test_data_MCM_output_full.xml").getPath();
    private String filenameOfvalidMCMOutputMultipleFiles = getClass().getClassLoader().getResource(
        "test_data_MCM_output_multiple_files.xml").getPath();
    private String filenameOfvalidMCMOutputInvalidSession = getClass().getClassLoader().getResource(
        "test_data_MCM_output_invalid_session.xml").getPath();
    private String filenameOfInvalidMCMOutput = getClass().getClassLoader().getResource(
        "test_data_invalid_output.xml").getPath();

    private Logger logger;

    public MCMOReturnValueWrapperTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
    }

    @AfterEach
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testExtractOutputFilename() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
        MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
        String returnedValue = returnWrapper.getFilenames().get(0);
        String expectedValue = "P1_0000_0200_910201_001.mp3"; 
        assertEquals(expectedValue, returnedValue, "Filename");
    }

    @Test
    public void testExtractOutputObjectID() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputFull);
        MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
        String returnedValue = returnWrapper.getObjectID();
        String expectedValue = "643703"; 
        assertEquals(expectedValue, returnedValue, "ObjectID");
    }

    @Test
    public void testExtractOutputInvalidSessionID() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputInvalidSession);
        MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
        boolean returnedValue = returnWrapper.isSessionValid();
        boolean expectedValue = false; 
        assertEquals(expectedValue, returnedValue, "Valid session");
    }

    @Test
    public void testExtractOutputBogusXML() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfInvalidMCMOutput);
        assertThrows(MCMOutputException.class, () -> {
            new MCMOReturnValueWrapper(logger, is);
          });
        
    }

    @Test
    public void testExtractMultipleFilenames() throws FileNotFoundException, MCMOutputException {
        InputStream is = getTestDataFileAsInputStream(filenameOfvalidMCMOutputMultipleFiles);
        MCMOReturnValueWrapper returnWrapper = new MCMOReturnValueWrapper(logger, is);
        List<String> returnedFilenames = returnWrapper.getFilenames();
        String expectedValue1 = "P2_1800_2000_890121_001.mp3"; 
        String expectedValue2 = "P2_2000_2200_890121_001.mp3";
        assertTrue(returnedFilenames.contains(expectedValue1), "Filename");
        assertTrue(returnedFilenames.contains(expectedValue2), "Filename");
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
