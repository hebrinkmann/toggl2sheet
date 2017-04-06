package de.henningbrinkmann.toggl2sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

public class Main {

    public static void main(String args[]) throws IOException {
        final Config config = new Config(args);

        TogglService togglService = new TogglService(config);

        final FileInputStream fis = new FileInputStream(config.getFile());
        final InputStreamReader inputStreamReader = new InputStreamReader(fis);
        togglService.read(inputStreamReader);

        List<String> projects = config.getProjects();
        if (projects == null) {
            projects = togglService.getProjects();
        }

        final List<String> finalProjects = projects;

        final List<TimeSheetRecord> timeSheetRecords = togglService.getTimeSheetRecords();

        StringBuffer info = new StringBuffer(TimeSheetRecord.toHeadings(finalProjects) + "\n");

        Map<DateTime, TimeSheetRecord> timeSheetRecordsByDay = timeSheetRecords.stream()
                .collect(Collectors.toMap(timeSheetRecord -> timeSheetRecord.getStart().withTimeAtStartOfDay(),
                        Function.identity()));
        List<DateTime> dateTimes = timeSheetRecordsByDay.keySet().stream().sorted().collect(Collectors.toList());

        if (dateTimes.size() > 0) {
            final DateTime startDate = config.getStartDate() != null ? config.getStartDate() : dateTimes.get(0);
            final DateTime endDate = (config.getEndDate() != null ? config.getEndDate() : dateTimes.get(dateTimes.size() - 1));

            DateTime date = startDate;
            long estimate = 0;

            do {
                TimeSheetRecord timeSheetRecord = timeSheetRecordsByDay.get(date);

                if (timeSheetRecord == null) {
                    timeSheetRecord = new TimeSheetRecord(date);
                }

                estimate += timeSheetRecord.getDuration();

                info.append(timeSheetRecord.toTSV(finalProjects)).append("\n");

                date = date.plusDays(1);
            } while (!endDate.isBefore(date));

            info.append("Sollarbeitszeit: ").append(Util.longToHourString(Util.getSollarbeitszeit(startDate, endDate))).
                    append("\n");
            info.append(togglService.getEfforts()).append("\n");

            info.append("Prognose: " + Util.longToHourString(estimate));
        }

        System.out.println(config);
        System.out.println(info);
        System.out.println(togglService.getEffortsByWeekAndProject());
    }
}
