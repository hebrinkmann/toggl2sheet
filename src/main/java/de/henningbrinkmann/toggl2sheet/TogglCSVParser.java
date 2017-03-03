package de.henningbrinkmann.toggl2sheet;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVReader;

class TogglCSVParser {
    List<TogglRecord> parse(final Reader reader) throws IOException {
        final CSVReader csvReader = new CSVReader(reader);

        final List<String[]> lines = csvReader.readAll();

        return lines.stream().map(line -> {
            try {
                return new TogglRecord(line);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }).filter(tr -> tr != null).collect(Collectors.toList());
    }
}
