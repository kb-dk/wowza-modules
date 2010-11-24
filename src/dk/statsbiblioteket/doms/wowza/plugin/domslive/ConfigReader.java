package dk.statsbiblioteket.doms.wowza.plugin.domslive;




import java.util.Properties;
import java.io.*;

/**
 * TODO javadoc
 */
public class ConfigReader {


    private Properties properties;

    /**
     * TODO javadoc
     * @param path
     */
    public ConfigReader(String path) {

        properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
        } catch (IOException e) { System.err.println(e); }

    }

    /**
     * TODO javadoc
     * @param key
     * @return object of key
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key,defaultValue);
    }
}