package de.henningbrinkmann.toggl2sheet;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

class Util {
    static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("dd.MM.yy");
    static final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm");

    static String longToHourString(final long millis) {
        if (millis == 0) {
            return "     ";
        }

        final long seconds = millis / 1000;
        final long minutes = seconds / 60;
        final long hours = minutes / 60;

        return String.format("%d:%02d", hours, minutes % 60);
    }

    static long getSollarbeitszeit(DateTime start, DateTime end) {
        long result = 0;
        DateTime dateTime = start;
        do {
            if (NonWorkingdays.INSTANCE.getNonWorkingDay(dateTime) == null) {
                result += 8 * 60 * 60 * 1000;
            }

            dateTime = dateTime.plusDays(1);
        } while (! end.isBefore(dateTime));

        return result;
    }
}
