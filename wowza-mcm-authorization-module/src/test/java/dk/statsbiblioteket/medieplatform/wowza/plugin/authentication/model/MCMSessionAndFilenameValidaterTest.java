package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Tests for the validator.
 */
public class MCMSessionAndFilenameValidaterTest {

    private MCMSessionAndFilenameValidater validater;

    public MCMSessionAndFilenameValidaterTest() {
        super();
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();

        WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
        validater = new MCMSessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer");
    }

    @AfterEach
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testValidateFilerequestWithMCMResultUnixStylePath() throws MCMOutputException, IOException {
        String pathAndFilename = "Kulturarv_MP3/Batch01/Disc02/mp3_128kbps/P1_0000_0200_910201_001";
        List<String> validatingFilenames = Arrays.asList("P1_0000_0200_910201_001.mp3", "anotherfile.mp3");

        boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);
        
        assertTrue(doesValidate);
    }

    @Test
    public void testValidateFilerequestWithMCMResultWindowsStylePath() throws MCMOutputException, IOException {
        String pathAndFilename = "Kulturarv_MP3\\Batch01\\Disc02\\mp3_128kbps\\P1_0000_0200_910201_001";
        List<String> validatingFilenames = Arrays.asList("P1_0000_0200_910201_001.mp3", "anotherfile.mp3");

        boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);

        assertTrue(doesValidate);
    }

    @Test
    public void testValidateFilerequestWithMCMResultNotFound() throws MCMOutputException, IOException {
        String pathAndFilename = "Kulturarv_MP3\\Batch01\\Disc02\\mp3_128kbps\\P1_0000_0200_910201_001";
        List<String> validatingFilenames = Arrays.asList("anotherfile.mp3");

        boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);

        assertFalse(doesValidate);
    }

    @Test
    public void testCleanFilename() throws Exception {
        assertEquals("test", validater.cleanFilename("/usr/local/test"));
        assertEquals("test", validater.cleanFilename("c:\\Documents\\test"));
        assertEquals("test", validater.cleanFilename("test.txt"));
        assertEquals("test.file", validater.cleanFilename("test.file.txt"));
        assertEquals("test", validater.cleanFilename("/usr/local/test.txt"));
        assertEquals("test", validater.cleanFilename("c:\\Documents\\test.mp3"));
    }
}
