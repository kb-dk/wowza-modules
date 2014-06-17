package dk.statsbiblioteket.medieplatform.wowza.plugin.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Read configuration.
 * This reads configuration file from a given path.
 */
public class ConfigReader {

    private Properties properties;

    /**
     * Read configuration from given path.
     * Fails if list of required properties are not defined.
     * @param path The path to read properties from.
     * @param requiredProperties If given properties are not found, throws exception.
     *
     * @throws RuntimeException if properties are not set.
     * @throws IOException on trouble reading property from path.
     */
    public ConfigReader(String path, String... requiredProperties) throws IOException {

        properties = new Properties();

        InputStream props = this.getClass().getClassLoader().getResourceAsStream(path);
        if (props == null) {
            throw new FileNotFoundException("property file '" + path + "' not found in the classpath");
        }
        properties.load(props);
        checkProperties(requiredProperties);
    }

    public ConfigReader(File file, String... requiredProperties) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(file));
        checkProperties(requiredProperties);
    }

    /**
     * Read property.
     * @param key The property key.
     * @return The property value.
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Read property.
     * @param key The property key.
     * @return The property value, or defaultValue if key is not set.
     */
    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Throw exception if given property keys are not set.
     * @param propertyKeys Properties that must be set.
     *
     * @throws RuntimeException if properties are not set.
     */
    private void checkProperties(String[] propertyKeys) {
        Set<String> keysLeft = new HashSet<String>();
        keysLeft.addAll(Arrays.asList(propertyKeys));
        for (String key : propertyKeys) {
            if (properties.getProperty(key) != null) {
                keysLeft.remove(key);
            }
        }
        if (!keysLeft.isEmpty()) {
            String message = "Missing keys:";
            for (String key:keysLeft) {
                message += " " + key;
            }
            throw new RuntimeException("Missing properties.\n" + message);
        }
    }
}
