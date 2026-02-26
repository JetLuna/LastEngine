package net.jetluna.api.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    private static final Pattern PATTERN = Pattern.compile("(?:([0-9]+)d)?(?:([0-9]+)h)?(?:([0-9]+)m)?(?:([0-9]+)s)?");

    // Парсинг (1d2h -> миллисекунды)
    public static long parseDuration(String input) {
        Matcher m = PATTERN.matcher(input);
        long total = 0;
        if (m.find()) {
            if (m.group(1) != null) total += Long.parseLong(m.group(1)) * 86400000L;
            if (m.group(2) != null) total += Long.parseLong(m.group(2)) * 3600000L;
            if (m.group(3) != null) total += Long.parseLong(m.group(3)) * 60000L;
            if (m.group(4) != null) total += Long.parseLong(m.group(4)) * 1000L;
        }
        return total;
    }

    // НОВЫЙ МЕТОД: Форматирование (миллисекунды -> 1д 2ч 30м)
    public static String formatDuration(long millis) {
        long sec = millis / 1000;
        long min = sec / 60;
        long hour = min / 60;
        long day = hour / 24;

        StringBuilder sb = new StringBuilder();
        if (day > 0) sb.append(day).append("д ");
        if (hour % 24 > 0) sb.append(hour % 24).append("ч ");
        if (min % 60 > 0) sb.append(min % 60).append("м ");
        if (sec % 60 > 0 && day == 0 && hour == 0) sb.append(sec % 60).append("с"); // Секунды только если мало времени

        String result = sb.toString().trim();
        return result.isEmpty() ? "меньше секунды" : result;
    }
}