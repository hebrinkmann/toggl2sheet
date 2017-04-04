package de.henningbrinkmann.toggl2sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

class TimeSheetRecord {

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

    String toTSV(final List<String> projects) {
        final ArrayList<String> strings = new ArrayList<>();

        strings.add(Util.dayFormatter.print(start));
        strings.add(Util.hourFormatter.print(start));
        strings.add(Util.hourFormatter.print(end));
        strings.add(Util.longToHourString(getPause()));
        strings.add(Util.longToHourString(duration));

        projects.forEach(project -> {
            final Long duration = durationByProject.get(project);

            strings.add(duration == null ? "     " : Util.longToHourString(duration));
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
}
