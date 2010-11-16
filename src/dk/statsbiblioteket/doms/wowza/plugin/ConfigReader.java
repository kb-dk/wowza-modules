package dk.statsbiblioteket.doms.wowza.plugin;

import java.util.HashMap;
import java.io.*;

/**
 * TODO javadoc
 */
public class ConfigReader {


    private HashMap<String, String> hashmap;

    /**
     * TODO javadoc
     * @param path
     */
    public ConfigReader(String path) {

        hashmap = new HashMap<String, String>();
        File file = new File(path);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedReader dis = null;
        HashMap<String,String> hashmap = new HashMap<String, String>();
        try {
            fis = new FileInputStream(file);

            bis = new BufferedInputStream(fis);
            dis = new BufferedReader(new InputStreamReader(bis));
            String s = "";
            while (dis.ready()) {

                s=dis.readLine().trim();
                String t =  s.substring(s.indexOf("<")+1,s.indexOf(">"));

                String u = s.substring(s.indexOf("<" + t + ">")+t.length()+2, s.indexOf("</"+t+">"));
                hashmap.put(t,u);



            }

            fis.close();
            bis.close();
            dis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * TODO javadoc
     * @param key
     * @return object of key
     */
    public String get(String key) {
        return hashmap.get(key);
    }
}
