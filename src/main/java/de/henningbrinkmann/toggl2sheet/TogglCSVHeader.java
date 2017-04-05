package de.henningbrinkmann.toggl2sheet;

import java.util.stream.IntStream;

/**
 * Created by hso72 on 05.04.2017.
 */
public enum TogglCSVHeader {
    User("User"),
    Email("Email"),
    Client("Client"),
    Project("Project"),
    Task("Task"),
    Description("Description"),
    Billable("Billable"),
    StartDate("Start date"),
    StartTime("Start time"),
    EndDate("End date"),
    EndTime("End time"),
    Duration("Duration"),
    Tags("Tags"),
    Amount("Amount()");

    String header;

    TogglCSVHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    static public String[] getHeaders() {
        String[] result = new String[values().length];

        IntStream.range(0, values().length).forEach(i -> result[i] = values()[i].getHeader());

        return result;
    }
}
