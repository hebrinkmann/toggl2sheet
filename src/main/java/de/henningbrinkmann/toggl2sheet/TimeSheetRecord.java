package de.henningbrinkmann.toggl2sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by hso72 on 01.03.2017.
 */
public class TimeSheetRecord {
    public static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("dd.MM.yy");
    public static final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm");

    final private DateTime start;
    final private DateTime end;
    final private long duration;
    final private Map<String, Long> durationByClient;
    final private Map<String, Long> durationByProject;
    final private Set<String> description;

    TimeSheetRecord(List<TogglRecord> togglRecords) {
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

        durationByClient = togglRecords.stream()
                .collect(Collectors.toMap(TogglRecord::getClient,
                        TogglRecord::getDuration,
                        (a, b) -> a + b));
        durationByProject = togglRecords.stream()
                .collect(Collectors.toMap(TogglRecord::getProject,
                        TogglRecord::getDuration,
                        (a, b) -> a + b));

        description = togglRecords.stream()
                .map(togglRecord -> togglRecord.getDescription())
                .collect(Collectors.toSet());
    }


    public long getPause() {
        return end.getMillis() - start.getMillis() - duration;
    }

    public String toTSV(Set<String> projects) {
        ArrayList<String> strings = new ArrayList<>();

        strings.add(dayFormatter.print(start));
        strings.add(hourFormatter.print(start));
        strings.add(hourFormatter.print(end));
        strings.add(longToHourString(getPause()));
        strings.add(longToHourString(duration));

        projects.stream().forEach(project -> {
            Long duration = durationByProject.get(project);

            strings.add(duration == null ? "     " : longToHourString(duration));
        });

        strings.add(description.stream().collect(Collectors.joining(", ")));


        return strings.stream().collect(Collectors.joining("\t"));
    }

    static public String toHeadings(Set<String> projects) {
        ArrayList<String> strings = new ArrayList<>();

        strings.add("Datum   ");
        strings.add("Von  ");
        strings.add("Bis  ");
        strings.add("Pause");
        strings.add("Arbeit");

        projects.stream().forEach(project -> strings.add(String.format("%5s", project)));

        strings.add("Tätigkeit");

        return strings.stream().collect(Collectors.joining("\t"));
    }

    public String longToHourString(long millis) {
        final DateTime dateTime = new DateTime(millis, DateTimeZone.UTC);

        return hourFormatter.print(dateTime);
    }

    public DateTime getStart() {
        return start;
    }
}
