package de.henningbrinkmann.toggl2sheet;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class Toggl2SheetRestController {
    private static final String LF = System.getProperty("line.separator");

    private TogglService togglService;
    private TimeSheetRecordService timeSheetRecordService;
    private Util util;

    @RequestMapping(value = "/current", produces = MediaType.TEXT_HTML_VALUE)
    public String home(@RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end,
        @RequestParam(value = "grouping", required = false) String grouping,
        @RequestHeader(value = "Toggle-API-Token", required = true) String apitoken) {
        ConfigBuilder configBuilder = new ConfigBuilder()
            .setApiToken(apitoken)
            .setStartDate(start)
            .setEndDate(end)
            .setGrouping(grouping != null ? Config.Grouping.valueOf(grouping) : Config.Grouping.NONE);

        Config config = configBuilder.createConfig();

        List<String> projects = config.getProjects();
        if (projects == null) {
            projects = togglService.getProjects(config);
        }

        final List<String> finalProjects = projects;

        final Collection<TimeSheetRecord> timeSheetRecords = togglService.getDateTimeSheetRecordsByDateWithMissingDays(config);

        String style = "<style>"
            + "tbody:nth-child(2n) { background-color: #eeeeee; } "
            + "</style>";
        StringBuilder info = new StringBuilder("<html><head>" + style + "</head><body>");

        info.append("<table>");
        info.append("<thead>");
        info.append(timeSheetRecordService.toHeadingsHtml(finalProjects)).append(LF);
        info.append("</thead>");

        Map<DateTime,List<TimeSheetRecord>> timeSheetRecordsByDay = timeSheetRecords.stream().collect(Collectors.groupingBy(t -> t.getStart().withTimeAtStartOfDay(), Collectors.toList()));

        timeSheetRecordsByDay.keySet().stream().sorted().forEach(date -> {
            info.append("<tbody>");
            timeSheetRecordsByDay.get(date).forEach(timeSheetRecord -> info.append(timeSheetRecordService.toHtml(timeSheetRecord, finalProjects)).append(LF));
            info.append("</tbody>").append(LF);
        });

        info.append("</table>");

        long estimate = timeSheetRecords.stream().mapToLong(TimeSheetRecord::getDuration).sum();

        info.append("<pre>");
        if (config.getStartDate() != null && config.getEndDate() != null) {
            info.append("Sollarbeitszeit: ").append(util.longToHourString(util.getSollarbeitszeit(config.getStartDate(), config.getEndDate()))).
                append(LF);
        }

        info.append(togglService.getEfforts(config)).append(LF);

        info.append("Prognose: ").append(util.longToHourString(estimate)).append(LF);

        info.append(togglService.getEffortsByWeekAndProject(config));

        info.append("</pre>");
        info.append("</body></html>");

        return info.toString();
    }

    @RequestMapping("/timesheet")
    public TimeSheet getTimeSheetRecords(@RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end,
        @RequestParam(value = "grouping", required = false) String grouping,
        @RequestHeader(value = "Toggle-API-Token", required = true) String apitoken) {
        ConfigBuilder configBuilder = new ConfigBuilder()
            .setApiToken(apitoken)
            .setStartDate(start)
            .setEndDate(end)
            .setGrouping(grouping != null ? Config.Grouping.valueOf(grouping) : Config.Grouping.NONE);

        Config config = configBuilder.createConfig();

        final List<TimeSheetRecord> timeSheetRecords = togglService.getDateTimeSheetRecordsByDateWithMissingDays(config);


        List<String> projects = togglService.getProjects(config);

        return new TimeSheet(timeSheetRecords, projects);
    }

    @RequestMapping("/clear")
    public void clear() {
        togglService.clear();
    }

    @Autowired
    public void setTogglService(TogglService togglService) {
        this.togglService = togglService;
    }

    @Autowired
    public void setTimeSheetRecordService(TimeSheetRecordService timeSheetRecordService) {
        this.timeSheetRecordService = timeSheetRecordService;
    }

    @Autowired
    public void setUtil(Util util) {
        this.util = util;
    }
}
