package generator;

import generator.classpath.ClassPathHandler;
import generator.classpath.ResourceList;
import generator.result.TestGenerationResult;
import generator.result.TestGenerationResultBuilder;
import generator.rmi.ClientServices;
import generator.rmi.service.ClientState;
import generator.setup.DependencyAnalysis;
import generator.strategy.TestGenerationStrategy;
import generator.testcase.DefaultTestCase;
import generator.testcase.ExecutionResult;
import generator.testcase.TestCase;
import generator.testcase.TestCaseExecutor;
import generator.testsuite.TestSuiteChromosome;
import generator.utils.LoggingUtils;
import org.bytedeco.javacpp.tools.Builder;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LoopCounter;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class TestSuiteGenerator {

    /**
     * Generate a test suite for the target class
     *
     * @return a {@link String} object.
     */
    public TestGenerationResult generateTestSuite() {

        LoggingUtils.getGeneratorLogger().info("* Analyzing classpath: ");

        ClientServices.getInstance().getClientNode().changeState(ClientState.INITIALIZATION);

        // Deactivate loop counter to make sure classes initialize properly
        LoopCounter.getInstance().setActive(false);

        //TestCaseExecutor.initExecutor();
        try {
            initializeTargetClass();
        } catch (Throwable e) {

            // If the bytecode for a method exceeds 64K, Java will complain
            // Very often this is due to mutation instrumentation, so this dirty
            // hack adds a fallback mode without mutation.
            // This currently breaks statistics and assertions, so we have to also set these properties
            boolean error = true;

            String message = e.getMessage();
            if (message != null && (message.contains("Method code too large") || message.contains("Class file too large"))) {
                LoggingUtils.getGeneratorLogger().info("* Instrumentation exceeds Java's 64K limit per method in target class");
                Properties.Criterion[] newCriteria = Arrays.stream(Properties.CRITERION).filter(t -> !t.equals(Properties.Criterion.STRONGMUTATION) && !t.equals(Properties.Criterion.WEAKMUTATION) && !t.equals(Properties.Criterion.MUTATION)).toArray(Properties.Criterion[]::new);
                if(newCriteria.length < Properties.CRITERION.length) {
                    LoggingUtils.getGeneratorLogger().info("* Attempting re-instrumentation without mutation");
                    Properties.CRITERION = newCriteria;
                    if(Properties.NEW_STATISTICS) {
                        LoggingUtils.getGeneratorLogger().info("* Deactivating EvoSuite statistics because of instrumentation problem");
                        Properties.NEW_STATISTICS = false;
                    }

                    try {
                        initializeTargetClass();
                        error = false;
                    } catch(Throwable t) {
                        // No-op, error handled below
                    }
                    if(Properties.ASSERTIONS && Properties.ASSERTION_STRATEGY == Properties.AssertionStrategy.MUTATION) {
                        LoggingUtils.getGeneratorLogger().info("* Deactivating assertion minimization because mutation instrumentation does not work");
                        Properties.ASSERTION_STRATEGY = Properties.AssertionStrategy.ALL;
                    }
                }
            }

            if(error) {
                LoggingUtils.getGeneratorLogger().error("* Error while initializing target class: "
                        + (e.getMessage() != null ? e.getMessage() : e.toString()));
                logger.error("Problem for " + Properties.TARGET_CLASS + ". Full stack:", e);
                return TestGenerationResultBuilder.buildErrorResult(e.getMessage() != null ? e.getMessage() : e.toString());
            }

        } finally {
            if (Properties.RESET_STATIC_FIELDS) {
                configureClassReInitializer();

            }
            // Once class loading is complete we can start checking loops
            // without risking to interfere with class initialisation
            LoopCounter.getInstance().setActive(true);
        }

        LoggingUtils.getGeneratorLogger().info("* Generating tests for class " + Properties.TARGET_CLASS);
        TestSuiteGeneratorHelper.printTestCriterion();

        if (!Properties.hasTargetClassBeenLoaded()) {
            // initialization failed, then build error message
            return TestGenerationResultBuilder.buildErrorResult("Could not load target class");
        }

        if (Properties.isRegression() && Properties.REGRESSION_SKIP_SIMILAR) {
            // Sanity checks
            if (Properties.getTargetClassRegression(true) == null) {
                Properties.IGNORE_MISSING_STATISTICS = false;
                logger.error("class {} was not on the regression projectCP", Properties.TARGET_CLASS);
                return TestGenerationResultBuilder.buildErrorResult("Could not load target regression class");
            }
            if (!ResourceList.getInstance(null).hasClass(Properties.TARGET_CLASS)) {
                Properties.IGNORE_MISSING_STATISTICS = false;
                logger.error("class {} was not on the regression_cp", Properties.TARGET_CLASS);
                return TestGenerationResultBuilder.buildErrorResult(
                        "Class " + Properties.TARGET_CLASS + " did not exist on regression classpath");

            }

            boolean areDifferent = false;//RegressionClassDiff.differentAcrossClassloaders(Properties.TARGET_CLASS);

            // If classes are different, no point in continuing.
            // TODO: report it to master to create a nice regression report
            if (!areDifferent) {
                Properties.IGNORE_MISSING_STATISTICS = false;
                logger.error("class {} was equal on both versions", Properties.TARGET_CLASS);
                return TestGenerationResultBuilder.buildErrorResult(
                        "Class " + Properties.TARGET_CLASS + " was not changed between the two versions");
            }
        }

        if (Properties.isRegression() && Properties.REGRESSION_SKIP_DIFFERENT_CFG) {
            // Does the class have the same CFG across the two versions of the program?
            boolean sameBranches = false;//RegressionClassDiff.sameCFG();

            if (!sameBranches) {
                Properties.IGNORE_MISSING_STATISTICS = false;
                logger.error("Could not match the branches across the two versions.");
                return TestGenerationResultBuilder.buildErrorResult("Could not match the branches across the two versions.");
            }
        }

        TestSuiteChromosome testCases = generateTests();

        postProcessTests(testCases);
        ClientServices.getInstance().getClientNode().publishPermissionStatistics();

        // progressMonitor.setCurrentPhase("Writing JUnit test cases");
        TestGenerationResult result = writeUnitTestsAndCreateResult(testCases);
        writeJUnitFailingTests();
        /*
         * TODO: when we will have several processes running in parallel, we ll
         * need to handle the gathering of the statistics.
         */
        ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_STATISTICS);

        LoggingUtils.getGeneratorLogger().info("* Done!");
        LoggingUtils.getGeneratorLogger().info("");

        return result;
    }

    private void configureClassReInitializer() {
    }

    private TestSuiteChromosome generateTests() {
        TestGenerationStrategy strategy = TestSuiteGeneratorHelper.getTestGenerationStrategy();
        TestSuiteChromosome testSuite = strategy.generateTests();

        //StatisticsSender.executedAndThenSendIndividualToMaster(testSuite);
        TestSuiteGeneratorHelper.getBytecodeStatistics();

        ClientServices.getInstance().getClientNode().publishPermissionStatistics();

        writeObjectPool(testSuite);
        return testSuite;

    }

    private void writeObjectPool(TestSuiteChromosome testSuite) {
    }

    private static TestGenerationResult writeUnitTestsAndCreateResult(TestSuiteChromosome testSuite) {
        return writeUnitTest(testSuite, Properties.JUNIT_SUFFIX);
    }

    private static TestGenerationResult writeUnitTest(TestSuiteChromosome testSuite, String junitSuffix) {
        List<TestCase> tests = testSuite.getTests();
        if (Properties.JUNIT_TESTS) {
            ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_TESTS);

            TestSuiteWriter suiteWriter = new TestSuiteWriter();
            suiteWriter.insertTests(tests);

            String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
            String testDir = ClassPathHandler.getInstance().getTargetProjectClasspath();

            LoggingUtils.getGeneratorLogger().info("* Writing JUnit test case '" + (name + junitSuffix) + "' to " + testDir);
            suiteWriter.writeTestSuite(junitSuffix, testDir);
        }
        return TestGenerationResultBuilder.buildSuccessResult();
    }

    private void writeJUnitFailingTests() {
    }

    private void postProcessTests(TestSuiteChromosome testCases) {
    }

    private static boolean hasThrownInitializerError(ExecutionResult execResult) {
        for (Throwable t : execResult.getAllThrownExceptions()) {
            if (t instanceof ExceptionInInitializerError) {
                return true;
            }
        }
        return false;
    }

    private static ExceptionInInitializerError getInitializerError(ExecutionResult execResult) {
        for (Throwable t : execResult.getAllThrownExceptions()) {
            if (t instanceof ExceptionInInitializerError) {
                ExceptionInInitializerError exceptionInInitializerError = (ExceptionInInitializerError)t;
                return exceptionInInitializerError;
            }
        }
        return null;
    }

    private static void writeJUnitTestSuiteForFailedInitialization() {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test = buildLoadTargetClassTestCase(Properties.TARGET_CLASS);
        suite.addTest(test);
        writeUnitTestsAndCreateResult(suite);
    }

    private static DefaultTestCase buildLoadTargetClassTestCase(String className) {
        DefaultTestCase test = new DefaultTestCase();
        return test;
    }

    private void initializeTargetClass() throws Throwable {
        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
        ClassReader classReader = new ClassReader();
        try {
            classReader.readFile(cp+"/"+Properties.TARGET_CLASS);
            classReader.parseTree();
            TestGenerationContext.getInstance().setClassReader(classReader);
            Properties.setTargetClass(classReader);
        } catch (CoreException e) {
            throw e;
        }
        // Here is where the <clinit> code should be invoked for the first time
        DefaultTestCase test = buildLoadTargetClassTestCase(Properties.TARGET_CLASS);
        ExecutionResult execResult = TestCaseExecutor.getInstance().execute(test, Integer.MAX_VALUE);

        if (hasThrownInitializerError(execResult)) {
            // create single test suite with Class.forName()
            writeJUnitTestSuiteForFailedInitialization();
            ExceptionInInitializerError ex = getInitializerError(execResult);
            throw ex;
        } else if (!execResult.getAllThrownExceptions().isEmpty()) {
            // some other exception has been thrown during initialization
            Throwable t = execResult.getAllThrownExceptions().iterator().next();
            throw t;
        }

        DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS, Arrays.asList(cp.split(File.pathSeparator)));
        LoggingUtils.getGeneratorLogger().info("* Finished analyzing classpath");
    }

    private static Logger logger = LoggerFactory.getLogger(TestSuiteGenerator.class);

    public static void main(String[] args) {
        TestSuiteGenerator generator = new TestSuiteGenerator();
        generator.generateTestSuite();
        System.exit(0);
    }
}
