package vn.tech;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Simplifies processing of command line features.
 */
public class CommonCommandLine {
    /** Command line options. */
    private Options options;

    /** Name of the program. */
    private String programName;

    /** Program version. */
    private String programVersion;

    /** Command line options. */
    private CommandLine line;

    private boolean displayHelp;

    /** Class to store parsed options. */
    class ParsedOption {
        public boolean required;
        public String value;
    }

    /** Actual options. */
    private final Map<String, ParsedOption> parsedOptions;

    private String[][] optionsArray;

    /**
     * Create command line.
     *
     * @param programName
     */
    public CommonCommandLine(String programName) {
        this.programName = programName;
        parsedOptions = new HashMap<>();
        programVersion = "0.0.0";
        displayHelp = true;
    }

    /**
     * Create command line.
     *
     * @param programName
     */
    public CommonCommandLine(String programName, String version) {
        this.programName = programName;
        parsedOptions = new HashMap<>();
        programVersion = version;
        displayHelp = true;

    }

    /**
     * Returns the set of CommandLine options.
     */
    public Options buildCmdLineOptions(String[][] optionsArray) {
        this.optionsArray = optionsArray;
        options = new Options();
        options.addOption("h", "help", false, "Print this message.");
        options.addOption("v", "version", false, "Program version " + programVersion);

        for (String[] oa : optionsArray) {
            options.addOption(oa[0], oa[1], Boolean.parseBoolean(oa[2]), oa[3]);
            ParsedOption option = new ParsedOption();
            option.required = Boolean.parseBoolean(oa[4]);
            parsedOptions.put(oa[1], option);
        }
        return options;
    }

    public String opt(int optID) {
        return getOption(optionsArray[optID][0]);
    }

    public boolean hasOpt(int optID) {
        return hasOption(optionsArray[optID][0]);
    }

    public String getOptionSloshed(int optID) {
        return FileUtils.ensureSloshed(opt(optID));
    }

    public int getOptionAsInt(int optID) {
        return Integer.parseInt(opt(optID));
    }

    public void printHelp() {
        if (this.isDisplayHelp() == false) {
            return;
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(programName, options);
    }

    /**
     * Returns the option result.
     *
     * @param arg
     * @return
     */
    public String getOption(String arg) {
        String option = line.getOptionValue(arg);
        return (option == null) ? "" : option;
    }

    /**
     * Returns whether an option exists or not
     *
     * @param option
     *            anOption
     * @return
     */
    public boolean hasOption(String option) {
        return line.hasOption(option);
    }

    /**
     * Build a command line with no validation.
     *
     * @param args
     * @param optionsArray
     * @return
     */
    public boolean setCommandLineNoCheck(String[] args, String[][] optionsArray) {
        CommandLineParser parser = new PosixParser();
        try {
            line = parser.parse(buildCmdLineOptions(optionsArray), args);
            if (line.hasOption("h")) {
                printHelp();
                return false;
            }
            if (line.hasOption("v")) {
                System.out.println(programVersion);
                return false;
            }
        } catch (ParseException ex) {
            System.out.println("Unexpected parse exception.");
            return false;
        }
        return true;
    }

    public boolean checkCommandLine(String[][] optionsArray) {
        for (String[] oa : optionsArray) {
            String cmd = oa[1];
            ParsedOption anOption = parsedOptions.get(cmd);
            anOption.value = getOption(oa[0]);
            if (anOption.required && ((anOption.value == null) || (anOption.value.length() == 0))) {
                printHelp();
                return false;
            }
        }
        return true;
    }

    /**
     * Pass the command line options in as well as main class to trigger a result.
     *
     * @param args
     * @param optionsArray
     * @return
     */
    public boolean setCommandLine(String[] args, String[][] optionsArray) {
        return setCommandLineNoCheck(args, optionsArray) ? checkCommandLine(optionsArray) : false;
    }

    public boolean isDisplayHelp() {
        return displayHelp;
    }

    public void setDisplayHelp(boolean displayHelp) {
        this.displayHelp = displayHelp;
    }
}