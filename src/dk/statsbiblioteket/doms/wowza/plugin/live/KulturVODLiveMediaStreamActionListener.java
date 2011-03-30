package dk.statsbiblioteket.doms.wowza.plugin.live;

import org.apache.log4j.Logger;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify2;

import dk.statsbiblioteket.doms.wowza.plugin.utilities.IllegallyFormattedQueryStringException;
import dk.statsbiblioteket.doms.wowza.plugin.utilities.QueryUtil;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Checksums;

/**
 * Created by IntelliJ IDEA.
 * User: abr + heb
 * Date: Nov 26, 2010
 * Time: 3:07:58 PM
 * To change this template use File | Settings | File Templates.
 */
class KulturVODLiveMediaStreamActionListener implements IMediaStreamActionNotify2 {

	private Logger logger;
	
	/**
	 * Used by Wowza Streaming Server
	 */
    public KulturVODLiveMediaStreamActionListener() {
		super();
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
	}
    
    /**
     * For test purpose.
     * @param logger
     */
	public KulturVODLiveMediaStreamActionListener(Logger logger) {
		super();
		this.logger = logger;
	}

	public void onPlay(IMediaStream stream, String streamName,
                       double playStart, double playLen, int playReset) {
    	
        logger.info("onPlay");
        IClient client = stream.getClient();

        if (client != null){

            String querystring = stream.getClient().getQueryStr();
            String expectedStreamName;
			try {
				expectedStreamName = Bytes.toHex(Checksums.md5(QueryUtil.extractTicketID(querystring)))+".stream";
			} catch (IllegallyFormattedQueryStringException e) {
				logger.warn("Unable to parse query string. Stopped with message: " + e.getMessage());
				expectedStreamName = null;
			}
            if ((expectedStreamName==null) || (!expectedStreamName.equals(streamName))){
                logger.warn("Shutting down stream "+streamName+", because it does not have the expected name "+expectedStreamName+".");
                stream.shutdown();
            }

        }
        logger.info("onStreamPlay (name)       : " + stream.getName());
        logger.info("onStreamPlay (name)       : " + streamName);
        logger.info("onStreamPlay (ext)        : " + stream.getExt());
        logger.info("onStreamPlay (cachename)  : " + stream.getCacheName());
        logger.info("onStreamPlay (contextstr) : " + stream.getContextStr());
        logger.info("onStreamPlay (querystr)   : " + stream.getQueryStr());
        logger.info("onStreamPlay (streamtype) : " + stream.getStreamType());
    }

    private void logClient(String s, IClient client) {
        if (client != null){
            logger.info(s+ " (client query string) : " + client.getQueryStr());
            logger.info(s+ " (client page url)     : " + client.getPageUrl());
            logger.info(s+ " (client uri)          : " + client.getUri());
        }
    }

    public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
        logger.info("onMetaData By: " + stream.getClientId());
    }

    public void onPauseRaw(IMediaStream stream, boolean isPause,
                           double location) {
        logger.info("onPauseRaw By: " + stream.getClientId());
    }

    public void onSeek(IMediaStream stream, double location) {
        logger.info("onSeek");
    }

    public void onStop(IMediaStream stream) {
        logger.info("onStop By: " + stream.getClientId());
        stream.shutdown();
    }

    public void onUnPublish(IMediaStream stream, String streamName,
                            boolean isRecord, boolean isAppend) {
        logger.info("onUnPublish");
    }

    public  void onPublish(IMediaStream stream, String streamName,
                           boolean isRecord, boolean isAppend) {
        logger.info("onPublish");
        if (stream.getClient() != null){
            logClient("onStreamPublish ",stream.getClient());
        }
        logger.info("onStreamPublish (name)     : " + stream.getName());
        logger.info("onStreamPublish (name)     : " + streamName);
        logger.info("onStreamPublish (ext)  : " + stream.getExt());
        logger.info("onStreamPublish (cachename)     : " + stream.getCacheName());
        logger.info("onStreamPublish (contextstr)     : " + stream.getContextStr());
        logger.info("onStreamPublish (querystr)     : " + stream.getQueryStr());
        logger.info("onStreamPublish (streamtype)     : " + stream.getStreamType());


    }

    public void onPause(IMediaStream stream, boolean isPause,
                        double location) {
        logger.info("onPause");
    }
 }
