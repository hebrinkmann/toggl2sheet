package de.henningbrinkmann.toggl2sheet;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    void read(Reader reader) throws IOException {
        TogglCSVParser parser = new TogglCSVParser();
        String client = config.getClient();

        togglRecords = parser.parse(reader)
                .stream()
                .filter(togglRecord -> client == null || client.equals(togglRecord.getClient()))
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

    Set<String> getProjects() {
        return togglRecords.stream().map(TogglRecord::getProject).collect(Collectors.toSet());
    }

    List<TimeSheetRecord> getTimeSheetRecords() {
        return getRecordsByDay().entrySet()
                .stream()
                .map(entry -> new TimeSheetRecord(entry.getValue()))
                .sorted((a, b) -> a.getStart().compareTo(b.getStart()))
                .collect(Collectors.toList());
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

        return byWeekAndProject.entrySet().stream().map(entry -> {
            final String collect = entry.getValue()
                    .entrySet()
                    .stream()
                    .map(entry1 -> "  " + entry1.getKey() + ":\t" + Util.longToHourString(entry1.getValue()))
                    .collect(joining("\n"));

            return "KW " + entry.getKey() + ":\n" + collect + "\n  Gesamt:\t" + Util.longToHourString(byWeek.get(entry.getKey()));

        }).collect(joining("\n"));
    }
}
