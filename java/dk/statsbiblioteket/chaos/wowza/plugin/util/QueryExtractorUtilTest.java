package dk.statsbiblioteket.chaos.wowza.plugin.util;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

public class QueryExtractorUtilTest {

	private WMSLogger logger;

	private String validSessionID;
	private String validObjectID;

	public QueryExtractorUtilTest() throws FileNotFoundException, IOException {
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
		String value = StringAndTextUtil.extractValueFromQueryStringAndKey(key, queryString);
		assertEquals("Unexpected return value.", null, value);
	}

	@Test
	public void testExtractValueFromQueryStringAndKeyObjectID() {
		String queryString = "ObjectID=" + validObjectID + "&SessionID=" + validSessionID;
		String key = "ObjectID";
		String value = StringAndTextUtil.extractValueFromQueryStringAndKey(key, queryString);
		assertEquals("Unexpected return value.", validObjectID, value);
	}

	@Test
	public void testExtractValueFromQueryStringAndKeySessionID() {
		String queryString = "ObjectID=" + validObjectID + "&SessionID=" + validSessionID;
		String key = "SessionID";
		String value = StringAndTextUtil.extractValueFromQueryStringAndKey(key, queryString);
		assertEquals("Unexpected return value.", validSessionID, value);
	}

}
