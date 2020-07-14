package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.model;

import java.io.IOException;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.ChaosV6API;
import dk.statsbiblioteket.medieplatform.wowza.plugin.authentication.ChaosV6SessionAndFilenameValidater;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Test chaos V6 authorization validator */
public class ChaosV6SessionAndFilenameValidaterTest {

    @Test
    public void testValidateRightsToPlayFileSuccess() throws MCMOutputException, IOException {
        WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
        ChaosV6API chaosV6API = mock(ChaosV6API.class);
        when(chaosV6API.larmValidateSession(anyString(), anyString(), anyString())).thenReturn(Thread.currentThread().getContextClassLoader().getResourceAsStream("test_data_chaosv6_output_full.xml"));

        ChaosV6SessionAndFilenameValidater validater = new ChaosV6SessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer",
                                                                                              chaosV6API);
        boolean doesValidate = validater.validateRightsToPlayFile("a", "b", "c");
        
        assertEquals(true, doesValidate, "Filename validation:");
    }

    @Test
    public void testValidateRightsToPlayFileFailure() throws MCMOutputException, IOException {
        WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
        ChaosV6API chaosV6API = mock(ChaosV6API.class);
        when(chaosV6API.larmValidateSession(anyString(), anyString(), anyString())).thenReturn(Thread.currentThread().getContextClassLoader().getResourceAsStream("test_data_chaosv6_output_invalid_session.xml"));

        ChaosV6SessionAndFilenameValidater validater = new ChaosV6SessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer",
                                                                                              chaosV6API);
        boolean doesValidate = validater.validateRightsToPlayFile("a", "b", "c");

        assertEquals(false, doesValidate, "Filename validation:");
    }

    @Test
    public void testValidateRightsToPlayFileError() throws MCMOutputException, IOException {
        WMSLogger wmsLogger = WMSLoggerFactory.getLogger(this.getClass());
        ChaosV6API chaosV6API = mock(ChaosV6API.class);
        when(chaosV6API.larmValidateSession(anyString(), anyString(), anyString())).thenReturn(Thread.currentThread().getContextClassLoader().getResourceAsStream("test_data_invalid_output.xml"));

        ChaosV6SessionAndFilenameValidater validater = new ChaosV6SessionAndFilenameValidater(wmsLogger, "connectionURLString", "validationMethodAtServer",
                                                                                              chaosV6API);
        try {
            validater.validateRightsToPlayFile("a", "b", "c");
        } catch (MCMOutputException e) {
            //expected
            return;
        }
        fail("Should throw exception");
    }
}
