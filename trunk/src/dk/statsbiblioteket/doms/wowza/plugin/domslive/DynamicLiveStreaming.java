package dk.statsbiblioteket.doms.wowza.plugin.domslive;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import com.wowza.wms.stream.IMediaStreamNotify;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Nov 26, 2010
 * Time: 12:00:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class DynamicLiveStreaming implements IMediaStreamNotify{


    Random random = new Random();
    Map<String, Liver> runningstuff = new HashMap<String, Liver>();
    private IApplicationInstance appInstance
            ;
    private IMediaStreamFileMapper domsUriToFileMapper;

    public DynamicLiveStreaming(IApplicationInstance appInstance) {
        this.appInstance = appInstance;

    }

    public DynamicLiveStreaming(IApplicationInstance appInstance,
                                IMediaStreamFileMapper domsUriToFileMapper) {

        this.appInstance = appInstance;
        this.domsUriToFileMapper = domsUriToFileMapper;
    }

    @Override
    public void onMediaStreamCreate(IMediaStream iMediaStream) {
        getLogger().info("***Entered onMediaStreamCreate()");

        getLogger().info("onStreamCreate (name)     : " + iMediaStream.getName());
        getLogger().info("onStreamCreate (ext)  : " + iMediaStream.getExt());
        getLogger().info("onStreamCreate (cachename)     : " + iMediaStream.getCacheName());
        getLogger().info("onStreamCreate (contextstr)     : " + iMediaStream.getContextStr());
        getLogger().info("onStreamCreate (querystr)     : " + iMediaStream.getQueryStr());
        getLogger().info("onStreamCreate (streamtype)     : " + iMediaStream.getStreamType());

        iMediaStream.addClientListener(new StreamListener());

        try {
            File datafile = domsUriToFileMapper.streamToFileForRead(iMediaStream);
            if (datafile == null){//not one of ours
                return;
            }
            getLogger().info("iMediaStream datafile:"+datafile.getAbsolutePath());

            //first, we could decide on a random port for this this.
            int port = getRandomPort(iMediaStream);

            File streamfile
                    = new File(appInstance.getStreamStorageDir(),
                               port + ".stream");
            //create wowza streaming file
            if (streamfile.createNewFile()){
                //1. create stream file
                getLogger().info("created new File: "+streamfile.getAbsolutePath());
                Writer writer = new FileWriter(streamfile);
                writer.append("udp://0.0.0.0:"+port+"\n");
                writer.close();

                //2. start streaming app
                ProcessRunner process = startStreamingApp(datafile,port);

                //3. start wowza collection on that stream
                boolean success = appInstance.startMediaCasterStream(
                        streamfile.getName(),
                        iMediaStream.getExt(),
                        "rtp");
                iMediaStream.setName(streamfile.getName());
                if (success){
                    runningstuff.put(streamfile.getName(),new Liver(process,streamfile));
                } else {
                    process.stop();
                    getLogger().warn("vlc std error output: " +process.getProcessErrorAsString());
                    getLogger().warn("vlc std out output: " +process.getProcessOutputAsString());
                    streamfile.delete();
                }
            }
        } catch (IOException e) {
            getLogger().error("Caught exception e"+e.getMessage(),iMediaStream);
        }
    }

    @Override
    public void onMediaStreamDestroy(IMediaStream iMediaStream) {
        getLogger().info("***Entered onMediaStreamDestroy()");

        getLogger().info("onStreamDestroy (name)     : " + iMediaStream.getName());
        getLogger().info("onStreamDestroy (ext)  : " + iMediaStream.getExt());
        getLogger().info("onStreamDestroy (cachename)     : " + iMediaStream.getCacheName());
        getLogger().info("onStreamDestroy (contextstr)     : " + iMediaStream.getContextStr());
        getLogger().info("onStreamDestroy (querystr)     : " + iMediaStream.getQueryStr());
        getLogger().info("onStreamDestroy (streamtype)     : " + iMediaStream.getStreamType());

        String streamName = iMediaStream.getName();
        Liver liver = runningstuff.get(streamName);
        if (liver != null){
            //1. Stop mediacaster
            appInstance.stopMediaCasterStream(streamName);
            //2. stop vlc
            ProcessRunner process = liver.getProcess();
            process.stop();
            getLogger().warn("vlc std error output: " +process.getProcessErrorAsString());
            getLogger().warn("vlc std out output: " +process.getProcessOutputAsString());


            //3. remove file
            liver.getStreamfile().delete();
        }


    }


    private ProcessRunner startStreamingApp(File datafile, int port)
            throws IOException {

        List<String> commandList = new ArrayList<String>();
        commandList.add("cvlc");
        commandList.add("$DATAFILE");
        commandList.add("--sout");
        commandList.add("#transcode{venc=x264{keyint=60,profile=baseline,level=3.0,nocabac},vcodec=x264,vb=150,scale=0.5,acodec=mp4a,ab=96,channels=2,samplerate=48000}:rtp{dst=127.0.0.1,port=$PORT,mux=ts}");
        for (int i = 0; i < commandList.size(); i++) {
            String s;
            s =commandList.get(i);
            s = s.replace("$DATAFILE",datafile.getAbsolutePath());
            s = s.replace("$PORT",port+"");
            commandList.remove(i);
            commandList.add(i,s);
            getLogger().info("command element: "+s);
        }
        ProcessRunner runner = new ProcessRunner(commandList);
        Thread thread = new Thread(runner);
        thread.start();
        return runner;
    }

    private int getRandomPort(IMediaStream iMediaStream) {
        String queryString = iMediaStream.getClient().getQueryStr();
        int index = queryString.indexOf("port=");
        String port = queryString.substring(index+"port=".length());
        return Integer.parseInt(port);
/*
        int tryport;
        while (true){
            tryport = (random.nextInt() % 10000) + 34000;
            if (!runningstuff.containsKey(tryport+"")){
                break;
            }
        }
        return tryport;
*/
    }


    private class Liver {

        private ProcessRunner process;

        private File streamfile;

        private Liver(ProcessRunner process, File streamfile) {
            this.process = process;
            this.streamfile = streamfile;
        }

        public ProcessRunner getProcess() {
            return process;
        }

        public File getStreamfile() {
            return streamfile;
        }
    }

    protected static WMSLogger getLogger()
    {
        return WMSLoggerFactory.getLogger(DynamicLiveStreaming.class);
    }

}
