package dk.statsbiblioteket.medieplatform.wowza.plugin;

import java.io.File;
import java.io.IOException;
import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import com.wowza.wms.module.IModuleOnApp;
import com.wowza.wms.module.IModuleOnConnect;
import com.wowza.wms.module.IModuleOnHTTPSession;
import com.wowza.wms.module.IModuleOnStream;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import com.wowza.wms.stream.IMediaStreamNotify;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.CombiningContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.ContentResolver;
import dk.statsbiblioteket.medieplatform.contentresolver.lib.DirectoryBasedContentResolver;
import dk.statsbiblioteket.medieplatform.wowza.plugin.utilities.ConfigReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This module sets up the file mapper that is needed for identifying the file
 * to be played.
 *
 * @author heb + jrg + abr + kfc
 */
public class ContentResolverModule extends ModuleBase
        implements IModuleOnApp, IModuleOnConnect, IModuleOnStream, IMediaStreamNotify, IModuleOnHTTPSession {

    private static final String PLUGIN_NAME = "Wowza Content Resolver Plugin";
    private static final String PLUGIN_VERSION = ContentResolverModule.class.getPackage().getImplementationVersion();

    public ContentResolverModule() {
        super();
    }

    /**
     * Called when Wowza is started. We use this to set up the
     * ContentResolverMapper.
     *
     * @param appInstance The application running.
     */
    @Override
    public void onAppStart(IApplicationInstance appInstance) {
        String appName = appInstance.getApplication().getName();
        String vhostDir = appInstance.getVHost().getHomePath();
        String storageDir = appInstance.getStreamStorageDir();
        getLogger().info("***Entered onAppStart: " + appName
                + "\n  Plugin: " + PLUGIN_NAME + " version " + PLUGIN_VERSION
                + "\n  VHost home path: " + vhostDir + " VHost storage dir: " + storageDir);
        try {
            // Setup file mapper
            IMediaStreamFileMapper defaultMapper = appInstance.getStreamFileMapper();

            //Initialise the config reader
            ConfigReader cr;
            cr = new ConfigReader(new File(vhostDir + "/conf/" + appName + "/wowza-modules.properties"));

            //Read to initialise the content resolver for doms
            String presentationType = cr.get("presentationType", "Stream");

            ContentResolver contentResolver = getContentResolver(cr, storageDir);

            ContentResolverMapper contentResolverMapper = new ContentResolverMapper(presentationType, defaultMapper, contentResolver);
            // Set File mapper
            appInstance.setStreamFileMapper(contentResolverMapper);
        } catch (IOException e) {
            getLogger().error("An IO error occured.", e);
            throw new RuntimeException("An IO error occured.", e);
        }
    }

    /**
     * <p>
     * Configure and instantiate a ContentResolver</p>
     *
     * @param cr Config Reader with wowza-modules.properties loaded
     * @param topdir The storagedir for this appInstance
     * @return ContentResolver
     */
    protected ContentResolver getContentResolver(ConfigReader cr, String topdir) {
        String[] contentresolvernames = {""};
        String cs = cr.get("contentResolverNames");
        if (cs != null && !cs.isEmpty()) {
            //this wowza instance is configured for more content providers
            contentresolvernames = cs.split(",");
            List<ContentResolver> contentResolvers = Arrays.stream(contentresolvernames)
                    .map(s -> {
                        return getContentResolver(cr,topdir, s + ".");
                    })
                    .collect(Collectors.toList());

            ContentResolver combinedResolver = new CombiningContentResolver(contentResolvers);
            return combinedResolver;
        } else {
            //This wowza instance assumes there is only one content provider
            return getContentResolver(cr, topdir, "");
        }

    }

    /**
     * <p>
     * Configure and instantiate a ContentResolver</p>
     *
     * @param contentProviderName If the configuration properties file is set up
     * with more than one contentprovider, this is the prefix for the desired
     * configuration
     * @param cr Config Reader with wowza-modules.properties loaded
     * @param topdir The storagedir for this appInstance
     * @return ContentResolver
     */
    protected ContentResolver getContentResolver(ConfigReader cr, String topdir, String contentProviderName) {
        File baseDirectory = new File(topdir + File.separator + cr.get(contentProviderName + "subdirectory","")).getAbsoluteFile();
        int characterDirs = Integer.parseInt(cr.get(contentProviderName + "characterDirs", "4"));
        int characterDirsWidth = Integer.parseInt(cr.get(contentProviderName + "characterDirsWidth", "1"));
        String filenameRegexPattern = cr
                .get(contentProviderName + "filenameRegexPattern", "missing-filename-regex-pattern-in-property-file");
        String uriPattern = "file://" + baseDirectory + "/%s";
        String presentationType = cr.get("presentationType", "Stream");

        return new DirectoryBasedContentResolver(presentationType, baseDirectory,
                characterDirs, characterDirsWidth, filenameRegexPattern,
                uriPattern);

    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
    }


    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onStreamCreate(IMediaStream stream) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onStreamDestroy(IMediaStream stream) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onAppStop(IApplicationInstance appInstance) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnectAccept(IClient client) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onConnectReject(IClient client) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onDisconnect(IClient client) {
        // Do nothing.
    }


    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onMediaStreamCreate(IMediaStream stream) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onMediaStreamDestroy(IMediaStream stream) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onHTTPSessionCreate(IHTTPStreamerSession httpSession) {
        // Do nothing.
    }

    /*Mainly here to remember that we can hook this method*/
    @Override
    public void onHTTPSessionDestroy(IHTTPStreamerSession ihttpStreamerSession) {
        // Do nothing.
    }
}
