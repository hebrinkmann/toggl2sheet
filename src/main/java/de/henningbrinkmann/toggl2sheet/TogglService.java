package de.henningbrinkmann.toggl2sheet;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

class TogglService {
    private long timeStep = 15 * 60 * 1000;
    private List<TogglRecord> togglRecords = new ArrayList<>();

    void read(Reader reader) throws IOException {
        TogglCSVParser parser = new TogglCSVParser();

        togglRecords = parser.parse(reader)
                .stream()
                .map(togglRecord -> togglRecord.trim(timeStep))
                .collect(Collectors.toList());
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

        return togglRecords.stream().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    List<TimeSheetRecord> getTimeSheetRecords() {
        return getRecordsByDay().entrySet()
                .stream()
                .map(entry -> new TimeSheetRecord(entry.getValue()))
                .sorted((a, b) -> a.getStart().compareTo(b.getStart()))
                .collect(Collectors.toList());
    }

}
