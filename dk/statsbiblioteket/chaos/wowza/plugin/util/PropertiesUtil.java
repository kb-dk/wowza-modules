package dk.statsbiblioteket.chaos.wowza.plugin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.wowza.wms.logging.WMSLogger;

public class PropertiesUtil {

	private static final String propertyFilePath = "conf/chaos/chaos-streaming-server-plugin.properties";
    private static boolean propertiesRead = false;
    private static Properties properties;

    public static synchronized void loadProperties(WMSLogger logger, String vHostRootDir, String[] requiredProperties) {
		if (!propertiesRead) {
			readPropertiesFromFile(logger, vHostRootDir);
			propertiesRead = true;
		}
        checkProperties(logger, requiredProperties);
	}

	public static String getProperty(String key) {
		if (!propertiesRead) {
			throw new RuntimeException("Properties not read. Load properties before getting them.");
		}
		String value = properties.getProperty(key);
		if (value == null) {
			throw new RuntimeException("Fetching unexpected property for key: " + key);
		}
		return value; 
	}

	/**
	 * Reads properties from property file.
	 *
	 * @throws FileNotFoundException if property file is not found
	 * @throws IOException if reading process failed
	 */
	private static void readPropertiesFromFile(WMSLogger logger, String vHostRootDir) {
		try {
            properties = new Properties();
			File propertyFile = new File(vHostRootDir + "/" + propertyFilePath);
			logger.info("Loading properties from file:" + propertyFile.getAbsoluteFile());
			properties.load(new FileInputStream(propertyFile));
		} catch (IOException e) {
			throw new RuntimeException("Could not read properties.", e);
		}
	}

    private static void checkProperties(WMSLogger logger, String[] propertyKeys) {
        logger.debug("1");
        Set<String> keysLeft = new HashSet<String>();
        keysLeft.addAll(Arrays.asList(propertyKeys));
        logger.debug("2");
        for (String key : propertyKeys) {
            if (properties.getProperty(key) != null) {
                keysLeft.remove(key);
            }
        }
        logger.debug("3");
        if (!keysLeft.isEmpty()) {
            String message = "Missing keys:";
            for (String key:keysLeft) {
                message += " " + key;
            }
            throw new RuntimeException("Missing properties.\n" + message);
        }
        logger.debug("4");
    }

}
