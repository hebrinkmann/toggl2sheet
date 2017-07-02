package de.henningbrinkmann.toggl2sheet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class Application {

    private static final String LF = System.getProperty("line.separator");

    private TogglService togglService;

    @RequestMapping(value = "/current", produces = MediaType.TEXT_HTML_VALUE)
    String home(@RequestParam(value = "start", required = false) String start,
                @RequestParam(value = "end", required = false) String end,
                @RequestParam(value = "byProject", required = false) Boolean byProject) {
        ConfigBuilder configBuilder = new ConfigBuilder().setStartDate(start).setEndDate(end).setByProject(byProject != null ? byProject : false);

        Config config = configBuilder.createConfig();

        TogglService togglService = new TogglService(config);

        List<String> projects = config.getProjects();
        if (projects == null) {
            projects = togglService.getProjects();
        }

        final List<String> finalProjects = projects;

        final Collection<TimeSheetRecord> timeSheetRecords = togglService.getDateTimeSheetRecordsByDateWithMissingDays();

        StringBuffer info = new StringBuffer("<html><body>");

        info.append("<table>");
        info.append(TimeSheetRecord.toHeadingsHtml(finalProjects) + LF);

        timeSheetRecords.forEach(timeSheetRecord -> info.append(timeSheetRecord.toHtml(finalProjects)).append(LF));

        info.append("</table>");

        long estimate = timeSheetRecords.stream().mapToLong(TimeSheetRecord::getDuration).sum();

        if (config.getStartDate() != null && config.getEndDate() != null) {
            info.append("Sollarbeitszeit: ").append(Util.longToHourString(Util.getSollarbeitszeit(config.getStartDate(), config.getEndDate()))).
                    append(LF);
        }

        info.append(togglService.getEfforts()).append(LF);

        info.append("Prognose: ").append(Util.longToHourString(estimate)).append(LF);

        info.append(togglService.getEffortsByWeekAndProject());

        info.append("</body></html>");

        return info.toString();
    }

    @RequestMapping("/timesheet")
    public TimeSheet getTimeSheetRecords(@RequestParam(value = "start", required = false) String start,
                                                     @RequestParam(value = "end", required = false) String end) {
        ConfigBuilder configBuilder = new ConfigBuilder().setStartDate(start).setEndDate(end);

        Config config = configBuilder.createConfig();

        TogglService togglService = new TogglService(config);

        final List<TimeSheetRecord> timeSheetRecords = togglService.getDateTimeSheetRecordsByDateWithMissingDays();


        List<String> projects = togglService.getProjects();

        return new TimeSheet(timeSheetRecords, projects);
    }

    public static void main(String args[]) throws IOException {
        SpringApplication.run(Application.class, args);
    }
}
