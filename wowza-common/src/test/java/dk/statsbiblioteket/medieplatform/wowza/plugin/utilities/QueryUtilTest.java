package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryUtilTest extends TestCase {

    private String param1 = "param1Key=param1Value";
    private String param2 = "param1Key=param2Value";
    private String param3 = "param1Key=param3Value";

    public QueryUtilTest() {
        super();
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
    public void testTicketFirstInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = ticketParam + "&" + param1 + "&" + param2 + "&" + param3;
        String ticketIDExtract = QueryUtil.extractTicketID(queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testTicketSecondInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = param1 + "&" + ticketParam + "&" + param2 + "&" + param3;
        String ticketIDExtract = QueryUtil.extractTicketID(queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testTicketLastInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = param1 + "&" + param2 + "&" + param3 + "&" + ticketParam;
        String ticketIDExtract = QueryUtil.extractTicketID(queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testTicketOnlyInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = ticketParam;
        String ticketIDExtract = QueryUtil.extractTicketID(queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testNoTicketInQuery() {
        // Setup environment
        String queryString = param1 + "&" + param2 + "&" + param3;
        try {
            QueryUtil.extractTicketID(queryString);
            fail("This statement should not be reached!");
        } catch (IllegallyFormattedQueryStringException e) {
            // Expected behavior
        }
    }
}
