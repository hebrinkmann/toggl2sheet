package de.henningbrinkmann.toggl2sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.DateTime;

class TimeSheetRecord implements Comparable<TimeSheetRecord> {

    public static final String BLANK = "     ";
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

    TimeSheetRecord(DateTime dateTime) {
        this.start = dateTime.withTimeAtStartOfDay();
        this.end = null;
        this.durationByProject = new HashMap<>();
        this.description = new HashSet<>();
        String description = NonWorkingdays.INSTANCE.getNonWorkingDay(start);
        long duration = 0;
        if (description == null) {
            description = "?";
            duration = 8 * 60 * 60 * 1000;
        }
        this.duration = duration;
        this.description.add(description);
    }


    private long getPause() {

        if (end == null || start == null) {
            return 0;
        }

        return end.getMillis() - start.getMillis() - duration;
    }

    String toTSV(final List<String> projects) {
        final ArrayList<String> strings = new ArrayList<>();

        strings.add(Util.dayFormatter.print(start));

        if (end != null) {
            strings.add(Util.hourFormatter.print(start));
            strings.add(Util.hourFormatter.print(end));
            strings.add(Util.longToHourString(getPause()));
        } else {
            IntStream.range(0, 3).forEach(i -> strings.add(BLANK));
        }

        strings.add(Util.longToHourString(duration));

        projects.forEach(project -> {
            final Long duration = durationByProject.get(project);

            strings.add(duration == null ? BLANK : Util.longToHourString(duration));
        });

        strings.add(description.stream().collect(Collectors.joining(", ")));


        return strings.stream().collect(Collectors.joining("\t"));
    }

    static String toHeadings(final List<String> projects) {
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

    DateTime getStart() {
        return start;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public int compareTo(TimeSheetRecord o) {
        int result = 0;

        if (o == null) {
            return 1;
        }

        result = start.compareTo(o.getStart());

        if (result == 0) {
            result = Long.compare(duration, o.getDuration());
        }

        return result;
    }
}
