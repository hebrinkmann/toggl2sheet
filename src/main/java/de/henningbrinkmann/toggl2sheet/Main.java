package de.henningbrinkmann.toggl2sheet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by henning on 02.07.17.
 */
public class Main {
    public static void main(String[] args) {
        Config config = new Config(args);

        TogglService togglService = new TogglService(config);

        final List<String> projects = config.getProjects() != null ? config.getProjects() : togglService.getProjects();

        List<TimeSheetRecord> dateTimeSheetRecordsByDateWithMissingDays = togglService.getDateTimeSheetRecordsByDateWithMissingDays();

        String headers = TimeSheetRecord.toHeadings(projects);
        String timeSheetRecordsString = dateTimeSheetRecordsByDateWithMissingDays.stream()
                .map(timeSheetRecord -> {
                    return timeSheetRecord.toTSV(projects);
                }).collect(Collectors.joining("\n"));
        String efforts = togglService.getEffortsByWeekAndProject();

        System.out.println(headers + "\n" + timeSheetRecordsString + "\n" + efforts);
    }
}
