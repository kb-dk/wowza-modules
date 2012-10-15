package dk.statsbiblioteket.doms.wowza.plugin.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** TODO javadoc */
public class ConfigReader {

    private Properties properties;

    /**
     * TODO javadoc
     *
     * @param path
     */
    public ConfigReader(String path) throws IOException {

        properties = new Properties();

        InputStream props = this.getClass().getClassLoader().getResourceAsStream(path);
        if (props == null) {
            throw new FileNotFoundException("property file '" + path + "' not found in the classpath");
        }
        properties.load(props);
    }

    public ConfigReader(File file) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(file));
    }

    /**
     * TODO javadoc
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
}
