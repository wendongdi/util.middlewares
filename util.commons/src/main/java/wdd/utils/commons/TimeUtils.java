package wdd.utils.commons;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static final int DEFAULT_START_TIME = 1388505600;
    public static final int DEFAULT_START_TIME_2012 = 1325347200;

    public static Date getAbsoluteDate(int offset, TimeUnit timeUnit) {
        long absoluteTime = (1388505600L + timeUnit.toSeconds(offset)) * 1000L;
        return new Date(absoluteTime);
    }

    public static Date getAbsoluteDate2012(int offset, TimeUnit timeUnit) {
        long absoluteTime = (1325347200L + timeUnit.toSeconds(offset)) * 1000L;
        return new Date(absoluteTime);
    }

    public static int getOffsetSecond() {
        return getOffsetSecond(new Date());
    }

    public static int getOffsetDay() {
        return getOffsetDay(new Date());
    }

    public static int getOffsetDay2012() {
        return getOffsetDay2012(new Date());
    }

    public static int getOffsetSecond(Date date) {
        return (int) (date.getTime() / 1000L - 1388505600L);
    }

    public static int getOffsetSecond2012(Date date) {
        return (int) (date.getTime() / 1000L - 1325347200L);
    }

    public static int getOffsetDay(Date date) {
        return getOffsetSecond(date) / 24 / 3600;
    }

    public static int getOffsetDay2012(Date date) {
        return getOffsetSecond2012(date) / 24 / 3600;
    }

    public static int getTodayLeftSecond() {
        long now = System.currentTimeMillis() / 1000L;
        long end = now / 86400L * 86400L + 57600L;
        return end > now ? (int) (end - now) : (int) (end - now + 86400L);
    }

    public static int getHourLeftSecond() {
        long now = System.currentTimeMillis() / 1000L;
        long end = now / 3600L * 3600L + 3600L;
        return (int) (end - now);
    }

    public static boolean between(Date source, Date start, Date end) {
        if ((source == null) || (start == null) || (end == null)) return true;
        int s = source.compareTo(start);
        int e = source.compareTo(end);
        return (s >= 0) && (e <= 0);
    }

    public static int getCurrentMinute() {
        long now = System.currentTimeMillis() / 1000L;
        long start = now / 3600L * 3600L;
        return (int) (now - start) / 60;
    }
}