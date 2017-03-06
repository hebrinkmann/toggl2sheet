package de.henningbrinkmann.toggl2sheet;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

class Config {
    private File file;
    private String client;
    private Set<String> projects;

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
                    case "projects":
                        this.projects = Arrays.stream(option.getValues()).collect(Collectors.toSet());
                        break;
                }
            });
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        final Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        final Option client = new Option("c", "client", true, "client");
        client.setRequired(false);
        options.addOption(client);

        final Option projects = new Option("p", "projects", true, "projects");
        projects.setRequired(false);
        projects.setArgs(10);
        options.addOption(projects);

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

    Set<String> getProjects() {
        return projects;
    }
}
