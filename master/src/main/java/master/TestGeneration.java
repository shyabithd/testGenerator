package master;

import executionmode.Help;
import generator.ClientProcess;
import generator.Properties;
import generator.classpath.ClassPathHandler;
import generator.classpath.ResourceList;
import generator.result.TestGenerationResult;
import generator.rmi.service.ClientNodeRemote;
import generator.utils.LoggingUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rmi.MasterServices;
import utils.ExternalProcessHandler;
import utils.JarPathing;
import utils.JavaExecCmdUtil;

import java.io.File;
import java.rmi.RemoteException;
import java.util.*;

public class TestGeneration {

    private static Logger logger = LoggerFactory.getLogger(TestGeneration.class);

    public static Object executeTestGeneration(Options options, List<String> javaOpts, CommandLine line) throws Exception {

        Properties.Strategy strategy = getChosenStrategy(javaOpts, line);

        if (strategy == null) {
            strategy = Properties.Strategy.TESTSUITE;
        }

        List<List<TestGenerationResult>> results = new ArrayList<List<TestGenerationResult>>();

        if(line.getOptions().length == 0) {
            return results;
        }

        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
        if(cp==null || cp.isEmpty()){
            LoggingUtils.getGeneratorLogger().error("No classpath has been defined for the target project.\nOn the command line you can set it with the -projectCP option\n");
            return results;
        }


        if (line.hasOption("class")) {
            results.addAll(generateTests(strategy, line.getOptionValue("class"), javaOpts));
        } else {
            LoggingUtils.getGeneratorLogger().error(
                    "Please specify either target class ('-class' option), prefix ('-prefix' option), or " +
                            "classpath entry ('-target' option)\n");
            Help.execute(options);
        }
        return results;
    }

    private static boolean findTargetClass(String target) {

        if (ResourceList.getInstance(null).hasClass(target)) {
            return true;
        }

        LoggingUtils.getGeneratorLogger().info("* Unknown class: " + target +
                ". Be sure its full qualifying name  is correct and the classpath is properly set with '-projectCP'");

        return true;
    }

    private static void handleClassPath(List<String> cmdLine) {
        String classPath = ClassPathHandler.getInstance().getEvoSuiteClassPath();
        String projectCP = ClassPathHandler.getInstance().getTargetProjectClasspath();

        if (! classPath.isEmpty() && ! projectCP.isEmpty()) {
            classPath += File.pathSeparator;
        }

        if(! projectCP.isEmpty()) {
            classPath += projectCP;
        }

        cmdLine.add("-cp");
        //cmdLine.add(classPath);
        String pathingJar = JarPathing.createJarPathing(classPath);
        cmdLine.add(pathingJar);

        if (projectCP.isEmpty()) {
            projectCP =  classPath;
        }

        String projectCPFilePath = ClassPathHandler.writeClasspathToFile(projectCP);
        cmdLine.add("-DCP_file_path="+projectCPFilePath);
    }

    private static List<List<TestGenerationResult>> generateTests(Properties.Strategy strategy, String target,
                                                                  List<String> args) {

        LoggingUtils.getGeneratorLogger().info("* Going to generate test cases for class: "+target);

        if (!findTargetClass(target)) {
            return Arrays.asList(Arrays.asList(new TestGenerationResult[]{}));
        }

        List<String> cmdLine = new ArrayList<>();
        cmdLine.add(JavaExecCmdUtil.getJavaBinExecutablePath(true)/*EvoSuite.JAVA_CMD*/);

        handleClassPath(cmdLine);

        if(Properties.SPAWN_PROCESS_MANAGER_PORT != null){
            cmdLine.add("-Dspawn_process_manager_port="+Properties.SPAWN_PROCESS_MANAGER_PORT);
        }

        ExternalProcessHandler handler = new ExternalProcessHandler();
        int port = handler.openServer();
        if (port <= 0) {
            throw new RuntimeException("Not possible to start RMI service");
        }

        cmdLine.add("-Dprocess_communication_port=" + port);
        cmdLine.add("-Dinline=true");
        if(Properties.HEADLESS_MODE == true) {
            cmdLine.add("-Djava.awt.headless=true");
        }
        cmdLine.add("-Dlogback.configurationFile="+LoggingUtils.getLogbackFileName());
        cmdLine.add("-Dlog4j.configuration=SUT.log4j.properties");

        /*
         * FIXME: following 3 should be refactored, as not particularly clean.
         * First 2 does not work for master, as logback is read
         * before Properties is initialized
         */
        if(Properties.LOG_LEVEL!=null){
            cmdLine.add("-Dlog.level=" + Properties.LOG_LEVEL);
        }
        if(Properties.LOG_TARGET!=null){
            cmdLine.add("-Dlog.target=" + Properties.LOG_TARGET);
        }
        String logDir = System.getProperty("evosuite.log.folder");
        if(logDir!=null){
            // this parameter is for example used in logback-ctg.xml
            cmdLine.add(" -Devosuite.log.folder="+logDir);
        }
        //------------------------------------------------

        cmdLine.add("-Djava.library.path=lib");
        // cmdLine.add("-Dminimize_values=true");

        if (Properties.DEBUG) {
            // enabling debugging mode to e.g. connect the eclipse remote debugger to the given port
            cmdLine.add("-Ddebug=true");
            cmdLine.add("-Xdebug");
            cmdLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
                    + Properties.PORT);
            LoggingUtils.getGeneratorLogger().info("* Waiting for remote debugger to connect on port "
                    + Properties.PORT + "..."); // TODO find the right
            // place for this
        }

        if (!Properties.PROFILE.isEmpty()) {
            // enabling debugging mode to e.g. connect the eclipse remote debugger to the given port
            File agentFile = new File(Properties.PROFILE);
            if(!agentFile.exists()) {
                LoggingUtils.getGeneratorLogger().info("* Error: "+Properties.PROFILE+" not found");
            } else {
                cmdLine.add("-agentpath:" + Properties.PROFILE);
                LoggingUtils.getGeneratorLogger().info("* Using profiling agent " + Properties.PROFILE);
            }
        }


        if(Properties.JMC){
            //FIXME: does not seem to work, at least on Mac. Looks like some RMI conflict
            cmdLine.add("-XX:+UnlockCommercialFeatures");
            cmdLine.add("-XX:+FlightRecorder");
            cmdLine.add("-Dcom.sun.management.jmxremote");
            cmdLine.add("-Dcom.sun.management.jmxremote.autodiscovery");
            cmdLine.add("-Dcom.sun.management.jmxremote.authenticate=false");
            cmdLine.add("-Dcom.sun.management.jmxremote.ssl=false");
        }
        cmdLine.add("-XX:MaxJavaStackTraceDepth=1000000");
        cmdLine.add("-XX:+StartAttachListener");

        for (String arg : args) {
            if (!arg.startsWith("-DCP=")) {
                cmdLine.add(arg);
            }
        }

        switch (strategy) {
            case TESTSUITE:
                cmdLine.add("-Dstrategy=EvoSuite");
                break;
            case ONEBRANCH:
                cmdLine.add("-Dstrategy=OneBranch");
                break;
            case RANDOM:
                cmdLine.add("-Dstrategy=Random");
                break;
            case RANDOM_FIXED:
                cmdLine.add("-Dstrategy=Random_Fixed");
                break;
            case REGRESSION:
                cmdLine.add("-Dstrategy=Regression");
                break;
            case ENTBUG:
                cmdLine.add("-Dstrategy=EntBug");
                break;
            case MOSUITE:
                cmdLine.add("-Dstrategy=MOSuite");
                break;
            case DSE:
                cmdLine.add("-Dstrategy=Dynamic_Symbolic_Execution");
                break;
            case NOVELTY:
                cmdLine.add("-Dstrategy=Novelty");
                break;
            default:
                throw new RuntimeException("Unsupported strategy: " + strategy);
        }
        cmdLine.add("-DTARGET_CLASS=" + target);
        if (Properties.PROJECT_PREFIX != null) {
            cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);
        }

        cmdLine.add(ClientProcess.class.getName());

        /*
         * TODO: here we start the client with several properties that are set through -D. These properties are not visible to the master process (ie
         * this process), when we access the Properties file. At the moment, we only need few parameters, so we can hack them
         */
        Properties.getInstance();// should force the load, just to be sure
        Properties.TARGET_CLASS = target;
        Properties.PROCESS_COMMUNICATION_PORT = port;


        /*
         *  FIXME: refactor, and double-check if indeed correct
         *
         * The use of "assertions" in the client is pretty tricky, as those properties need to be transformed into JVM options before starting the
         * client. Furthermore, the properties in the property file might be overwritten from the commands coming from shell
         */

        String definedEAforClient = null;
        String definedEAforSUT = null;

        final String DISABLE_ASSERTIONS_EVO = "-da:"+"...";
        final String ENABLE_ASSERTIONS_EVO = "-ea:"+"...";
        final String DISABLE_ASSERTIONS_SUT = "-da:" + Properties.PROJECT_PREFIX + "...";
        final String ENABLE_ASSERTIONS_SUT = "-ea:" + Properties.PROJECT_PREFIX + "...";

        for (String s : cmdLine) {
            // first check client
            if (s.startsWith("-Denable_asserts_for_evosuite")) {
                if (s.endsWith("false")) {
                    definedEAforClient = DISABLE_ASSERTIONS_EVO;
                } else if (s.endsWith("true")) {
                    definedEAforClient = ENABLE_ASSERTIONS_EVO;
                }
            }
            // then check SUT
            if (s.startsWith("-Denable_asserts_for_sut")) {
                if (s.endsWith("false")) {
                    definedEAforSUT = DISABLE_ASSERTIONS_SUT;
                } else if (s.endsWith("true")) {
                    definedEAforSUT = ENABLE_ASSERTIONS_SUT;
                }
            }
        }

        /*
         * the assertions might not be defined in the command line, but they might be in the property file, or just use default values. NOTE: if those
         * are defined in the command line, then they overwrite whatever we had in the conf file
         */

        if (definedEAforSUT == null) {
            if (Properties.ENABLE_ASSERTS_FOR_SUT) {
                definedEAforSUT = ENABLE_ASSERTIONS_SUT;
            } else {
                definedEAforSUT = DISABLE_ASSERTIONS_SUT;
            }
        }

        if (definedEAforClient == null) {
            if (Properties.ENABLE_ASSERTS_FOR_EVOSUITE) {
                definedEAforClient = ENABLE_ASSERTIONS_EVO;
            } else {
                definedEAforClient = DISABLE_ASSERTIONS_EVO;
            }
        }

        /*
         * We add them in first position, after the java command To avoid confusion, we only add them if they are enabled. NOTE: this might have side
         * effects "if" in the future we have something like a generic "-ea"
         */
        if (definedEAforClient.equals(ENABLE_ASSERTIONS_EVO)) {
            cmdLine.add(1, definedEAforClient);
        }
        if (definedEAforSUT.equals(ENABLE_ASSERTIONS_SUT)) {
            cmdLine.add(1, definedEAforSUT);
        }

        LoggingUtils logUtils = new LoggingUtils();

        if (!Properties.CLIENT_ON_THREAD) {
            /*
             * We want to completely mute the SUT. So, we block all outputs from client, and use a remote logging
             */
            boolean logServerStarted = logUtils.startLogServer();
            if (!logServerStarted) {
                logger.error("Cannot start the log server");
                return null;
            }
            int logPort = logUtils.getLogServerPort(); //
            cmdLine.add(1, "-Dmaster_log_port=" + logPort);
            cmdLine.add(1, "-Devosuite.log.appender=CLIENT");
        }

        String[] newArgs = cmdLine.toArray(new String[cmdLine.size()]);

        for (String entry : ClassPathHandler.getInstance().getTargetProjectClasspath().split(File.pathSeparator)) {

        }

        handler.setBaseDir(TestSuite.base_dir_path);

        if (handler.startProcess(newArgs)) {

            Set<ClientNodeRemote> clients = null;
            try {
                //FIXME: timeout here should be handled by TimeController
                clients = MasterServices.getInstance().getMasterNode().getClientsOnceAllConnected(60000);
            } catch (InterruptedException e) {
            }
            if (clients == null) {
                logger.error("Not possible to access to clients. Clients' state: "+handler.getProcessState() +
                        ". Master registry port: "+MasterServices.getInstance().getRegistryPort());
            } else {
                /*
                 * The clients have started, and connected back to Master.
                 * So now we just need to tell them to start a search
                 */
                for (ClientNodeRemote client : clients) {
                    try {
                        client.startNewSearch();
                    } catch (RemoteException e) {
                        logger.error("Error in starting clients", e);
                    }
                }

                int time = 1;//TimeController.getInstance().calculateForHowLongClientWillRunInSeconds();
                handler.waitForResult(time * 1000);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            if (Properties.CLIENT_ON_THREAD) {
                handler.stopAndWaitForClientOnThread(10000);
            }

            handler.killProcess();
        } else {
            LoggingUtils.getGeneratorLogger().info("* Could not connect to client process");
        }

        boolean hasFailed = false;

        if (Properties.NEW_STATISTICS) {
            if(MasterServices.getInstance().getMasterNode() == null) {
                logger.error("Cannot write results as RMI master node is not running");
                hasFailed = true;
            } else {
                boolean written = false;//SearchStatistics.getInstance().writeStatistics();
                hasFailed = !written;
            }
        }

        /*
         * FIXME: it is unclear what is the relation between TestGenerationResult and writeStatistics()
         */
        List<List<TestGenerationResult>> results = null;//SearchStatistics.getInstance().getTestGenerationResults();
        //SearchStatistics.clearInstance();

        handler.closeServer();

        if (Properties.CLIENT_ON_THREAD) {
            handler.stopAndWaitForClientOnThread(10000);
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            logUtils.closeLogServer();
        }

        logger.debug("Master process has finished to wait for client");

        //FIXME: tmp hack till understood what TestGenerationResult is...
        if(hasFailed){
            logger.error("failed to write statistics data");
            //note: cannot throw exception because would require refactoring of many SystemTests
            return new ArrayList<List<TestGenerationResult>>();
        }

        return results;
    }

    private static Properties.Strategy getChosenStrategy(List<String> javaOpts, CommandLine line) {
        Properties.Strategy strategy = null;
        if (javaOpts.contains("-Dstrategy="+ Properties.Strategy.ENTBUG.name())
                && line.hasOption("generateTests")) {
            strategy = Properties.Strategy.ENTBUG;
            // TODO: Find a better way to integrate this
        } else if(javaOpts.contains("-Dstrategy="+ Properties.Strategy.NOVELTY.name())) {
            // TODO: Find a better way to integrate this
            strategy = Properties.Strategy.NOVELTY;
        } else if (line.hasOption("generateTests")) {
                strategy = Properties.Strategy.ONEBRANCH;
        } else if (line.hasOption("generateSuite")) {
            strategy = Properties.Strategy.TESTSUITE;
        } else if (line.hasOption("generateRandom")) {
            strategy = Properties.Strategy.RANDOM;
        } else if (line.hasOption("regressionSuite")) {
            strategy = Properties.Strategy.REGRESSION;
        } else if (line.hasOption("generateNumRandom")) {
            strategy = Properties.Strategy.RANDOM_FIXED;
            javaOpts.add("-Dnum_random_tests="
                    + line.getOptionValue("generateNumRandom"));
        } else if (line.hasOption("generateMOSuite")){
            strategy = Properties.Strategy.MOSUITE;
        } else if (line.hasOption("generateSuiteUsingDSE")) {
            strategy = Properties.Strategy.DSE;
        }
        return strategy;
    }
}
