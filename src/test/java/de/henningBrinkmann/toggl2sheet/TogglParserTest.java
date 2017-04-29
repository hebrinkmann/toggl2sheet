package de.henningbrinkmann.toggl2sheet;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by hbrinkmann on 23.03.2017.
 */
public class TogglParserTest {
    static private Logger logger = Logger.getLogger(TogglParserTest.class);
    TogglParser testee;

    @Before
    public void before() throws Exception {
        InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("Toggl_time_entries_2017-03-01_to_2017-03-31.csv");
        Reader reader = new InputStreamReader(in);
        testee = new TogglParser(reader);
    }

    @Test
    public void getRecordsByDay() {
        Map<DateTime, List<TogglRecord>> recordsByDay = testee.getRecordsByDate();

        String result = mapToString(recordsByDay, TogglRecord::toString);

        logger.info(result);
    }

    @Test
    public void getRecordsByDayAndProject() {
        Map<DateTime, Map<String, List<TogglRecord>>> recordsByDayAndProject = testee.getRecordsByDayAndProject();

        logger.info(recordsByDayAndProject.entrySet()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n")));
    }

    private <K, V> String mapToString(Map<K, List<V>> map, Function<V, String> valueToString) {
        return map.entrySet()
                .stream()
                .map(entry -> "" + entry.getKey() + "\n" + entry.getValue()
                        .stream().map(v -> "  " + valueToString.apply(v)).collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n"));
    }

}