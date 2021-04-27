import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Commons {

    static void addCRtoStringBuilder(StringBuilder stringBuilder) {
        stringBuilder.append(System.getProperty("line.separator"));
    }

    static StringBuilder populateStringWithListElems(List<String> stringList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (stringList.size() == 0) return stringBuilder;
        stringBuilder.append("'");
        for (int i = 0; i < stringList.size(); i++) {
            stringBuilder.append(stringList.get(i));
            if (i != stringList.size() - 1) stringBuilder.append("', '");
        }
        stringBuilder.append("'");
        return stringBuilder;
    }

    public static int parseDateTime(String dateTime) {
        String[] dateTimeStrs = dateTime.split(" ");
        String date;
        String time;
        if (dateTimeStrs[0].contains(":")) {
            time = dateTimeStrs[0];
            date = dateTimeStrs[1];
        } else {
            time = dateTimeStrs[1];
            date = dateTimeStrs[0];
        }
        if (date.contains(".")) date = date.replaceAll("\\.", "/");
        int year = Integer.parseInt(date.split("/")[2]);
        if (year < 100) {
            year = year + 2000;
            if (year > 2050) year = year - 100;
            date = date.substring(0, 6) + year;
        }
        if (time.length() == 7) time = "0" + time;
        if (date.length() == 9) date = "0" + date;
        String standartizedDateTime = time + " " + date;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        ZonedDateTime zdt = LocalDateTime.parse(standartizedDateTime, dtf).atZone(Configuration.zoneId);
        return (int) zdt.toEpochSecond();
    }
}
