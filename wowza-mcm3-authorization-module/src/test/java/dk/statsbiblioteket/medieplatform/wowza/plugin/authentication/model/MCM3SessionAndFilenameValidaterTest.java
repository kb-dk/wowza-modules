package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.MCM3SessionAndFilenameValidater;

import org.apache.log4j.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Test MCM3 authorization validator */
public class MCM3SessionAndFilenameValidaterTest {

    private Logger logger;

    public MCM3SessionAndFilenameValidaterTest() {
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
    public void testValidateFilerequestWithMCMResultUnixStylePath() throws MCMOutputException, IOException {
        WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
        String pathAndFilename = "Kulturarv_MP3/Batch01/Disc02/mp3_128kbps/P1_0000_0200_910201_001.mp3";
        List<String> validatingFilenames = new ArrayList<String>();
        validatingFilenames.add("P1_0000_0200_910201_001.mp3");
        MCM3SessionAndFilenameValidater validater = new MCM3SessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer");
        boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);
        
        assertEquals(true, doesValidate, "Filename validation:");
    }

    @Test
    public void testValidateFilerequestWithMCMResultWindowsStylePath() throws MCMOutputException, IOException {
        WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
        String pathAndFilename = "Kulturarv_MP3\\Batch01\\Disc02\\mp3_128kbps\\P1_0000_0200_910201_001.mp3";
        List<String> validatingFilenames = new ArrayList<String>();
        validatingFilenames.add("P1_0000_0200_910201_001.mp3");
        MCM3SessionAndFilenameValidater validater = new MCM3SessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer");
        boolean doesValidate = validater.validateFilerequestWithMCMResult(pathAndFilename, validatingFilenames);
        assertEquals(true, doesValidate, "Filename validation:");
    }
}
