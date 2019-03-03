package master;

import generator.Properties;
import generator.classpath.ClassPathHandler;
import generator.utils.LoggingUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandLineParameters {

	/**
	 * Validate all the "-" options set on the command line and all
	 * the already handled -D ones in Properties
	 * 
	 * @param line
	 */
	public static void validateInputOptionsAndParameters(CommandLine line) throws IllegalArgumentException{
		
		/*
		 * TODO: here there is lot more that could be added
		 */
		
		java.util.Properties properties = line.getOptionProperties("D");

		String cut = line.getOptionValue("class");
		
		if(cut!=null){
			if(cut.endsWith(".java")){
				throw new IllegalArgumentException("The target -class should be a JVM qualifying name (e.g., org.foo.SomeClass) and not a source file");
			}
			if(cut.endsWith(".class")){
				throw new IllegalArgumentException("The target -class should be a JVM qualifying name (e.g., org.foo.SomeClass) and not a bytecode file");
			}
		}


        String junitSuffix = properties.getProperty("junit_suffix");
        if(junitSuffix!=null && !junitSuffix.endsWith("Test")){
            throw new IllegalArgumentException("A JUnit suffix should always end with a 'Test'");
        }
	}
	
	
	/**
	 * Return all the available command line options that can be used with "-"
	 * 
	 * @return
	 */
	public static Options getCommandLineOptions() {
		Options options = new Options();

		Option targetClass = new Option("class", true, 
				"target class for test generation. A fully qualifying needs to be provided, e.g. org.foo.SomeClass");
		Option targetPrefix = new Option("prefix", true,
				"target package prefix for test generation. All classes on the classpath with the given package prefix " +
				"will be used, i.e. all classes in the given package and sub-packages.");
		Option targetCP = new Option("target", true,
				"target classpath for test generation. Either a jar file or a folder where to find the .class files");

		Option projectCP = new Option("projectCP", true,
				"classpath of the project under test and all its dependencies");		
		
		Option evosuiteCP = new Option("evosuiteCP", true,
				"classpath of EvoSuite jar file(s). This is needed when EvoSuite is called in plugins like Eclipse/Maven");
		
		Option junitPrefix = new Option("junit", true, "junit prefix");
		Option criterion = new Option("criterion", true,
				"target criterion for test generation. Can define more than one criterion by using a ':' separated list");
		Option seed = new Option("seed", true, "seed for random number generator");
		Option mem = new Option("mem", true,"heap size for client process (in megabytes)");
		Option libraryPath = new Option("libraryPath", true,"java library path to native libraries of the project under test");
		Option startedByCtg = new Option("startedByCtg",false, "Determine if current process was started by a CTG process");
		Option inheritance = new Option("inheritanceTree","Cache inheritance tree during setup");
		Option heapDump = new Option("heapdump", "Create heap dump on client VM out of memory error");
		Option base_dir = new Option("base_dir", true, "Working directory in which tests and reports will be placed");


		@SuppressWarnings("static-access")
		Option property = OptionBuilder.withArgName("property=value").hasArgs(2).withValueSeparator().withDescription("use value for given property").create("D");

		options.addOption(targetClass);
		options.addOption(targetPrefix);
		options.addOption(targetCP);
		options.addOption(junitPrefix);
		options.addOption(criterion);
		options.addOption(seed);
		options.addOption(mem);
		options.addOption(libraryPath);
		options.addOption(evosuiteCP);
		options.addOption(inheritance);
		options.addOption(base_dir);
		options.addOption(property);
		options.addOption(projectCP);
		options.addOption(heapDump);
		options.addOption(startedByCtg);

		return options;
	}
	
	public static void handleSeed(List<String> javaOpts, CommandLine line) throws NullPointerException{
		if (line.hasOption("seed")) {
			/*
			 * user can both use -seed and -Drandom_seed to set this variable
			 */
			String seedValue = line.getOptionValue("seed");
			javaOpts.add("-Drandom_seed=" + seedValue);
			Properties.RANDOM_SEED = Long.parseLong(seedValue);
		}
	}
	
	/**
	 * Add all the properties that were set with -D
	 * 
	 * @param javaOpts
	 * @param line
	 * @throws Error
	 */
	public static void addJavaDOptions(List<String> javaOpts, CommandLine line) throws Error {

        java.util.Properties properties = line.getOptionProperties("D");
		Set<String> propertyNames = new HashSet<>(Properties.getParameters());

        for (String propertyName : properties.stringPropertyNames()) {

            if (!propertyNames.contains(propertyName)) {
				LoggingUtils.getGeneratorLogger().error("* Unknown property: " + propertyName);
				throw new Error("Unknown property: " + propertyName);
			}

            String propertyValue = properties.getProperty(propertyName);
			javaOpts.add("-D" + propertyName + "=" + propertyValue);
			System.setProperty(propertyName, propertyValue);

            try {
				Properties.getInstance().setValue(propertyName, propertyValue);
			} catch (Exception e) {
				throw new Error("Invalid value for property " + propertyName+": "+propertyValue+". Exception "+e.getMessage(),e);
			}
		}
	}
	
	public static void handleClassPath(CommandLine line) {

		String DCP = null;
		java.util.Properties properties = line.getOptionProperties("D");
		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.equals("CP")) {
				DCP = properties.getProperty(propertyName);
			}
		}

		if(line.hasOption("projectCP") && DCP!=null){
			throw new IllegalArgumentException("Ambiguous classpath: both -projectCP and -DCP are defined");
		}

		String[] cpEntries = null;

		if (line.hasOption("projectCP")) {
			cpEntries = line.getOptionValue("projectCP").split(File.pathSeparator);
		} else if (DCP != null) {
			cpEntries = DCP.split(File.pathSeparator);
		}

		if(cpEntries != null){
			ClassPathHandler.getInstance().changeTargetClassPath(cpEntries);
		}

		if (line.hasOption("target")) {
			String target = line.getOptionValue("target");

			/*
			 * let's just add the target automatically to the classpath.
			 * This is useful for when we do not want to specify the classpath,
			 * and so just typing '-target' on command line
			 *
			 */
			ClassPathHandler.getInstance().addElementToTargetProjectClassPath(target);
		}

		if (line.hasOption("evosuiteCP")) {
			String entry = line.getOptionValue("evosuiteCP");
			String[] entries = entry.split(File.pathSeparator);
			ClassPathHandler.getInstance().setEvoSuiteClassPath(entries);
		}
	}
	
	public static void handleJVMOptions(List<String> javaOpts, CommandLine line) {
		/*
		 * NOTE: JVM arguments will not be passed over from the master to the client. So for -Xmx, we need to use "mem"
		 */
		if (line.hasOption("mem")) {
			javaOpts.add("-Xmx" + line.getOptionValue("mem") + "M");
		}
		if (line.hasOption("libraryPath")) {
			javaOpts.add("-Djava.library.path=" + line.getOptionValue("libraryPath"));
		}

		if (line.hasOption("heapdump")) {
			javaOpts.add("-XX:+HeapDumpOnOutOfMemoryError");
		}
	}
}
