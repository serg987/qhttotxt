import org.junit.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DuplicateChecker {
    @Test
    public void printDuplicatesInTxts() {
        Configuration.workingDir = "E:\\!Temp\\210423\\CombHistory";
        Configuration.recursiveSearch = false;
        Configuration.zoneId = ZoneId.of("Europe/Moscow");
        List<Path> paths = IOHelper.getPathListTxt();
        Map<Path, List<String>> map = IOHelper.convertFilesToStrings(paths);
        for (Map.Entry<Path, List<String>> entry : map.entrySet()) {
            Map<Integer, Integer> timeMap = new HashMap<>();
            for (String str : entry.getValue()) {
                Pattern pattern = Pattern.compile(Configuration.qip_icq_timeline);
                Matcher m = pattern.matcher(str);
                if (m.find()) {
                    String headerTime = str.split("\\(")[1].replace(")", "");
                    int unixDate = Commons.parseDateTime(headerTime);
                    Integer time = new Integer(unixDate);
                    timeMap.computeIfPresent(time, (k, v) -> v + 1);
                    timeMap.putIfAbsent(time, 1);
                }
            }
            timeMap = timeMap.entrySet().stream().filter(e -> e.getValue() > 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (timeMap.size() > 0) {
                System.out.println("File " + entry.getKey().getFileName() + " has " +
                        timeMap.size() + " duplicate times:");

                timeMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((e) -> {
                        ZonedDateTime zonedDateTime = Instant.ofEpochSecond(e.getKey()).atZone(Configuration.zoneId);
                        String time = zonedDateTime.format(DateTimeFormatter.ofPattern(Configuration.timePatternInTxt));
                        System.out.println("Time : " + time + " - " + e.getValue() + " times");
                    }
                );
            }
        }
    }
}
