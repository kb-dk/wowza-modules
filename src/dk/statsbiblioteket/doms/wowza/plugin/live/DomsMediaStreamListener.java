package dk.statsbiblioteket.doms.wowza.plugin.live;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;
import com.wowza.wms.stream.IMediaStreamNotify;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.ConfigReader;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.ProcessRunner;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.Utils;

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
public class DomsMediaStreamListener implements IMediaStreamNotify{


    Random random = new Random();
    Map<String, Liver> runningstuff = new HashMap<String, Liver>();
    private IApplicationInstance appInstance;
    private IMediaStreamFileMapper domsUriToFileMapper;
    private ConfigReader configReader;

    public DomsMediaStreamListener(IApplicationInstance appInstance) {
        this.appInstance = appInstance;

    }

    public DomsMediaStreamListener(IApplicationInstance appInstance,
                                IMediaStreamFileMapper domsUriToFileMapper,
                                ConfigReader configReader) {

        this.appInstance = appInstance;
        this.domsUriToFileMapper = domsUriToFileMapper;
        this.configReader = configReader;
    }

    @Override
    public void onMediaStreamCreate(IMediaStream iMediaStream) {
        getLogger().info("***Entered onMediaStreamCreate()");


        try {
            //Check the ticket and decode the file
            File datafile = domsUriToFileMapper.streamToFileForRead(iMediaStream);
            if (datafile == null){//not one of ours
                getLogger().info("This mediaStream is not one of ours, returning",iMediaStream);
                return;
            }

            //The Datafile is now the correct file or the error file. Otherwise an exception would have been thrown

/*
            getLogger().info("onStreamCreate (name)     : " + iMediaStream.getName());
            getLogger().info("onStreamCreate (ext)  : " + iMediaStream.getExt());
            getLogger().info("onStreamCreate (cachename)     : " + iMediaStream.getCacheName());
            getLogger().info("onStreamCreate (contextstr)     : " + iMediaStream.getContextStr());
            getLogger().info("onStreamCreate (querystr)     : " + iMediaStream.getQueryStr());
            getLogger().info("onStreamCreate (streamtype)     : " + iMediaStream.getStreamType());
*/

            //Add the stream listener, that will plug the next security hole
            iMediaStream.addClientListener(new DomsMediaStreamActionListener());
            

            getLogger().info("iMediaStream datafile:"+datafile.getAbsolutePath());


            //first, we find a port to use for this streaming
            int port = getRandomPort(iMediaStream);

            //Make the new file so that wowza can receive the streaming
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

                try {
                    Thread.sleep(Integer.parseInt(configReader.get("SleepyTime","3000")));
                } catch (InterruptedException e) {

                }
                
                //iMediaStream.setName(streamfile.getName());
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
            runningstuff.remove(streamName);
        }


    }


    private ProcessRunner startStreamingApp(File datafile, int port)
            throws IOException {

        getLogger().info("Entered startStreamingApp");
        List<String> commandList = new ArrayList<String>();
        int commandNumber=0;
        while (true){
            String command = configReader.get("process" + commandNumber++);
            if (command == null){
                break;
            } else {
                command = command.replace("$DATAFILE",datafile.getAbsolutePath());
                command = command.replace("$PORT",port+"");
                commandList.add(command);
                getLogger().info("process: '"+command+"'");
            }
        }

        ProcessRunner runner = new ProcessRunner(commandList);
        Thread thread = new Thread(runner);
        thread.start();
        getLogger().info("Streaming process started");
        return runner;
    }

    private int getRandomPort(IMediaStream iMediaStream) {
        String queryString = iMediaStream.getClient().getQueryStr();
        String port = Utils.extractPortID(queryString);
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
        return WMSLoggerFactory.getLogger(DomsMediaStreamListener.class);
    }


}
