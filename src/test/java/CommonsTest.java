import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.*;

public class CommonsTest {

    @Test
    public void parseDateTime() {
        Configuration.zoneId = ZoneId.of("Europe/Moscow");
        String[] datetimes = {"09.12.08 09:03:44", "09.12.08 9:03:44", "09/12/08 9:03:44", "09/12/08 09:03:44",
                "09:03:44 09.12.08", "09:03:44 09.12.2008", "09:03:44 09/12/2008", "9:03:44 09.12.2008",
                "9:03:44 09.12.08"};

        for (String dateTime : datetimes) {
            Assert.assertEquals(1228802624, Commons.parseDateTime(dateTime));
        }
    }
}