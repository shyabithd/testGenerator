package runtime;

public class RuntimeSettings {

    /*
     * By default, all these properties should be false.
     *
     * WARNING If this behavior is changed, or any variable name is changed,
     * we HAVE TO update how JUnit code is generated
     */

    /**
     *  The full name of the class we are unit testing, ie the system under test (SUT)
     */
    public static String className = "unknown";

    /**
     * Shall the test cases use the mocking framework to remove non-determinism like
     * CPU clock?
     */
    public static boolean mockJVMNonDeterminism = false;

    /**
     * Should the use of System.in be mocked?
     */
    public static boolean mockSystemIn = false;

    
    /**
     * Should the use the GUI (javax.swing, etc.) be mocked?
     */
    public static boolean mockGUI = false;
    /**
     * Shall the test cases use a virtual file system?
     */
    public static boolean useVFS = false;

    /**
     * Shall the test cases use a virtual network?
     */
    public static boolean useVNET = false;


    /**
     * Shall we have support for Java Enterprise Edition?
     */
    public static boolean useJEE = false;

    /**
     * Should the static state be reset after each test execution?
     */
    public static boolean resetStaticState = false;

    /**
     * How many threads is each test allowed to start?
     * Note: such checks depend on RuntimeSettings#mockJVMNonDeterminism
     */
    public static int maxNumberOfThreads = 100;

    /**
     * How many iterations is each loop allowed to take?
     */
    public static long maxNumberOfIterationsPerLoop = 10_000;

    /**
     * Should tests be executed in a separate instrumenting class loader
     * or with the standard classloader and instrumentation via an agent?
     */
    public static boolean useSeparateClassLoader = true;

    /**
     * If the class is serializable, then doing any change (adding hashCode, static reset, etc)
     * will change the serialVersionUID if it is not defined in the class.
     * Hence, if it is not defined, we have to define it to
     * avoid problems in serialising the class, as reading Master will not do instrumentation.
     * The serialVersionUID HAS to be the same as the un-instrumented class.
     * However, this should not be done in the final JUnit, as it leads to a lot of issues
     * with classloaders
     */
    public static boolean applyUIDTransformation = false;


    /**
     * Used internally in EvoSuite when its regression suites are run
     */
    public static boolean isRunningASystemTest = false;

    public static boolean isUsingAnyMocking() {
        return mockJVMNonDeterminism || useVFS || useVNET || mockGUI;
    }

    public static void deactivateAllMocking() {
        mockJVMNonDeterminism = false;
        mockGUI = false;
        useVNET = false;
        useVFS = false;
        assert !isUsingAnyMocking();
    }

    public static void activateAllMocking() {
        mockJVMNonDeterminism = true;
        mockGUI = true;
        useVNET = true;
        useVFS = true;
        assert isUsingAnyMocking();
    }
}
