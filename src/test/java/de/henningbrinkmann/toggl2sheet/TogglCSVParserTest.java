package de.henningbrinkmann.toggl2sheet;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TogglCSVParserTest {
    private static final Logger logger = Logger.getLogger(TogglCSVParserTest.class);

    @Test
    public void parse() throws Exception {
        TogglCSVParser testee = new TogglCSVParser();

        final InputStreamReader reader = getInputStreamReader();

        final List<TogglRecord> togglRecords = testee.parse(reader);
        logger.info(togglRecords);
    }

    private InputStreamReader getInputStreamReader() {
        final InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("Toggl_time_entries_2017-02-01_to_2017-02-28 (1).csv");

        return new InputStreamReader(inputStream);
    }

}