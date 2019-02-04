package generator;

import generator.result.TestGenerationResult;
import generator.testsuite.TestSuiteChromosome;
import generator.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestSuiteGenerator {

    /**
     * Generate a test suite for the target class
     *
     * @return a {@link java.lang.String} object.
     */
    public TestGenerationResult generateTestSuite() {

//        TestSuiteChromosome testCases = generateTests();
//
//        postProcessTests(testCases);
//        ClientServices.getInstance().getClientNode().publishPermissionStatistics();
//        PermissionStatistics.getInstance().printStatistics(LoggingUtils.getEvoLogger());
//
//        // progressMonitor.setCurrentPhase("Writing JUnit test cases");
//        TestGenerationResult result = writeJUnitTestsAndCreateResult(testCases);
//        writeJUnitFailingTests();
//        TestCaseExecutor.pullDown();
//        /*
//         * TODO: when we will have several processes running in parallel, we ll
//         * need to handle the gathering of the statistics.
//         */
//        ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_STATISTICS);

        LoggingUtils.getEvoLogger().info("* Done!");
        LoggingUtils.getEvoLogger().info("");

        return null;
    }

    private static Logger logger = LoggerFactory.getLogger(TestSuiteGenerator.class);

    public static void main(String[] args) {
        TestSuiteGenerator generator = new TestSuiteGenerator();
        generator.generateTestSuite();
        System.exit(0);
    }
}
