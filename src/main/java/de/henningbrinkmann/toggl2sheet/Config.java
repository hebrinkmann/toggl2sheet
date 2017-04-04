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

@SuppressWarnings("AccessStaticViaInstance")
class Config {
    private File file;
    private String client;
    private List<String> projects;
    private long timeStep = 15 * 60 * 1000;

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
                }
            });
        }
    }

    private Config(Builder builder) {
        file = builder.file;
        client = builder.client;
        projects = builder.projects;
        timeStep = builder.timeStep;
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("input")
                .withDescription("input file path")
                .hasArg()
                .isRequired()
                .create('i'));
        options.addOption(OptionBuilder.withLongOpt("client").withDescription("client").hasArg().create('c'));
        options.addOption(OptionBuilder.withLongOpt("timeStep")
                .withDescription("time step in minutes")
                .hasArg()
                .withType(Number.class)
                .create('t'));
        options.addOption(OptionBuilder.withLongOpt("projects")
                .withDescription("projects")
                .hasOptionalArgs()
                .create('p'));

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

    @Override
    public String toString() {
        return "Config{" +
                "file=" + file +
                ", client='" + client + '\'' +
                ", projects=" + projects +
                ", timeStep=" + timeStep +
                '}';
    }

    @SuppressWarnings("WeakerAccess")
    public static final class Builder {
        private File file;
        private String client;
        private List<String> projects;
        private long timeStep = 15 * 60 * 1000;

        public Builder() {
        }

        public Builder(Config copy) {
            this.file = copy.file;
            this.client = copy.client;
            this.projects = copy.projects;
            this.timeStep = copy.timeStep;
        }

        public Builder withFile(File val) {
            file = val;
            return this;
        }

        public Builder withClient(String val) {
            client = val;
            return this;
        }

        public Builder withProjects(List<String> val) {
            projects = val;
            return this;
        }

        public Builder withTimeStep(long val) {
            timeStep = val;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}
