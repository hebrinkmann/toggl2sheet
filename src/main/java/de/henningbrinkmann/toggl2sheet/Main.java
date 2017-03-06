package de.henningbrinkmann.toggl2sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.cli.*;

public class Main {

    public static void main(String args[]) throws IOException {
        final Config config = new Config(args);

        TogglService togglService = new TogglService(config.getClient());

        final FileInputStream fis = new FileInputStream(config.getFile());
        final InputStreamReader inputStreamReader = new InputStreamReader(fis);
        togglService.read(inputStreamReader);

        Set<String> projects = config.getProjects();
        if (projects == null) {
            projects = togglService.getProjects();
        }

        final Set<String> finalProjects = projects;

        final List<TimeSheetRecord> timeSheetRecords = togglService.getTimeSheetRecords();

        String info = TimeSheetRecord.toHeadings(finalProjects) + "\n" + timeSheetRecords.stream()
                .map(timeSheetRecord -> timeSheetRecord.toTSV(finalProjects))
                .collect(Collectors.joining("\n"));

        System.out.println(info);
        System.out.println(togglService.getEffortByWeekAndProject());
    }
}
