package de.henningbrinkmann.toggl2sheet;

import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class TimeSheetRecordService {
    private static final String BLANK = "     ";

    private Util util;

    @Autowired
    public TimeSheetRecordService(Util util) {
        this.util = util;
    }

    public ArrayList<String> getTSRStrings(TimeSheetRecord record, List<String> projects) {
        final ArrayList<String> strings = new ArrayList<>();

        strings.add(util.getDayFormatter().print(record.getStart()));

        if (record.getEnd() != null) {
            strings.add(util.getDayFormatter().print(record.getStart()));
            strings.add(util.getHourFormatter().print(record.getEnd()));
            strings.add(util.longToHourString(getPause(record)));
        } else {
            IntStream.range(0, 3).forEach(i -> strings.add(BLANK));
        }

        strings.add(util.longToHourString(record.getDuration()));

        Map<String, Long> durationByProject = record.getDurationByProject();
        projects.forEach(project -> {
            final Long duration = durationByProject.get(project);

            strings.add(duration == null ? BLANK : util.longToHourString(duration));
        });

        strings.add(String.join(", ", record.getDescription()));

        return strings;
    }

    private long getPause(TimeSheetRecord record) {

        if (record.getEnd() == null || record.getStart() == null) {
            return 0;
        }

        return record.getEnd().getMillis() - record.getStart().getMillis() - record.getDuration();
    }

    public String toHtml(TimeSheetRecord record, final List<String> projects) {
        final ArrayList<String> strings = getTSRStrings(record, projects);

        return strings.stream().collect(Collectors.joining("</td><td>", "<tr><td>", "</td></tr>"));
    }

    static String toHeadings(final List<String> projects) {
        final ArrayList<String> strings = getHeaderStrings(projects);

        return String.join("\t", strings);
    }

    private static ArrayList<String> getHeaderStrings(List<String> projects) {
        final ArrayList<String> strings = new ArrayList<>();

        strings.add("Datum   ");
        strings.add("Von  ");
        strings.add("Bis  ");
        strings.add("Pause");
        strings.add("Arbeit");

        projects.forEach(project -> strings.add(String.format("%5s", project)));

        strings.add("Tätigkeit");
        return strings;
    }

    String toHeadingsHtml(final List<String> projects) {
        final ArrayList<String> strings = getHeaderStrings(projects);

        return strings.stream().collect(Collectors.joining("</th><th>", "<tr><th>", "</th></tr>"));
    }

}
