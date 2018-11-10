package de.henningbrinkmann.toggl2sheet;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class Util {
    private final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("dd.MM.yy");
    private final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm");

    private final NonWorkingdays nonWorkingdays;

    @Autowired
    public Util(NonWorkingdays nonWorkingdays) {
        this.nonWorkingdays = nonWorkingdays;
    }

    public String longToHourString(final long millis) {
        if (millis == 0) {
            return "     ";
        }

        final long seconds = millis / 1000;
        final long minutes = seconds / 60;
        final long hours = minutes / 60;

        return String.format("%d:%02d", hours, minutes % 60);
    }

    public long getSollarbeitszeit(DateTime start, DateTime end) {
        long result = 0;
        DateTime dateTime = start;
        while (! end.isBefore(dateTime)) {
            if (nonWorkingdays.getNonWorkingDay(dateTime) == null) {
                result += 8 * 60 * 60 * 1000;
            }

            dateTime = dateTime.plusDays(1);
        }

        return result;
    }

    public DateTimeFormatter getDayFormatter() {
        return dayFormatter;
    }

    public DateTimeFormatter getHourFormatter() {
        return hourFormatter;
    }
}
