package master;

import generator.Properties;
import generator.utils.LoggingUtils;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import generator.utils.Randomness;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSuite {

    private static Logger logger = LoggerFactory.getLogger(TestSuite.class);

    private static String separator = System.getProperty("file.separator");
    //private static String javaHome = System.getProperty("java.home");

    //public final static String JAVA_CMD = javaHome + separator + "bin" + separator + "java";

    public static String base_dir_path = System.getProperty("user.dir");

    public static String generateInheritanceTree(String cp) throws IOException {
        LoggingUtils.getGeneratorLogger().info("* Analyzing classpath (generating inheritance tree)");
        List<String> cpList = Arrays.asList(cp.split(File.pathSeparator));
        // Clear current inheritance file to make sure a new one is generated
        Properties.INHERITANCE_FILE = "";
        return "";
    }

    private void setupProperties() {
        if (base_dir_path.equals("")) {
            Properties.getInstanceSilent();
        } else {
            Properties.getInstanceSilent().loadProperties(base_dir_path
                            + separator
                            + Properties.PROPERTIES_FILE,
                    true);
        }
    }

    /**
     * <p>
     * parseCommandLine
     * </p>
     *
     * @param args an array of {@link String} objects.
     * @return a {@link Object} object.
     */
    public Object parseCommandLine(String[] args) {
        Options options = CommandLineParameters.getCommandLineOptions();

        List<String> javaOpts = new ArrayList<String>();

        String version = TestSuite.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "";
        }


        // create the parser
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            setupProperties();

            if (!line.hasOption("regressionSuite")) {
                if (line.hasOption("criterion")) {
                    //TODO should check if already defined
                    javaOpts.add("-Dcriterion=" + line.getOptionValue("criterion"));

                    //FIXME should really better handle the validation of javaOpts in the master, not client
                    try {
                        Properties.getInstance().setValue("criterion", line.getOptionValue("criterion"));
                    } catch (Exception e) {
                        throw new Error("Invalid value for criterion: "+e.getMessage());
                    }
                }
            } else {
                javaOpts.add("-Dcriterion=regression");
            }

			/*
			 * FIXME: every time in the Master we set a parameter with -D,
			 * we should check if it actually exists (ie detect typos)
			 */

            CommandLineParameters.handleSeed(javaOpts, line);

            CommandLineParameters.addJavaDOptions(javaOpts, line);

            CommandLineParameters.handleClassPath(line);

            CommandLineParameters.handleJVMOptions(javaOpts, line);


            if (line.hasOption("base_dir")) {
                base_dir_path = line.getOptionValue("base_dir");
                File baseDir = new File(base_dir_path);
                if (!baseDir.exists()) {
                    LoggingUtils.getGeneratorLogger().error("Base directory does not exist: "
                            + base_dir_path);
                    return null;
                }
                if (!baseDir.isDirectory()) {
                    LoggingUtils.getGeneratorLogger().error("Specified base directory is not a directory: "
                            + base_dir_path);
                    return null;
                }
            }

            CommandLineParameters.validateInputOptionsAndParameters(line);
            return TestGeneration.executeTestGeneration(options, javaOpts, line);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static boolean hasLegacyTargets() {
        File directory = new File(Properties.OUTPUT_DIR);
        if (!directory.exists()) {
            return false;
        }
        String[] extensions = {"task"};
        return !FileUtils.listFiles(directory, extensions, false).isEmpty();
    }

    /**
     * <p>
     * main
     * </p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(String[] args) {

        try {
            TestSuite evosuite = new TestSuite();
            evosuite.parseCommandLine(args);
        } catch (Throwable t) {
            logger.error("Fatal crash on main master.EvoSuite process. Class "
                    + Properties.TARGET_CLASS + " using seed " + Randomness.getSeed()
                    + ". Configuration id : " + Properties.CONFIGURATION_ID, t);
            System.exit(-1);
        }

		/*
		 * Some threads could still be running, so we need to kill the process explicitly
		 */
        System.exit(0);
    }

}
