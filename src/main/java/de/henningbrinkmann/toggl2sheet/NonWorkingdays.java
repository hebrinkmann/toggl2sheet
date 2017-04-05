package de.henningbrinkmann.toggl2sheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Created by hso72 on 05.04.2017.
 */
public enum NonWorkingdays {
    INSTANCE;

    private final Logger logger = Logger.getLogger(NonWorkingdays.class);
    public final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd.MM.yyyy");
    private final HashMap<DateTime, String> days = new HashMap<>();

    NonWorkingdays() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("nonworking.csv");
        InputStreamReader reader = new InputStreamReader(is);

        try {
            read(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(Reader reader) throws IOException {
        final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withDelimiter(';').withSkipHeaderRecord().withHeader(
                "Date",
                "Description"));

        parser.getRecords().forEach(record -> {
            DateTime date = DateTime.parse(record.get("Date"), DATE_TIME_FORMATTER);
            String description = record.get("Description");

            days.put(date, description);
        });

        logger.info("Read days: " + days);
    }

    public String getNonWorkingDay(DateTime dateTime) {
        String result = days.get(dateTime.withTimeAtStartOfDay());

        if (result == null) {
            if (dateTime.getDayOfWeek() == 6 || dateTime.getDayOfWeek() == 7) {
                result = "Wochenende";
            }
        }

        return result;
    }
}
