package de.henningbrinkmann.toggl2sheet;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

class Util {
    static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("dd.MM.yy");
    static final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm");

    static final DateTime partimeStart = DateTime.parse("2017-04-01");

    static final Map<DateTime, String> holidays = new HashMap<>();

    static {
        addHoliday("2017-01-01", "Neujahrstag");
        addHoliday("2017-04-14", "Karfreitag");
        addHoliday("2017-04-17", "Ostermontag");
        addHoliday("2017-05-01", "Tag der Arbeit");
        addHoliday("2017-05-25", "Christi Himmelfahrt");
        addHoliday("2017-06-05", "Pfingstmontag");
        addHoliday("2017-10-03", "Tag der Deutschen Einheit");
        addHoliday("2017-10-31", "Reformationstag");
        addHoliday("2017-12-26", "Weihnachten");
        addHoliday("2017-12-27", "Zweiter Weihnachtstag");
    }

    private static void addHoliday(String date, String description) {
        DateTime dateTime = DateTime.parse(date);
        holidays.put(dateTime, description);
    }

    static String longToHourString(final long millis) {
        final long seconds = millis / 1000;
        final long minutes = seconds / 60;
        final long hours = minutes / 60;

        return String.format("%d:%02d", hours, minutes % 60);
    }

    static String getHoliday(DateTime dateTime) {
        return holidays.get(dateTime.withTimeAtStartOfDay());
    }

    static String getNoWorkday(DateTime dateTime) {
        String result = getHoliday(dateTime);

        if (result == null) {
            if (dateTime.getDayOfWeek() == 6 || dateTime.getDayOfWeek() == 7) {
                result = "Wochenende";
            }

            if (dateTime.getDayOfWeek() == 1 && ! dateTime.isBefore(partimeStart)) {
                result = "frei (Teilzeit)";
            }
        }

        return result;
    }

    static long getSollarbeitszeit(DateTime start, DateTime end) {
        long result = 0;
        DateTime dateTime = start;
        do {
            if (getNoWorkday(dateTime) == null) {
                result += 8;
            }

            dateTime = dateTime.plusDays(1);
        } while (! end.isBefore(dateTime));

        return result;
    }
}
