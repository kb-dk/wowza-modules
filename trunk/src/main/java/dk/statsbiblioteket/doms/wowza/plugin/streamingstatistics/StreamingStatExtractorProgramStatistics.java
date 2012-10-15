package dk.statsbiblioteket.doms.wowza.plugin.streamingstatistics;

import com.wowza.wms.logging.WMSLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class StreamingStatExtractorProgramStatistics {

    private WMSLogger logger;

    // Title -> Channel -> Date -> Play count
    private Map<String, Map<String, Map<String, PlayCounter>>> titleMap;

    public StreamingStatExtractorProgramStatistics(WMSLogger logger) {
        this.logger = logger;
        this.titleMap = new HashMap<String, Map<String, Map<String, PlayCounter>>>();
    }

    public void add(StreamingStatLogEntry logEntry) {
        Map<String, Map<String, PlayCounter>> channelProgramMap = titleMap.get(logEntry.getProgramTitle());
        if (channelProgramMap == null) {
            channelProgramMap = new HashMap<String, Map<String, PlayCounter>>();
            titleMap.put(logEntry.getProgramTitle(), channelProgramMap);
        }
        Map<String, PlayCounter> dateChannelProgramMap = channelProgramMap.get(logEntry.getChannelID());
        if (dateChannelProgramMap == null) {
            dateChannelProgramMap = new HashMap<String, PlayCounter>();
            channelProgramMap.put(logEntry.getChannelID(), dateChannelProgramMap);
        }
        PlayCounter playCountDateChannelProgram = dateChannelProgramMap.get(logEntry.getProgramStart());
        if (playCountDateChannelProgram == null) {
            playCountDateChannelProgram = new PlayCounter();
            dateChannelProgramMap.put(logEntry.getProgramStart(), playCountDateChannelProgram);
        }
        playCountDateChannelProgram.add(1);
    }

    public Set<String> getProgramTitles() {
        return titleMap.keySet();
    }

    public Object getPlayCount() {
        return getPlayCountFromProgramMap(titleMap);
    }

    public int getPlayCount(String programTitle) {
        Map<String, Map<String, PlayCounter>> channelProgramMap = titleMap.get(programTitle);
        return getPlayCountFromProgramChannelMap(channelProgramMap);
    }

    public int getPlayCount(String programTitle, String channel) {
        int playCount = 0;
        Map<String, Map<String, PlayCounter>> programChannelMap = titleMap.get(programTitle);
        if (programChannelMap != null) {
            Map<String, PlayCounter> programChannelDateMap = programChannelMap.get(channel);
            if (programChannelDateMap != null) {
                playCount = getPlayCountFromProgramChannelDateMap(programChannelDateMap);
            }
        }
        return playCount;
    }

    public int getPlayCount(String programTitle, String channel, String date) {
        int playCount = 0;
        Map<String, Map<String, PlayCounter>> programChannelMap = titleMap.get(programTitle);
        if (programChannelMap != null) {
            Map<String, PlayCounter> programChannelDateMap = programChannelMap.get(channel);
            if (programChannelDateMap != null) {
                PlayCounter playCountProgramChannelDate = programChannelDateMap.get(date);
                if (playCountProgramChannelDate != null) {
                    playCount = playCountProgramChannelDate.getCount();
                }
            }
        }
        return playCount;
    }

    private int getPlayCountFromProgramMap(Map<String, Map<String, Map<String, PlayCounter>>> programMap) {
        PlayCounter playCounter = new PlayCounter();
        if (programMap != null) {
            for (Iterator<Map<String, Map<String, PlayCounter>>> programMapIterator = programMap.values().iterator();
                 programMapIterator.hasNext(); ) {
                Map<String, Map<String, PlayCounter>> programChannelMap = programMapIterator.next();
                int datePlayCount = getPlayCountFromProgramChannelMap(programChannelMap);
                playCounter.add(datePlayCount);
            }
        }
        return playCounter.getCount();
    }

    private int getPlayCountFromProgramChannelMap(Map<String, Map<String, PlayCounter>> programChannelMap) {
        PlayCounter playCounter = new PlayCounter();
        if (programChannelMap != null) {
            for (Iterator<Map<String, PlayCounter>> channelProgramMapIterator = programChannelMap.values().iterator();
                 channelProgramMapIterator.hasNext(); ) {
                Map<String, PlayCounter> dateChannelProgramMap = channelProgramMapIterator.next();
                int datePlayCount = getPlayCountFromProgramChannelDateMap(dateChannelProgramMap);
                playCounter.add(datePlayCount);
            }
        }
        return playCounter.getCount();
    }

    private int getPlayCountFromProgramChannelDateMap(Map<String, PlayCounter> programChannelDateMap) {
        PlayCounter datePlayCounter = new PlayCounter();
        if (programChannelDateMap != null) {
            for (Iterator<PlayCounter> dateChannelProgramMapIterator = programChannelDateMap.values().iterator();
                 dateChannelProgramMapIterator.hasNext(); ) {
                PlayCounter playCountDateChannelProgram = dateChannelProgramMapIterator.next();
                datePlayCounter.add(playCountDateChannelProgram.getCount());
            }
        }
        return datePlayCounter.getCount();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("ProgramStat:\n");
        for (Iterator<String> programIterator = titleMap.keySet().iterator(); programIterator.hasNext(); ) {
            String programTitle = programIterator.next();
            sb.append(" - " + programTitle + "\n");
            Map<String, Map<String, PlayCounter>> channelProgramMap = titleMap.get(programTitle);
            for (Iterator<String> channelIterator = channelProgramMap.keySet().iterator();
                 channelIterator.hasNext(); ) {
                String channel = channelIterator.next();
                sb.append("   - " + channel + "\n");
                Map<String, PlayCounter> programChannelDateMap = channelProgramMap.get(channel);
                for (Iterator<String> dateIterator = programChannelDateMap.keySet().iterator();
                     dateIterator.hasNext(); ) {
                    String date = dateIterator.next();
                    PlayCounter playCounter = programChannelDateMap.get(date);
                    sb.append("     - " + date + ": " + playCounter.getCount() + "\n");
                }
            }
        }
        return sb.toString();
    }
}
