package de.henningbrinkmann.toggl2sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

class TimeSheetRecord {
    private static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("dd.MM.yy");
    private static final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm");

    final private DateTime start;
    final private DateTime end;
    final private long duration;
    final private Map<String, Long> durationByProject;
    final private Set<String> description;

    TimeSheetRecord(final List<TogglRecord> togglRecords) {
        final Optional<TogglRecord> minStart = togglRecords.stream()
                .min((a, b) -> a.getStart().compareTo(b.getStart()));
        if (minStart.isPresent()) {
            this.start = minStart.get().getStart();
        } else {
            this.start = null;
        }

        final Optional<TogglRecord> maxEnd = togglRecords.stream().max((a, b) -> a.getEnd().compareTo(b.getEnd()));
        if (maxEnd.isPresent()) {
            this.end = maxEnd.get().getEnd();
        } else {
            this.end = null;
        }

        this.duration = togglRecords.stream()
                .collect(Collectors.summarizingLong(TogglRecord::getDuration)).getSum();

       durationByProject = togglRecords.stream()
                .collect(Collectors.toMap(TogglRecord::getProject,
                        TogglRecord::getDuration,
                        (a, b) -> a + b));

        description = togglRecords.stream()
                .map(TogglRecord::getDescription)
                .collect(Collectors.toSet());
    }


    private long getPause() {
        return end.getMillis() - start.getMillis() - duration;
    }

    String toTSV(final Set<String> projects) {
        final ArrayList<String> strings = new ArrayList<>();

        strings.add(dayFormatter.print(start));
        strings.add(hourFormatter.print(start));
        strings.add(hourFormatter.print(end));
        strings.add(longToHourString(getPause()));
        strings.add(longToHourString(duration));

        projects.forEach(project -> {
            final Long duration = durationByProject.get(project);

            strings.add(duration == null ? "     " : longToHourString(duration));
        });

        strings.add(description.stream().collect(Collectors.joining(", ")));


        return strings.stream().collect(Collectors.joining("\t"));
    }

    static String toHeadings(final Set<String> projects) {
        final ArrayList<String> strings = new ArrayList<>();

        strings.add("Datum   ");
        strings.add("Von  ");
        strings.add("Bis  ");
        strings.add("Pause");
        strings.add("Arbeit");

        projects.forEach(project -> strings.add(String.format("%5s", project)));

        strings.add("Tätigkeit");

        return strings.stream().collect(Collectors.joining("\t"));
    }

    private String longToHourString(final long millis) {
        final DateTime dateTime = new DateTime(millis, DateTimeZone.UTC);

        return hourFormatter.print(dateTime);
    }

    DateTime getStart() {
        return start;
    }
}
