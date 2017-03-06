package de.henningbrinkmann.toggl2sheet;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

class Util {
    static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("dd.MM.yy");
    static final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm");

    static String longToHourString(final long millis) {
        final long seconds = millis / 1000;
        final long minutes = seconds / 60;
        final long hours = minutes / 60;

        return String.format("%d:%02d", hours, minutes % 60);
    }
}
