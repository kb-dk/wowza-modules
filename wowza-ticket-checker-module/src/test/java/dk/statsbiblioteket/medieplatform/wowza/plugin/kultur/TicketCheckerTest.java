package dk.statsbiblioteket.medieplatform.wowza.plugin.kultur;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import dk.statsbiblioteket.medieplatform.ticketsystem.Property;
import dk.statsbiblioteket.medieplatform.ticketsystem.Ticket;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IApplicationInstanceMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IClientMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.IMediaStreamMock;
import dk.statsbiblioteket.medieplatform.wowza.plugin.mockobjects.TicketToolMock;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TicketCheckerTest {

    public static final String RTMP_HYPOTHETICAL_TEST_MACHINE_1935_TICKET = "rtmp://hypothetical-test-machine:1935/tickets?ticket=";
    private Logger logger;

    TicketToolMock ticketToolMock;

    IApplicationInstance iAppInstance = new IApplicationInstanceMock();



    String goodIP = "127.0.0.1";
    String badIP = "127.0.0.2-Invalid-ip";
    String programID = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c";

    String name = "0ef8f946-4e90-4c9d-843a-a03504d2ee6c.flv";


    public TicketCheckerTest() {
        super();
        this.logger = WMSLoggerFactory.getLogger(this.getClass());
    }

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        ticketToolMock = new TicketToolMock();
    }

    @After
    public void tearDown() throws Exception {
        org.apache.log4j.BasicConfigurator.resetConfiguration();
    }

    @Test
    public void testUserNotAllowedToPlayFile() {
        // Setup environment
        Ticket ticket = ticketToolMock.issueTicket(badIP, programID, new ArrayList<Property>());
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_TICKET + ticket.getId();

        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        TicketChecker ticketChecker = new TicketChecker("Stream", ticketToolMock);
        // Run test
        boolean result = ticketChecker.checkTicket(stream);
        // Validate result
        assertFalse("Expected not to be allowed", result);
    }

    @Test
    public void testNonExistingTicket() {
        // Setup environment
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_TICKET + "InvalidID";

        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        TicketChecker ticketChecker = new TicketChecker("Stream", ticketToolMock);
        // Run test
        boolean result = ticketChecker.checkTicket(stream);
        // Validate result
        assertFalse("Expected not to be allowed", result);
    }

    @Test
    public void testGetFileToStreamSucces() {
        // Setup
        Ticket ticket = ticketToolMock.issueTicket(goodIP, programID, new ArrayList<Property>());
        String queryString = RTMP_HYPOTHETICAL_TEST_MACHINE_1935_TICKET + ticket.getId();

        IClient iClient = new IClientMock(iAppInstance, logger, queryString);
        IMediaStream stream = new IMediaStreamMock(logger, name, iClient);
        TicketChecker ticketChecker = new TicketChecker("Stream", ticketToolMock);
        // Test
        boolean result = ticketChecker.checkTicket(stream);
        // Validate
        assertTrue("Expected success", result);
    }

}
