package utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gali on 9/16/17.
 */
public class DateUtils {

    public static Date fromSqlDate(java.sql.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        return calendar.getTime();
    }

}
