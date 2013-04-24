package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StringAndTextUtilTest {
    private String param1 = "param1Key=param1Value";
    private String param2 = "param1Key=param2Value";
    private String param3 = "param1Key=param3Value";

	private WMSLogger logger;

	private String validSessionID;
	private String validObjectID;

	public StringAndTextUtilTest() throws FileNotFoundException, IOException {
		super();
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
		this.validSessionID = "5F95E509-FD84-4570-9382-FEC5481E342F";
		this.validObjectID = "976";
		logger.info("Ready for test.");
	}

	@Test
	public void testExtractValueFromQueryStringAndKeyNonExistingKey() {
		String queryString = "ObjectID=" + validObjectID + "&SessionID=" + validSessionID;
		String key = "nonexistingKey";
        try {
            StringAndTextUtil.extractValueFromQueryStringAndKey(key, queryString);
            fail("This statement should not be reached");
        } catch (IllegallyFormattedQueryStringException e) {
            // Expected
        }
	}

	@Test
	public void testExtractValueFromQueryStringAndKeyObjectID() throws IllegallyFormattedQueryStringException {
		String queryString = "ObjectID=" + validObjectID + "&SessionID=" + validSessionID;
		String key = "ObjectID";
		String value = StringAndTextUtil.extractValueFromQueryStringAndKey(key, queryString);
		assertEquals("Unexpected return value.", validObjectID, value);
	}

	@Test
	public void testExtractValueFromQueryStringAndKeySessionID() throws IllegallyFormattedQueryStringException {
		String queryString = "ObjectID=" + validObjectID + "&SessionID=" + validSessionID;
		String key = "SessionID";
		String value = StringAndTextUtil.extractValueFromQueryStringAndKey(key, queryString);
		assertEquals("Unexpected return value.", validSessionID, value);
	}

    @Test
    public void testTicketFirstInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = ticketParam + "&" + param1 + "&" + param2 + "&" + param3;
        String ticketIDExtract = StringAndTextUtil.extractValueFromQueryStringAndKey("ticket", queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testTicketSecondInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = param1 + "&" + ticketParam + "&" + param2 + "&" + param3;
        String ticketIDExtract = StringAndTextUtil.extractValueFromQueryStringAndKey("ticket", queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testTicketLastInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = param1 + "&" + param2 + "&" + param3 + "&" + ticketParam;
        String ticketIDExtract = StringAndTextUtil.extractValueFromQueryStringAndKey("ticket", queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testTicketOnlyInQuery() throws IllegallyFormattedQueryStringException {
        // Setup environment
        String ticketIDOrig = "123456abcd";
        String ticketParam = "ticket=" + ticketIDOrig;
        String queryString = ticketParam;
        String ticketIDExtract = StringAndTextUtil.extractValueFromQueryStringAndKey("ticket", queryString);
        assertEquals(ticketIDOrig, ticketIDExtract);
    }

    @Test
    public void testNoTicketInQuery() {
        // Setup environment
        String queryString = param1 + "&" + param2 + "&" + param3;
        try {
            StringAndTextUtil.extractValueFromQueryStringAndKey("ticket", queryString);
            fail("This statement should not be reached!");
        } catch (IllegallyFormattedQueryStringException e) {
            // Expected behavior
        }
    }
}
