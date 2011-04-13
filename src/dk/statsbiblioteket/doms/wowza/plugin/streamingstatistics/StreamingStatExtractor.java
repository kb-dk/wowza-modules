package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;

import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;

public class StreamingStatExtractor {
	
	private WMSLogger logger;
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    private String logFileBaseDir;
    
    protected List<StreamingStatLogEntry> listOfLogEntries;
	protected StreamingStatExtractorProgramStatistics programCentricStat;

	public StreamingStatExtractor(WMSLogger logger, BufferedReader logReader) throws IOException {
		super();
		this.logger = logger;
		this.listOfLogEntries = new ArrayList<StreamingStatLogEntry>();
		this.programCentricStat = new StreamingStatExtractorProgramStatistics(logger);
		readLog(logReader);
	}
	
	public StreamingStatExtractor(WMSLogger logger, String logFilesBaseDir, String startDateString, String endDateString) throws IOException, ParseException {
		super();
		this.logger = logger;
		this.logFileBaseDir = logFilesBaseDir;
		this.listOfLogEntries = new ArrayList<StreamingStatLogEntry>();
		this.programCentricStat = new StreamingStatExtractorProgramStatistics(logger);
		readAndCategorizeLog(startDateString, endDateString);
	}
	
	public int getNumberOfStartedProgramViews() {
		return listOfLogEntries.size();
	}
	
	private void readAndCategorizeLog(String startDateString, String endDateString) throws ParseException {
		Date startDate = sdf.parse(startDateString);
		Date endDate = sdf.parse(endDateString);
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		while (cal.getTime().before(endDate)) {
			String filename = logFileBaseDir + "/" + StreamingEventLogger.getFilename(cal.getTime());
			File logFile = new File(filename);
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
				readLog(bufferedReader);
			} catch (FileNotFoundException e) {
				logger.debug("Log file not found: " + logFile.getAbsolutePath(), e);
			} catch (IOException e) {
				logger.info("Error reading file: " + logFile.getAbsolutePath(), e);
			}
			cal.add(Calendar.DATE, 1);
		}
	}

	protected void readLog(BufferedReader logReader) throws IOException {
		String logLine = logReader.readLine();
		while (logLine != null) {
			StreamingStatLogEntry logEntry;
			try {
				logEntry = new StreamingStatLogEntry(logger, logLine);
				this.listOfLogEntries.add(logEntry);
				this.programCentricStat.add(logEntry);
			} catch (InvalidLogLineParseException e) {
				logger.error("Invalid log line found: " + logLine);
			} catch (HeadlineEncounteredException e) {
				// ignore headlines
				logger.debug("Headline found in log file: " + logLine);
			}
			logLine = logReader.readLine();
		}
	}
	
	public static void main(String args[]) throws IOException, ParseException {
        if (args.length < 3) {
        	System.out.println("Missing arguments. 3 required.");
        	System.out.println(" 1. Base dir for log files");
            System.out.println(" 2. Start date of format " + DATE_PATTERN);
            System.out.println(" 3. End date of format " + DATE_PATTERN);
        	System.out.println("");
        	System.out.println("Parameter example: /home/wowza/services/wowza_vhost_kultur/logs/streamingStatistics 2011-04-08 2011-04-15");
            System.exit(1);
        }
        String logFileBaseDir = args[0];
        String startDateString = args[1];
        String endDateString = args[2];

        System.out.println("Read log file base dir: " + logFileBaseDir);
        System.out.println("Read start date       : " + startDateString + "(inclusive)");
        System.out.println("Read end date         : " + endDateString + "(exclusive)");
        
        try {
			sdf.parse(startDateString);
	        sdf.parse(endDateString);
		} catch (ParseException e) {
            System.out.println("Could not parse date. Expected format: " + DATE_PATTERN);
            System.out.println("Cause: " + e.getMessage());
		}
        
		Layout layout = new PatternLayout("%-5p [%d{yyyy-MM-dd HH.mm:ss}] %C{1}.%M(%L): %m%n");
		Appender fileAppender = new FileAppender(layout, StreamingStatExtractor.class.getSimpleName() + "-" + sdf.format(new Date()) + ".log", true);
		org.apache.log4j.BasicConfigurator.configure(fileAppender);
		WMSLogger logger = WMSLoggerFactory.getLogger(StreamingStatExtractor.class);
		StreamingStatExtractor extractor = new StreamingStatExtractor(logger, logFileBaseDir, startDateString, endDateString);
		System.out.println("=========================================================================================");
		System.out.println("Analyzing streaming statistics log for the period: " + startDateString + " - " + endDateString);
		System.out.println("Number of started program views: " + extractor.getNumberOfStartedProgramViews());
		System.out.println("Distributed by program titles:");
		
		List<String> programTitles = new ArrayList<String>(extractor.programCentricStat.getProgramTitles());
		Collections.sort(programTitles);
		for (Iterator<String> iterator = programTitles.iterator(); iterator.hasNext();) {
			String programTitle = iterator.next();
			int playCount = extractor.programCentricStat.getPlayCount(programTitle);
			System.out.println(" - " + programTitle + ": " + playCount);
		}
		
		System.out.println("=========================================================================================");
	}
}
