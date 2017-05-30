package de.henningbrinkmann.toggl2sheet;

import org.joda.time.DateTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class Main {

    private static final String LF = System.getProperty("line.separator");

    private TogglService togglService;

    @RequestMapping("/current")
    String home(@RequestParam(value = "start", required = false) String start,
                @RequestParam(value = "end", required = false) String end) {
        ConfigBuilder configBuilder = new ConfigBuilder().setStartDate(start).setEndDate(end);

        Config config = configBuilder.createConfig();

        TogglService togglService = new TogglService(config);

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

        info.append(togglService.getEffortsByWeekAndProject());

        return info.toString();
    }

    public static void main(String args[]) throws IOException {
        SpringApplication.run(Main.class, args);
    }
}
