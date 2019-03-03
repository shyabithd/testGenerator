package generator;

import generator.result.TestGenerationResult;
import generator.result.TestGenerationResultBuilder;
import generator.rmi.ClientServices;
import generator.utils.LoggingUtils;
import generator.utils.SpawnProcessKeepAliveChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import generator.utils.Randomness;
import runtime.RuntimeSettings;


/**
 * <p>
 * ClientProcess class.
 * </p>
 * 
 * @author Gordon Fraser
 * @author Andrea Arcuri
 */
public class ClientProcess {

	private static Logger logger = LoggerFactory.getLogger(ClientProcess.class);

	public static TestGenerationResult result;

	/**
	 * <p>
	 * run
	 * </p>
	 */
	public void run() {
		Properties.getInstance();
		setupRuntimeProperties();
		handleShadingSpecialCases();

		LoggingUtils.getGeneratorLogger().info("* Connecting to master process on port "
				+ Properties.PROCESS_COMMUNICATION_PORT);

		boolean registered = ClientServices.getInstance().registerServices();

		if (!registered) {
			result = TestGenerationResultBuilder.buildErrorResult("Could not connect to master process on port "
					+ Properties.PROCESS_COMMUNICATION_PORT);
			throw new RuntimeException("Could not connect to master process on port "
					+ Properties.PROCESS_COMMUNICATION_PORT);
		}

		if(Properties.SPAWN_PROCESS_MANAGER_PORT != null){
			SpawnProcessKeepAliveChecker.getInstance().registerToRemoteServerAndDieIfFails(
					Properties.SPAWN_PROCESS_MANAGER_PORT
			);
		}

		/*
		 * Now the client node is registered with RMI.
		 * The master will control this node directly.
		 */

		ClientServices.getInstance().getClientNode().waitUntilDone();
		ClientServices.getInstance().stopServices();
		SpawnProcessKeepAliveChecker.getInstance().unRegister();
	}

	private void initializeToolJar() {

		/*
		 * We load the agent although we "might" not use it.
		 * Reason is that when we compile the generated test cases to debug
		 * EvoSuite, those will/should use the agent.
		 * But for some arcane reason, the loading there fails.
		 * For example, there were/are issues with double-loading
		 * of libraries in different classloaders, eg the static
		 * initializer of BsdVirtualMachine does a  System.loadLibrary("attach"),
		 * and for some reason that is executed twice if the agent is loaded
		 * later in the search. Note: this does not affect the generated test
		 * cases when run from Eclipse (for example). 
		 */

        /*
        	TODO: tmp disabled to understand what the hack is happening on Jenkins.
        	however, it does not seem necessary any more, after quite a few refactoring/changes
        	in how agents are used (but don't know why...)
         */
		//AgentLoader.loadAgent();
	}

	private static void setupRuntimeProperties(){
		RuntimeSettings.useVFS = Properties.VIRTUAL_FS;
		RuntimeSettings.mockJVMNonDeterminism = Properties.REPLACE_CALLS;
		RuntimeSettings.mockSystemIn = Properties.REPLACE_SYSTEM_IN;
		RuntimeSettings.mockGUI = Properties.REPLACE_GUI;
        RuntimeSettings.maxNumberOfThreads = Properties.MAX_STARTED_THREADS;
        RuntimeSettings.maxNumberOfIterationsPerLoop = Properties.MAX_LOOP_ITERATIONS;
        RuntimeSettings.useVNET = Properties.VIRTUAL_NET;
        RuntimeSettings.useSeparateClassLoader = Properties.USE_SEPARATE_CLASSLOADER;
		RuntimeSettings.className = Properties.TARGET_CLASS;
		RuntimeSettings.useJEE = Properties.JEE;
		RuntimeSettings.applyUIDTransformation = true;
		RuntimeSettings.isRunningASystemTest = Properties.IS_RUNNING_A_SYSTEM_TEST;
    }


	private static void handleShadingSpecialCases(){

		String shadePrefix = "";

		String defaultFactory = System.getProperty("org.dom4j.factory", "org.dom4j.DocumentFactory");
		String defaultDomSingletonClass= System.getProperty(
				"org.dom4j.dom.DOMDocumentFactory.singleton.strategy", "org.dom4j.util.SimpleSingleton");
		String defaultSingletonClass = System.getProperty(
				"org.dom4j.DocumentFactory.singleton.strategy", "org.dom4j.util.SimpleSingleton");

		System.setProperty("org.dom4j.factory" , shadePrefix + defaultFactory);
		System.setProperty("org.dom4j.dom.DOMDocumentFactory.singleton.strategy" ,
				shadePrefix + defaultDomSingletonClass);
		System.setProperty("org.dom4j.DocumentFactory.singleton.strategy" ,
				shadePrefix + defaultSingletonClass);

		//restore in case SUT uses its own dom4j
		System.setProperty("org.dom4j.factory" ,defaultFactory);
		System.setProperty("org.dom4j.dom.DOMDocumentFactory.singleton.strategy",
				defaultDomSingletonClass);
		System.setProperty("org.dom4j.DocumentFactory.singleton.strategy",
				defaultSingletonClass);
	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link String} objects.
	 */
	public static void main(String[] args) {

		/*
		 * important to have it in a variable, otherwise 
		 * might be issues with following System.exit if successive
		 * threads change it if this thread is still running
		 */
		boolean onThread = Properties.CLIENT_ON_THREAD;

		try {
			LoggingUtils.getGeneratorLogger().info("* Starting client");
			ClientProcess process = new ClientProcess();
			TimeController.resetSingleton();
			process.run();
			if (!onThread) {
				/*
				 * If we we are in debug mode in which we run client on separated thread,
				 * then do not kill the JVM
				 */
				System.exit(0);
			}
		} catch (Throwable t) {
			logger.error("Error when generating tests for: " + Properties.TARGET_CLASS
					+ " with seed " + Randomness.getSeed()+". Configuration id : "+Properties.CONFIGURATION_ID, t);
			t.printStackTrace();

			//sleep 1 sec to be more sure that the above log is recorded
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			if (!onThread) {
				System.exit(1);
			}
		}
	}
}
