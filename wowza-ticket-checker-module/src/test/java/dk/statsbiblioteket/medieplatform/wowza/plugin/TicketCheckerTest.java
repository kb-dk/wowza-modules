package dk.statsbiblioteket.medieplatform.wowza.plugin;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.invocation.InvocationOnMock;

import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.ticket.TicketToolInterface;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;


public class TicketCheckerTest {

    public static final String QUERY_STRING = "ticket=";
    private Logger logger;

    TicketToolInterface ticketTool;

    IApplicationInstance iAppInstance = mock(IApplicationInstance.class);

    String goodIP = "127.0.0.1";
    String badIP = "127.0.0.2-Invalid-ip";
    String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";

    String name = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c.flv";


    public TicketCheckerTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
    }

    @BeforeEach
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        ticketTool = mock(TicketToolInterface.class); 
    }

    @AfterEach
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testUserNotAllowedToPlayFile() {
        // Setup environment
        TestTicketStore tss = new TestTicketStore();
        when(ticketTool.resolveTicket(anyString())).thenAnswer(
                (InvocationOnMock invocation) -> tss.resolveTicket((String) invocation.getArguments()[0]));
        
        Ticket ticket = tss.issueTicket(badIP, programID, new ArrayList<Property>());
        
        String queryString = QUERY_STRING + ticket.getId();

        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        IMediaStream stream = mock(IMediaStream.class);
        when(stream.getClient()).thenReturn(iClient);
        when(stream.getQueryStr()).thenReturn(queryString);
        when(stream.getName()).thenReturn(name);
        TicketChecker ticketChecker = new TicketChecker("Stream", ticketTool);
        // Run test
        boolean result = ticketChecker.checkTicket(stream, stream.getClient());
        // Validate result
        assertFalse(result, "Expected not to be allowed");
    }

    @Test
    public void testNonExistingTicket() {
        // Setup environment
        String queryString = QUERY_STRING + "InvalidID";

        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        IMediaStream stream = mock(IMediaStream.class);
        when(stream.getClient()).thenReturn(iClient);
        when(stream.getQueryStr()).thenReturn(queryString);
        when(stream.getName()).thenReturn(name);
        TicketChecker ticketChecker = new TicketChecker("Stream", ticketTool);
        // Run test
        boolean result = ticketChecker.checkTicket(stream, stream.getClient());
        // Validate result
        assertFalse(result, "Expected not to be allowed");
    }

    @Test
    public void testGetFileToStreamSucces() {
        // Setup
        TestTicketStore tss = new TestTicketStore();
        when(ticketTool.resolveTicket(anyString())).thenAnswer(
                (InvocationOnMock invocation) -> tss.resolveTicket((String) invocation.getArguments()[0]));
        
        Ticket ticket = tss.issueTicket(goodIP, programID, new ArrayList<Property>());
        String queryString = QUERY_STRING + ticket.getId();

        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        when(iClient.getIp()).thenReturn(goodIP);
        IMediaStream stream = mock(IMediaStream.class);
        when(stream.getClient()).thenReturn(iClient);
        when(stream.getQueryStr()).thenReturn(queryString);
        when(stream.getName()).thenReturn(name);
        TicketChecker ticketChecker = new TicketChecker("Stream", ticketTool);
        // Test
        boolean result = ticketChecker.checkTicket(stream, stream.getClient());
        // Validate
        assertTrue(result, "Expected success");
    }

    @Test
    public void testWrongProgramId() {
        // Setup
        TestTicketStore tss = new TestTicketStore();
        when(ticketTool.resolveTicket(anyString())).thenAnswer(
                (InvocationOnMock invocation) -> tss.resolveTicket((String) invocation.getArguments()[0]));
        
        Ticket ticket = tss.issueTicket(goodIP, "anotherprogram", new ArrayList<Property>());
        String queryString = QUERY_STRING + ticket.getId();

        IClient iClient = mock(IClient.class);
        when(iClient.getQueryStr()).thenReturn(queryString);
        IMediaStream stream = mock(IMediaStream.class);
        when(stream.getClient()).thenReturn(iClient);
        when(stream.getQueryStr()).thenReturn(queryString);
        when(stream.getName()).thenReturn(name);
        TicketChecker ticketChecker = new TicketChecker("Stream", ticketTool);
        // Test
        boolean result = ticketChecker.checkTicket(stream, stream.getClient());
        // Validate
        assertFalse(result, "Expected not to be allowed");
    }
}
