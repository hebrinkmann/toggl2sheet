package de.henningbrinkmann.toggl2sheet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

public class JodaDateTimeJsonSerializer extends JsonSerializer<DateTime> {
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:SSZ";

    @Override
    public void serialize(DateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String s = DateTimeFormat.forPattern(DATEFORMAT).print(dateTime);

        jsonGenerator.writeString(s);
    }
}
