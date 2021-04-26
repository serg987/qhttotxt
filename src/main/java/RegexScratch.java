import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexScratch {

    public static void main(String[] args) {
        String str1 = "-------------------------------------->-";
        String str2 = "--------------------------------------<-";
        String str3 = "-------------------------------------<-";

        String pattern1 = "^[-]{38}[<>][-]";
        System.out.println(str1.matches(pattern1));
        System.out.println(str2.matches(pattern1));
        System.out.println(str3.matches(pattern1));

        str1 = "Kisser (19:32:35 20/04/2015)";
        str2 = "D E (19:31:47 20/04/2015)";
        str3 = "Антонио (19:29:17 20/04/2015)";

        pattern1 = "^[\\d|\\p{L}|\\s|@|\\.]*\\s[(][\\d|:]*\\s[\\d|\\/]*[)]";
        System.out.println(str1.matches(pattern1));
        System.out.println(str2.matches(pattern1));
        System.out.println(str3.matches(pattern1));

        str1 = "09.12.08 18:03:44>тут еще?";
        str2 = "11.12.08 10:21:00<надо будет как-нибудь взять  ;-)";
        str3 = "11.12.08 10:2!:00<надо будет как-нибудь взять  ;-)";
        Pattern mchatLineHeaderPattern = Pattern.compile("^([\\d]{2}[\\/|\\.]){2}[\\d]{2,4}\\s[\\d]{1,2}([:][\\d]{2}){2}[<|>]");
        Matcher m = mchatLineHeaderPattern.matcher(str1);
        if (m.find()) {
            System.out.println(m.group());
        } else System.out.println("No matches");
        m = mchatLineHeaderPattern.matcher(str2);
        if (m.find()) {
            System.out.println(m.group());
        } else System.out.println("No matches");
        m = mchatLineHeaderPattern.matcher(str3);
        if (m.find()) {
            System.out.println(m.group());
        } else System.out.println("No matches");

        System.out.println(str3.replace(".txt", ""));

        str1 = "09.12.08 09:03:44";
        Configuration.zoneId = ZoneId.of("Europe/Moscow");
        System.out.println(Commons.parseDateTime(str1));

    }
}
