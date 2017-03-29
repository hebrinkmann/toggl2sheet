package de.henningbrinkmann.toggl2sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Comparator;
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
        Config config = new Config(new String[]{"-i", "C:\\Users\\hso72\\Downloads\\Toggl_time_entries_2017-03-01_to_2017-03-31 (2).csv"});
        TogglService testee = new TogglService(config);

        testee.read(getInputStreamReader(config));
        Map<DateTime, List<TogglRecord>> result = testee.getRecordsByDay();

        String info = result.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(entry ->
                entry.getKey().toString() + "\n\t" + entry.getValue()
                        .stream()
                        .map(TogglRecord::toString)
                        .collect(Collectors.joining("\n\t"))
        ).collect(Collectors.joining("\n"));

        logger.info(info);
    }

    private Reader getInputStreamReader(Config config) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(config.getFile()));
    }

    @Test
    public void getTimeSheetRecords() throws IOException {
        TogglService testee = new TogglService(new Config.Builder().withClient("VET").build());

        testee.read(getInputStreamReader());
        List<TimeSheetRecord> result = testee.getTimeSheetRecords();

        Set<String> projects = testee.getProjects();

        String info = TimeSheetRecord.toHeadings(projects) + "\n" + result.stream()
                .map(timeSheetRecord -> timeSheetRecord.toTSV(projects))
                .collect(Collectors.joining("\n"));

        logger.info("\n" + info);
    }

    @Test
    public void getEffortsByWeekAndProject() throws IOException {
        TogglService testee = new TogglService(new Config.Builder().withClient("VET").build());

        testee.read(getInputStreamReader());
        logger.info(testee.getEffortsByWeekAndProject());
    }

    @Test
    public void getEffortsByDayAndDescription() throws IOException {
        TogglService testee = new TogglService(new Config.Builder().withClient("VET").build());

        testee.read(getInputStreamReader());
        logger.info(testee.getEffortsByDayAndDescription());
    }

    private InputStreamReader getInputStreamReader() {
        final InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("Toggl_time_entries_2017-02-01_to_2017-02-28 (1).csv");

        return new InputStreamReader(inputStream);
    }


}