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

//TODO javadoc
public class ConfigReader {

    private Properties properties;

    // TODO javadoc
    /**
     *
     * @param path
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

    // TODO javadoc
    /**
     *
     * @param key
     * @return object of key
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

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
