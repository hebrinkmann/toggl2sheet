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

@SuppressWarnings("AccessStaticViaInstance")
class Config {
    private File file;
    private String client;
    private List<String> projects;
    private long timeStep = 15 * 60 * 1000;
    private DateTime startDate;
    private DateTime endDate;
    private boolean byProject = true;
    private String apiToken;

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
                    case "byProject":
                        this.byProject = Boolean.parseBoolean(option.getValue());
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

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("input")
                .withDescription("input file path")
                .hasArg()
                .isRequired()
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

    public boolean isByProject() {
        return byProject;
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
                ", byProject=" + byProject +
                ", apiToken='" + apiToken + '\'' +
                '}';
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }
}
