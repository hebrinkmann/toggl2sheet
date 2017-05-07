package de.henningbrinkmann.toggl2sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class Main {

    private static final String LF = System.getProperty("line.separator");

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

        final Collection<TimeSheetRecord> timeSheetRecords = togglService.getDateTimeSheetRecordsByDateWithMissingDays(
                config.getStartDate(),
                config.getEndDate());

        StringBuffer info = new StringBuffer(TimeSheetRecord.toHeadings(finalProjects) + LF);

        timeSheetRecords.forEach(timeSheetRecord -> info.append(timeSheetRecord.toTSV(finalProjects)).append(LF));

        long estimate = timeSheetRecords.stream().mapToLong(TimeSheetRecord::getDuration).sum();

        if (config.getStartDate() != null && config.getEndDate() != null) {
            info.append("Sollarbeitszeit: ").append(Util.longToHourString(Util.getSollarbeitszeit(config.getStartDate(), config.getEndDate()))).
                    append(LF);
        }

        info.append(togglService.getEfforts()).append(LF);

        info.append("Prognose: ").append(Util.longToHourString(estimate));

        System.out.println(config);
        System.out.println(info);
        System.out.println(togglService.getEffortsByWeekAndProject());
    }
}
