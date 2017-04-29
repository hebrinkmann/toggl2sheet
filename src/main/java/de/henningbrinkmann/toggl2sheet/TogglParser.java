package de.henningbrinkmann.toggl2sheet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by hbrinkmann on 23.03.2017.
 */
public class TogglParser {
    static private Logger logger = Logger.getLogger(TogglParser.class);
    private final CSVParser parser;
    private final List<TogglRecord> records;

    TogglParser(Reader in) throws IOException {

        parser = CSVFormat.DEFAULT.withHeader(de.henningbrinkmann.toggl2sheet.TogglCSVHeader.class).withSkipHeaderRecord().parse(in);
        List<CSVRecord> csvRecords = parser.getRecords();

        records = csvRecords.stream().map(record -> {
            TogglRecord togglRecord = new TogglRecord(record);

            logger.info(togglRecord);

            return togglRecord;
        }).collect(Collectors.toList());
    }

    Map<DateTime, List<TogglRecord>> getRecordsByDate() {
        return records.stream()
                .collect(Collectors.groupingBy(record -> record.getStart().withMillisOfDay(0)));
    }

    Map<DateTime, Map<String, List<TogglRecord>>> getRecordsByDayAndProject() {
        return records.stream()
                .collect(Collectors.groupingBy(record -> record.getStart()
                        .withMillisOfDay(0), Collectors.groupingBy(record -> record.getProject())));
    }

}
