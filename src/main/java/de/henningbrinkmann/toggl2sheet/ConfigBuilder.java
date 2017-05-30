package de.henningbrinkmann.toggl2sheet;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.io.File;
import java.util.List;

public class ConfigBuilder {
    private File file;
    private String client;
    private List<String> projects;
    private long timeStep = 15 * 60 * 1000;
    private DateTime startDate = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay();
    private DateTime endDate = startDate.plusMonths(1).minusDays(1);
    private boolean byProject = true;
    private String apiToken = "872d2e7bc6d1b11048148d53e5de3541";

    private final DateTimeParser[] dateTimeParser = {
            DateTimeFormat.forPattern("yyyy-MM-dd").getParser()
    };
    private final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().append(null, dateTimeParser)
            .toFormatter();

    public ConfigBuilder setFile(File file) {
        this.file = file;
        return this;
    }

    public ConfigBuilder setClient(String client) {
        this.client = client;
        return this;
    }

    public ConfigBuilder setProjects(List<String> projects) {
        this.projects = projects;
        return this;
    }

    public ConfigBuilder setTimeStep(long timeStep) {
        this.timeStep = timeStep;
        return this;
    }

    public ConfigBuilder setStartDate(DateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public ConfigBuilder setEndDate(DateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public ConfigBuilder setByProject(boolean byProject) {
        this.byProject = byProject;
        return this;
    }

    public ConfigBuilder setApiToken(String apiToken) {
        this.apiToken = apiToken;
        return this;
    }

    public Config createConfig() {
        return new Config(file, client, projects, timeStep, startDate, endDate, byProject, apiToken);
    }

    public ConfigBuilder setStartDate(String start) {
        if (start != null) {
            startDate = dateTimeFormatter.parseDateTime(start);
        }
        return this;
    }

    public ConfigBuilder setEndDate(String end) {
        if (end != null) {
            endDate = dateTimeFormatter.parseDateTime(end);
        }
        return this;
    }
}