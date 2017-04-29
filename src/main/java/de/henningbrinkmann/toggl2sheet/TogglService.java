package de.henningbrinkmann.toggl2sheet;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.DateTime;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class TogglService {
    private List<TogglRecord> togglRecords = new ArrayList<>();
    private final Config config;

    TogglService(Config config) {
        this.config = config;
    }

    public void read(Reader reader) throws IOException {
        TogglCSVParser parser = new TogglCSVParser();
        String client = config.getClient();

        togglRecords = parser.parse(reader)
                .stream()
                .map(togglRecord -> togglRecord.trim(config.getTimeStep()))
                .collect(toList());
    }

    Map<DateTime, List<TogglRecord>> getRecordsByDay() {
        Function<TogglRecord, DateTime> keyMapper = TogglRecord::getStartDay;
        BinaryOperator<List<TogglRecord>> mergeFunction = (a, b) -> {
            a.addAll(b);
            return a;
        };
        Function<TogglRecord, List<TogglRecord>> valueMapper = togglRecord -> {
            ArrayList<TogglRecord> result = new ArrayList<>();
            result.add(togglRecord);

            return result;
        };

        return getTogglRecordStreamFiltered()
                .collect(toMap(keyMapper, valueMapper, mergeFunction));
    }

    private Stream<TogglRecord> getTogglRecordStreamFiltered() {
        return togglRecords.stream()
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

    List<String> getProjects() {
        ArrayList<String> result = new ArrayList<>();
        result.addAll(togglRecords.stream().map(TogglRecord::getProject).collect(Collectors.toSet()));

        return result;
    }

    List<TimeSheetRecord> getTimeSheetRecords() {
        return getRecordsByDay().entrySet()
                .stream()
                .map(entry -> new TimeSheetRecord(entry.getValue()))
                .sorted((a, b) -> a.getStart().compareTo(b.getStart()))
                .collect(Collectors.toList());
    }

    String getEfforts() {
        return "Ist-Leistung: " + Util.longToHourString(getTogglRecordStreamFiltered().mapToLong(TogglRecord::getDuration)
                .sum());
    }

    String getEffortsByWeekAndProject() {
        final Map<Integer, Map<String, Long>> byWeekAndProject = getTogglRecordStreamFiltered()
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
                    .map(entry1 -> "  " + entry1.getKey() + ":\t" + Util.longToHourString(entry1.getValue()))
                    .collect(joining("\n"));
            return "KW " + entry.getKey() + ":\n" + collect + "\n  Gesamt:\t" + Util.longToHourString(byWeek.get(entry.getKey()));
        }).collect(joining("\n"));
    }

    String getEffortsByDayAndDescription() {
        final Map<DateTime, Map<String, Long>> byDayAndDescription = getTogglRecordStreamFiltered().collect(groupingBy(
                TogglRecord::getStartDay,
                groupingBy(TogglRecord::getDescription, Collectors.summingLong(TogglRecord::getDuration))));

        return byDayAndDescription.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    final Stream<String> stringStream = entry.getValue()
                            .entrySet()
                            .stream()
                            .map(entry1 -> "  " + entry1.getKey() + ":\t" + Util.longToHourString(entry1.getValue()));

                    final String collect = stringStream.collect(joining("\n"));

                    return entry.getKey().toString() + "\n" + collect;
                })
                .collect(joining("\n"));
    }

    Map<DateTime, TimeSheetRecord> getTimeTimeSheetRecordsByDate() {
        Function<TimeSheetRecord, DateTime> keyFunction = timeSheetRecord -> timeSheetRecord.getStart()
                .withTimeAtStartOfDay();
        return getTimeSheetRecords().stream().collect(Collectors.toMap(keyFunction, Function.identity()));
    }

    Map<DateTime, TimeSheetRecord> getDateTimeTimeSheetRecordsByDateWithMissingDays(DateTime start, DateTime end) {
        final Map<DateTime, TimeSheetRecord> timeTimeSheetRecordsByDate = getTimeTimeSheetRecordsByDate();
        final Map<DateTime, TimeSheetRecord> result = new TreeMap<>();

        DateTime dateTime = start;

        if (dateTime == null) {
            dateTime = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay();
        }

        while (dateTime.isBefore(end)) {
            TimeSheetRecord timeSheetRecord = timeTimeSheetRecordsByDate.get(dateTime);

            if (timeSheetRecord == null) {
                timeSheetRecord = new TimeSheetRecord(dateTime);
            }

            result.put(dateTime, timeSheetRecord);

            dateTime = dateTime.plusDays(1);
        }

        return result;
    }
}
