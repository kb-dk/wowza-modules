package dk.statsbiblioteket.chaos.wowza.plugin.bes;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamFileMapper;

import dk.statsbiblioteket.chaos.wowza.plugin.bes.ObjectStatus;
import dk.statsbiblioteket.chaos.wowza.plugin.util.StringAndTextUtil;

public class DomsShardIdToFileMapper implements IMediaStreamFileMapper {

	private WMSLogger logger;
	private WebResource besRestApi;
	private IMediaStreamFileMapper defaultMapper;
	private String streamStorageDir;

	public DomsShardIdToFileMapper(IMediaStreamFileMapper defaultMapper, String streamStorageDir, WebResource besRestApi) {
		this.logger = WMSLoggerFactory.getLogger(this.getClass());
		logger.info("Creating " + this.getClass().getSimpleName() + " dir=" + streamStorageDir);
		this.defaultMapper = defaultMapper;
		this.streamStorageDir = streamStorageDir;
		this.besRestApi = besRestApi;
	}

	@Override
	public File streamToFileForRead(IMediaStream stream) {
		logger.info("streamToFileForRead(IMediaStream stream)");
		String name = stream.getName();
		String ext = stream.getExt();
		String query = stream.getQueryStr();
		return streamToFileForRead(stream, name, ext, query);
	}

	@Override
	public File streamToFileForRead(IMediaStream stream, String name, String ext, String streamQuery) {
		logger.debug("streamToFileForRead(stream " + stream + ", name " + name + ", ext " + ext + ", streamQuery " + streamQuery + ")");
		String filename = stream.getName();
		File resultingFile = null;
		Matcher matcher = Pattern.compile("^([^\\.]*)\\.mp3").matcher(filename);
		if (matcher.find()) {
			String shardID = matcher.group(1);
			String relativeFilePath = retrieveMediaFileRelativePath(stream, shardID);
			resultingFile = new File(streamStorageDir + "/" + relativeFilePath);
			logger.debug("Resulting file: " + resultingFile.getAbsolutePath());
		} else {
			logger.warn("Resource stream name did not match the expected format. Was " + filename);
			resultingFile = new File("no-valid-uuid-given.mp3");
		}
		return resultingFile;
	}

	@Override
	public File streamToFileForWrite(IMediaStream stream) {
		return defaultMapper.streamToFileForWrite(stream);
	}

	@Override
	public File streamToFileForWrite(IMediaStream stream, String name, String ext, String query) {
		return defaultMapper.streamToFileForWrite(stream, name, ext, query);
	}

	/**
	 * Retrieve and parse object status from BES. Return path to file.
	 * 
	 * Object status has the XML format:
	 * <objectstatus>
	 *   <completionPercentage>100.0</completionPercentage>
	 *   <positionInQueue>0</positionInQueue>
	 *   <serviceUrl>rtmp://thalassa.statsbiblioteket.dk/kultur</serviceUrl>
	 *   <status>DONE</status>
	 *   <streamId>flv:0/b/a/a/0baa9285-b3ce-41ed-a4c4-71cd4c443025.flv</streamId>
	 * </objectstatus>
	 * 
	 * @param stream the stream from Wowza
	 * @param shardID the ID of the shard requested to be played
	 * @return path and filename to the file that matches the shardID
	 */
	protected String retrieveMediaFileRelativePath(IMediaStream stream, String shardID) {
		String filenameAndPath = null;
		try {
			logger.info("ObjectStatus: " + besRestApi.path("/getobjectstatus").queryParam("programpid", "uuid:" + shardID).toString());
			ObjectStatus objectStatusXml = besRestApi.path("/getobjectstatus").queryParam("programpid", "uuid:" + shardID).get(ObjectStatus.class);
			logger.debug("ObjectStatus: " + objectStatusXml);
			String streamID = objectStatusXml.getStreamId();
			int indexOfColon = streamID.indexOf(":");
			filenameAndPath = streamID.substring(indexOfColon+1, streamID.length());
		}  catch (UniformInterfaceException e) {
			logger.warn("UniformInterfaceException occured. ID might be invalidated.", e);
		}
		logger.info("Resolved relative path: " + filenameAndPath);
		return filenameAndPath;
	}

}
