package dk.statsbiblioteket.chaos.wowza.plugin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringTokenizer;

public class StringAndTextUtil {

	public static String extractValueFromQueryStringAndKey(String key,
			String queryString) {
		String foundValue = null;
		StringTokenizer queryItems = new StringTokenizer(queryString, "&");
		while (queryItems.hasMoreTokens() && foundValue == null) {
			String queryParameter = queryItems.nextToken();
			StringTokenizer paramTokenizer = new StringTokenizer(queryParameter, "=");
			String currentKey = paramTokenizer.nextToken();
			String currentValue = paramTokenizer.nextToken();
			if (currentKey.equalsIgnoreCase(key)) {
				foundValue = currentValue;
			}
		}
		return foundValue;
	}

	public static String convertStreamToString(InputStream is)
	            throws IOException {
			/*
			 * To convert the InputStream to String we use the
			 * Reader.read(char[] buffer) method. We iterate until the
			 * Reader return -1 which means there's no more data to
			 * read. We use the StringWriter class to produce the string.
			 */
	    if (is != null) {
	        Writer writer = new StringWriter();
	
	        char[] buffer = new char[1024];
	        try {
	            Reader reader = new BufferedReader(
	                    new InputStreamReader(is, "UTF-8"));
			    int n;
			    while ((n = reader.read(buffer)) != -1) {
			        writer.write(buffer, 0, n);
			    }
		    } finally {
		        is.close();
		    }
	    return writer.toString();
		} else {       
		    return "";
	    }
	}

}
