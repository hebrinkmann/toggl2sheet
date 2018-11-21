package de.henningbrinkmann.toggl2sheet;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.rocketbase.toggl.api.GetDetailed;
import io.rocketbase.toggl.api.TogglReportApi;
import io.rocketbase.toggl.api.TogglReportApiBuilder;
import io.rocketbase.toggl.api.model.DetailedResult;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Component
class TogglService {
    private static final Logger logger = Logger.getLogger(TogglService.class);
    private List<TogglRecord> togglRecords;

    private Util util;

    private NonWorkingdays nonWorkingdays;

    private void read(Reader reader, Config config) throws IOException {
        TogglCSVParser parser = new TogglCSVParser();

        togglRecords = parser.parse(reader)
                .stream()
                .map(togglRecord -> togglRecord.trim(config.getTimeStep()))
                .collect(toList());
    }

    private void readFromApi(Config config) {
        TogglReportApi togglReportApi = new TogglReportApiBuilder().apiToken(config.getApiToken())
                .userAgent("toggl2sheet")
                .workspaceId(1397713)
                .build();

        GetDetailed detailed = togglReportApi.detailed()
                .since(config.getStartDate().toDate())
                .until(config.getEndDate().toDate());


        togglRecords = new ArrayList<>();

        int page = 1;
        while (true) {
            DetailedResult detailedResult = detailed.page(page).get();
            logger.info(String.format("Reading from Toggl-API: %d/%d",
                    togglRecords.size(),
                    detailedResult.getTotalCount()));
            togglRecords.addAll(detailedResult.getData()
                    .stream()
                    .map(TogglRecord::new)
                    .map(togglRecord -> togglRecord.trim(config.getTimeStep()))
                    .collect(toList()));
            page = page + 1;

            if (togglRecords.size() >= detailedResult.getTotalCount()) {
                break;
            }
        }
    }

    private Stream<TogglRecord> getTogglRecordStreamFiltered(Config config) {
        return getTogglRecords(config).stream()
                .filter(togglRecord -> {
                    if (config.getClient() != null && !togglRecord.getClient().equals(config.getClient())) {
                        return false;
                    }

                    //noinspection RedundantIfStatement
                    if (config.getProjects() != null && !config.getProjects().contains(togglRecord.getProject())) {
                        return false;
                    }

                    return true;
                });
    }

    List<String> getProjects(Config config) {
        return getTogglRecords(config).stream().map(TogglRecord::getProject).distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    private List<TogglRecord> getTogglRecords(Config config) {
        if (togglRecords == null) {
            if (config.getApiToken() != null) {
                readFromApi(config);
            } else {
                try {
                    FileReader fileReader = new FileReader(config.getFile());
                    read(fileReader, config);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return togglRecords;
    }

    String getEfforts(Config config) {
        return "Ist-Leistung: " + util.longToHourString(getTogglRecordStreamFiltered(config).mapToLong(TogglRecord::getDuration)
                .sum());
    }

    String getEffortsByWeekAndProject(Config config) {
        final Map<Integer, Map<String, Long>> byWeekAndProject = getTogglRecordStreamFiltered(config)
                .collect(groupingBy(record -> record.getStart().getWeekOfWeekyear(),
                        groupingBy(TogglRecord::getProject, Collectors.summingLong(TogglRecord::getDuration))));


        final Map<Integer, Long> byWeek = byWeekAndProject.entrySet()
                .stream()
                .collect(groupingBy(Map.Entry::getKey,
                        Collectors.summingLong(entry1 -> entry1.getValue()
                                .values()
                                .stream()
                                .mapToLong(l -> l)
                                .sum())));

        return byWeekAndProject.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(entry -> {
            final String collect = entry.getValue()
                    .entrySet()
                    .stream()
                    .map(entry1 -> "  " + entry1.getKey() + ":\t" + util.longToHourString(entry1.getValue()))
                    .collect(joining("\n"));
            return "KW " + entry.getKey() + ":\n" + collect + "\n  Gesamt:\t" + util.longToHourString(byWeek.get(entry.getKey()));
        }).collect(joining("\n"));
    }

    List<TimeSheetRecord> getDateTimeSheetRecordsByDateWithMissingDays(Config config) {
        final Map<DateTime, List<TimeSheetRecord>> timeSheetRecordsByDate = getTimeSheetRecordsByDate(config).stream()
                .collect(groupingBy(t -> t.getStart().withTimeAtStartOfDay()));
        final List<TimeSheetRecord> result = new ArrayList<>();
        DateTime dateTime = config.getStartDate();
        if (dateTime == null) {
            dateTime = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay();
        }
        while (!config.getEndDate().isBefore(dateTime)) {
            List<TimeSheetRecord> timeSheetRecords = timeSheetRecordsByDate.get(dateTime);
            if (timeSheetRecords == null) {
                result.add(new TimeSheetRecord(dateTime, nonWorkingdays.getNonWorkingDay(dateTime)));
            } else {
                result.addAll(timeSheetRecords);
            }

            dateTime = dateTime.plusDays(1);
        }

        return result;
    }

    private List<TimeSheetRecord> getTimeSheetRecordsByDate(Config config) {
        Map<DateTime, Map<String, List<TogglRecord>>> collect = getTogglRecordStreamFiltered(config).collect(groupingBy(togglRecord -> togglRecord
                .getStart()
                .withTimeAtStartOfDay(), groupingBy(t -> {
            switch (config.getGrouping()) {
                case PROJECT:
                    return t.getProject();
                case CUSTOMER:
                    return t.getClient();
                case TITLE:
                    return t.getDescription();
                case SINGLE:
                    return t.toString();
                default:
                    return "";
            }
        })));
        return collect.entrySet().stream().flatMap(e -> e.getValue().entrySet().stream())
                .map(e1 -> new TimeSheetRecord(e1.getValue()))
                .sorted()
                .collect(toList());
    }

    void clear() {
        togglRecords = null;
    }

    @Autowired
    public void setUtil(Util util) {
        this.util = util;
    }

    @Autowired
    public void setNonWorkingdays(NonWorkingdays nonWorkingdays) {
        this.nonWorkingdays = nonWorkingdays;
    }
}
