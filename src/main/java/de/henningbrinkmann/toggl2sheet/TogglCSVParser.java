package de.henningbrinkmann.toggl2sheet;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

class TogglCSVParser {
    List<TogglRecord> parse(final Reader reader) throws IOException {
        final CSVParser parser = CSVFormat.DEFAULT.withSkipHeaderRecord()
                .withHeader(TogglCSVHeader.getHeaders())
                .parse(reader);

        return parser.getRecords()
                .stream()
                .map(record -> new TogglRecord(record))
                .filter(tr -> tr != null)
                .collect(Collectors.toList());
    }
}
