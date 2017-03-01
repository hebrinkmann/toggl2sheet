package de.henningbrinkmann.toggl2sheet;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hso72 on 01.03.2017.
 */
public class TogglCSVParserTest {
    public static final Logger logger = Logger.getLogger(TogglCSVParserTest.class);

    @Test
    public void parse() throws Exception {
        TogglCSVParser testee = new TogglCSVParser();

        final InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("Toggl_time_entries_2017-02-01_to_2017-02-28 (1).csv");

        final InputStreamReader reader = new InputStreamReader(inputStream);

        final List<TogglRecord> togglRecords = testee.parse(reader);
        logger.info(togglRecords);
    }

}