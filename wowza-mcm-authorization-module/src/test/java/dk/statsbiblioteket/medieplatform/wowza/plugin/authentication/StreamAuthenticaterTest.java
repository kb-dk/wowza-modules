package dk.statsbiblioteket.medieplatform.wowza.plugin.authentication;

import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.*;

import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.SessionAndFilenameValidaterMock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test authorization based on sessionid, objectid and filename.
 */
public class StreamAuthenticaterTest {

    private WMSLogger logger;
    private StreamAuthenticater streamAuthenticater;
    
    private String validSessionID;
    private String invalidSessionID;
    private String validObjectID;
    private String invalidObjectID;
    private String validFilename;
    private String invalidFilename;

    public StreamAuthenticaterTest() throws FileNotFoundException, IOException {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
        this.streamAuthenticater = new StreamAuthenticater(logger, new SessionAndFilenameValidaterMock());
        this.validSessionID = "5F95E509-FD84-4570-9382-FEC5481E342F";
        this.invalidSessionID = "E32262ED-21A8-46CB-BDA3-FF8D284DAC48";
        this.validObjectID = "976";
        this.invalidObjectID = "977";
        this.validFilename = new File("P3_2000_2200_890325_001.mp3").getAbsolutePath();
        this.invalidFilename = new File("P3_2000_2200_890325_001_invalid.mp3").getAbsolutePath();
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
    public void testCheckAuthorizationEverythingOK() {
        
        boolean result = streamAuthenticater.checkAuthorization(validSessionID, validObjectID, validFilename);
        assertEquals("Input expected to be valid.", true, result);
    }

    @Test
    public void testCheckAuthorizationInvalidSessionID() {
        boolean result = streamAuthenticater.checkAuthorization(invalidSessionID, validObjectID, validFilename);
        assertEquals("Input is not expected to be valid.", false, result);
    }

    @Test
    public void testCheckAuthorizationInvalidObjectID() {
        boolean result = streamAuthenticater.checkAuthorization(validSessionID, invalidObjectID, validFilename);
        assertEquals("Input is not expected to be valid.", false, result);
    }

    @Test
    public void testCheckAuthorizationInvalidFilename() {
        boolean result = streamAuthenticater.checkAuthorization(validSessionID, validObjectID, invalidFilename);
        assertEquals("Input is not expected to be valid.", false, result);
    }

    @Test
    public void testOnPlayValidInput() {
        String queryString = "ObjectID=" + validObjectID + "&SessionID=" + validSessionID;
        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);

        IMediaStream iMediaStream = mock(IMediaStream.class);
        when(iMediaStream.getClient()).thenReturn(iClient);
        when(iMediaStream.getQueryStr()).thenReturn(queryString);
        when(iMediaStream.getName()).thenReturn(validFilename);

        streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
        verify(iClient, never()).setShutdownClient(anyBoolean());
    }

    @Test
    public void testOnPlayWrongSession() {
        String queryString = "ObjectID=" + validObjectID + "&SessionID=" + invalidSessionID;
        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        
        IMediaStream iMediaStream = mock(IMediaStream.class);
        when(iMediaStream.getClient()).thenReturn(iClient);
        when(iMediaStream.getQueryStr()).thenReturn(queryString);
        when(iMediaStream.getName()).thenReturn(validFilename);

        streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
        verify(iClient, times(1)).setShutdownClient(anyBoolean());
    }

    @Test
    public void testOnPlayWrongObject() {
        String queryString = "ObjectID=" + invalidObjectID + "&SessionID=" + validSessionID;
        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        
        IMediaStream iMediaStream = mock(IMediaStream.class);
        when(iMediaStream.getClient()).thenReturn(iClient);
        when(iMediaStream.getQueryStr()).thenReturn(queryString);
        when(iMediaStream.getName()).thenReturn(validFilename);
        
        streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
        verify(iClient, times(1)).setShutdownClient(anyBoolean());
    }

    @Test
    public void testOnPlayWrongFilename() {
        String queryString = "ObjectID=" + invalidObjectID + "&SessionID=" + validSessionID;
        IClient iClient = mock(IClient.class);
        IMediaStream iMediaStream = mock(IMediaStream.class);
        when(iMediaStream.getClient()).thenReturn(iClient);
        when(iMediaStream.getQueryStr()).thenReturn(queryString);
        when(iMediaStream.getName()).thenReturn(invalidFilename);
        streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
        verify(iClient, times(1)).setShutdownClient(anyBoolean());
    }

    @Test
    public void testOnPlayMalformedURLException() {
        String queryString = "ObjectID=" + "MalformedURLExceptionTrigger" + "&SessionID=" + validSessionID;
        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        
        IMediaStream iMediaStream = mock(IMediaStream.class);
        when(iMediaStream.getClient()).thenReturn(iClient);
        when(iMediaStream.getQueryStr()).thenReturn(queryString);
        when(iMediaStream.getName()).thenReturn(validFilename);
 
        streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
        verify(iClient, times(1)).setShutdownClient(anyBoolean());
    }

    @Test
    public void testOnPlayIOException() {
        String queryString = "ObjectID=" + "IOExceptionTrigger" + "&SessionID=" + validSessionID;
        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        
        IMediaStream iMediaStream = mock(IMediaStream.class);
        when(iMediaStream.getClient()).thenReturn(iClient);
        when(iMediaStream.getQueryStr()).thenReturn(queryString);
        when(iMediaStream.getName()).thenReturn(validFilename);
        
        streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
        verify(iClient, times(1)).setShutdownClient(anyBoolean());
    }

    @Test
    public void testOnPlayMCMOutputException() {
        String queryString = "ObjectID=" + "MCMOutputExceptionTrigger" + "&SessionID=" + validSessionID;
        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        
        IMediaStream iMediaStream = mock(IMediaStream.class);
        when(iMediaStream.getClient()).thenReturn(iClient);
        when(iMediaStream.getQueryStr()).thenReturn(queryString);
        when(iMediaStream.getName()).thenReturn(validFilename);

        streamAuthenticater.onPlay(iMediaStream, null, 0, 0, 0);
        verify(iClient, times(1)).setShutdownClient(anyBoolean());
    }
}
