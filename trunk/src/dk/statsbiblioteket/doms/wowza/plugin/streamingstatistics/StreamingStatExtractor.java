package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

public class StreamingStatExtractor {
	
	private List<StreamingStatLogEntry> listOfLogEntries;

	private WMSLogger logger;
	
	public StreamingStatExtractor(WMSLogger logger, BufferedReader logReader) throws IOException {
		super();
		this.logger = logger;
		readAndCategorizeLog(logReader);
	}
	
	public int getNumberOfStartedProgramViews() {
		return listOfLogEntries.size();
	}
	
	protected void readAndCategorizeLog(BufferedReader logReader) throws IOException {
		this.listOfLogEntries = readLog(logReader);
		
	}
	
	protected List<StreamingStatLogEntry> readLog(BufferedReader logReader) throws IOException {
		List<StreamingStatLogEntry> logEntries = new ArrayList<StreamingStatLogEntry>();
		String logLine = logReader.readLine();
		while (logLine != null) {
			StreamingStatLogEntry logEntry;
			try {
				logEntry = new StreamingStatLogEntry(logger, logLine);
				logEntries.add(logEntry);
			} catch (InvalidLogLineParseException e) {
				logger.error("Invalid log line found: " + logLine);
			} catch (HeadlineEncounteredException e) {
				// ignore headlines
				logger.info("Headline found in log file: " + logLine);
			}
			logLine = logReader.readLine();
		}
		return logEntries;
	}
	
	public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Missing argument: log file");
            System.exit(1);
        }
        File logFile = new File(args[0]);
        try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
			WMSLogger logger = WMSLoggerFactory.getLogger(StreamingStatExtractor.class);
			StreamingStatExtractor extractor = new StreamingStatExtractor(logger, bufferedReader);
			System.out.println("=========================================================================================");
			System.out.println("Analyzing streaming statistics log");
			System.out.println("Input file: " + logFile.getAbsolutePath());
			System.out.println("Number of started program views: " + extractor.getNumberOfStartedProgramViews());
			System.out.println("=========================================================================================");
		} catch (FileNotFoundException e) {
            System.out.println("Could not find file " + logFile.getAbsolutePath() + "\n" + e.getMessage());
            System.exit(1);
		} catch (IOException e) {
            System.out.println("An IO errored occured.\n" + e.getMessage());
            System.exit(1);
		}
	}
}
