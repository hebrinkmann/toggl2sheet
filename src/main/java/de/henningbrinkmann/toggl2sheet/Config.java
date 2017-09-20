package de.henningbrinkmann.toggl2sheet;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.joda.time.DateTime;
import org.springframework.http.HttpRequest;

@SuppressWarnings("AccessStaticViaInstance")
class Config {
    private File file;
    private String client;
    private List<String> projects;
    private long timeStep = 15 * 60 * 1000;
    private DateTime startDate = DateTime.now().withTimeAtStartOfDay().withDayOfMonth(1);
    private DateTime endDate = startDate.plusMonths(1).minusDays(1);
    private Grouping grouping = Grouping.CUSTOMER;
    private String apiToken;

    public enum Grouping { PROJECT, TITLE, CUSTOMER, NONE };

    Config(String[] args) {
        final CommandLine commandLine = getCommandLine(args);
        if (commandLine != null) {
            Arrays.stream(commandLine.getOptions()).forEach(option -> {
                switch (option.getLongOpt()) {
                    case "input":
                        this.file = new File(option.getValue());
                        break;
                    case "client":
                        this.client = option.getValue();
                        break;
                    case "projects":
                        this.projects = Arrays.stream(option.getValues()).collect(Collectors.toList());
                        break;
                    case "timeStep":
                        this.timeStep = Long.parseLong(option.getValue()) * 60L * 1000L;
                        break;
                    case "startDate":
                        this.startDate = DateTime.parse(option.getValue());
                        break;
                    case "endDate":
                        this.endDate = DateTime.parse(option.getValue());
                        break;
                    case "grouping":
                        this.grouping = Grouping.valueOf(option.getValue());
                        break;
                    case "apiToken":
                        this.apiToken = option.getValue();
                        break;
                }
            });
        }
    }

    public Config() {

    }

    public Config(File file, String client, List<String> projects, long timeStep, DateTime startDate, DateTime endDate, Grouping grouping, String apiToken) {
        this.file = file;
        this.client = client;
        this.projects = projects;
        this.timeStep = timeStep;
        this.startDate = startDate;
        this.endDate = endDate;
        this.grouping = grouping;
        this.apiToken = apiToken;
    }

    public Config(HttpRequest request) {
        request.getURI();
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("input")
                .withDescription("input file path")
                .hasArg()
                .create('i'));
        options.addOption(OptionBuilder.withLongOpt("client")
                .withDescription("client")
                .hasArg()
                .create('c'));
        options.addOption(OptionBuilder.withLongOpt("timeStep")
                .withDescription("time step in minutes")
                .hasArg()
                .withType(Number.class)
                .create('t'));
        options.addOption(OptionBuilder.withLongOpt("projects")
                .withDescription("projects")
                .hasOptionalArgs()
                .create('p'));
        options.addOption(OptionBuilder.withLongOpt("startDate")
                .withDescription("start date")
                .hasArg()
                .create('s'));
        options.addOption(OptionBuilder.withLongOpt("endDate")
                .withDescription("end date")
                .hasArg()
                .create('e'));
        options.addOption(OptionBuilder.withLongOpt("byProject")
                .withDescription("one timesheet entry per project")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("apiToken")
                .withDescription("API token for Toggl")
                .hasArg()
                .create());


        return options;
    }

    private static CommandLine getCommandLine(String args[]) {
        CommandLineParser parser = new GnuParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        final Options options = getOptions();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());

            helpFormatter.printHelp("toggl2sheet", options);

            System.exit(1);
        }

        return null;
    }

    String getClient() {
        return client;
    }

    File getFile() {
        return file;
    }

    List<String> getProjects() {
        return projects;
    }

    long getTimeStep() {
        return timeStep;
    }

    public String getApiToken() {
        return apiToken;
    }

    @Override
    public String toString() {
        return "Config{" +
                "file=" + file +
                ", client='" + client + '\'' +
                ", projects=" + projects +
                ", timeStep=" + timeStep +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", grouping=" + grouping +
                ", apiToken='" + apiToken + '\'' +
                '}';
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public Grouping getGrouping() {
        return grouping;
    }
}
