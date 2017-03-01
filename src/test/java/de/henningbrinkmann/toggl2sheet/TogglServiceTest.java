package de.henningbrinkmann.toggl2sheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Test;

public class TogglServiceTest {
    private static final Logger logger = Logger.getLogger(TogglServiceTest.class);

    @Test
    public void getRecordsByDay() throws Exception {
        TogglService testee = new TogglService(null);

        testee.read(getInputStreamReader());
        Map<DateTime, List<TogglRecord>> result = testee.getRecordsByDay();

        String info = result.entrySet().stream().map(entry ->
                entry.getKey().toString() + "\n\t" + entry.getValue()
                        .stream()
                        .map(TogglRecord::toString)
                        .collect(Collectors.joining("\n\t"))
        ).collect(Collectors.joining("\n"));

        logger.info(info);
    }

    @Test
    public void getTimeSheetRecords() throws IOException {
        TogglService testee = new TogglService("VET");

        testee.read(getInputStreamReader());
        List<TimeSheetRecord> result = testee.getTimeSheetRecords();

        Set<String> projects = testee.getProjects();

        String info = TimeSheetRecord.toHeadings(projects) + "\n" + result.stream()
                .map(timeSheetRecord -> timeSheetRecord.toTSV(projects))
                .collect(Collectors.joining("\n"));

        logger.info("\n" + info);
    }

    private InputStreamReader getInputStreamReader() {
        final InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("Toggl_time_entries_2017-02-01_to_2017-02-28 (1).csv");

        return new InputStreamReader(inputStream);
    }

}