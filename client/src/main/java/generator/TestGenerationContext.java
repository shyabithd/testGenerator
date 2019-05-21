package generator;

import java.util.List;

import generator.coverage.branch.BranchPool;
import generator.coverage.mutation.MutationPool;
import generator.ga.archive.Archive;
import generator.ga.stoppingconditions.GlobalTimeStoppingCondition;
import generator.ga.stoppingconditions.MaxStatementsStoppingCondition;
import generator.instrumentations.LinePool;
import generator.setup.DependencyAnalysis;
import generator.setup.TestCluster;
import generator.setup.TestClusterGenerator;
import generator.testcase.ExecutionTracer;
import generator.testcase.TestCaseExecutor;
import generator.utils.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 *
 */
public class TestGenerationContext {

    private ClassReader classReader;

    private static final Logger logger = LoggerFactory.getLogger(TestGenerationContext.class);

    private static final TestGenerationContext singleton = new TestGenerationContext();

    /**
     * This is the classloader that does the instrumentation - it needs to be
     * used by all test code
     */

    /**
     * The classloader used to load this class
     */
    private ClassLoader originalClassLoader;

    /**
     * To avoid duplicate analyses we cache the cluster generator
     */
    private TestClusterGenerator testClusterGenerator;

    /**
     * Private singleton constructor
     */
    private TestGenerationContext() {
        originalClassLoader = this.getClass().getClassLoader();
    }

    public static TestGenerationContext getInstance() {
        return singleton;
    }

    /**
     * This is pretty important if the SUT use classloader of the running
     * thread. If we do not set this up, we will end up with cast exceptions.
     *
     * <p>
     * Note, an example in which this happens is in
     *
     * <p>
     * org.dom4j.bean.BeanAttribute
     *
     * <p>
     * in SF100 project 62_dom4j
     */
    public void goingToExecuteSUTCode() {

//        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public void doneWithExecutingSUTCode() {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    public TestClusterGenerator getTestClusterGenerator() {
        return testClusterGenerator;
    }

    public void setTestClusterGenerator(TestClusterGenerator generator) {
        testClusterGenerator = generator;
    }

    /**
     * @deprecated use {@code getInstance().getClassLoaderForSUT()}
     *
     * @return
     */
    public static ClassLoader getClassLoader() {
        return getInstance().getClass().getClassLoader();
    }

    public void resetContext() {
        logger.info("*** Resetting context");

        // A fresh context needs a fresh class loader to make sure we can
        // re-instrument classes

//        if (!DBManager.getInstance().isWasAccessed()) {
//            DBManager.getInstance().setSutClassLoader(classLoader);
//        }

        TestCaseExecutor.pullDown();

        ExecutionTracer.getExecutionTracer().clear();

//        // TODO: BranchPool should not be static
//        BranchPool.getInstance(classLoader).reset();
//        RemoveFinalClassAdapter.reset();
        LinePool.reset();
        MutationPool.clear();

//        // TODO: Clear only pool of current classloader?
//        GraphPool.clearAll();
//        DefUsePool.clear();
//
//        // TODO: This is not nice
//        for (ClassLoader cl : CFGMethodAdapter.methods.keySet())
//            CFGMethodAdapter.methods.get(cl).clear();
//
//        // TODO: Clear only pool of current classloader?
//        BytecodeInstructionPool.clearAll();

        // TODO: After this, the test cluster is empty until
        // DependencyAnalysis.analyse is called
        TestCluster.reset();
//        CastClassManager.getInstance().clear();
//        ConcreteClassAnalyzer.getInstance().clear();
        // This counts the current level of recursion during test generation

        MaxStatementsStoppingCondition.setNumExecutedStatements(0);
        GlobalTimeStoppingCondition.forceReset();
//        MutationTimeoutStoppingCondition.resetStatic();

        // Forget the old SUT
        Properties.resetTargetClass();

        TestCaseExecutor.initExecutor();

        Archive.getArchiveInstance().reset();

        // Constant pool
//        ConstantPoolManager.getInstance().reset();
//        ObjectPoolManager.getInstance().reset();
//        CarvingManager.getInstance().clear();

        // TODO: Why are we doing this?
        if (Properties.INSTRUMENT_CONTEXT || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.DEFUSE)
                || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.IBRANCH)) {
            // || ArrayUtil.contains(Properties.CRITERION,
            // Properties.Criterion.CBRANCH)) {
            try {
                testClusterGenerator = new TestClusterGenerator(
                        DependencyAnalysis.getInheritanceTree());
                testClusterGenerator.generateCluster(DependencyAnalysis.getCallGraph());
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }

//        if (Properties.CHECK_CONTRACTS) {
//            FailingTestSet.changeClassLoader(classLoader);
//        }
//        ContractChecker.setActive(true);
//
//        SystemInUtil.resetSingleton();
//        JOptionPaneInputs.resetSingleton();
//        Runtime.resetSingleton();
//        MethodCallReplacementCache.resetSingleton();
//
//        Injector.reset();
//
//        DSEStats.clear();
//
//        // keep the list of initialized classes (clear them when needed in
//        // the system test cases)
//        final List<String> initializedClasses = ClassReInitializer.getInstance().getInitializedClasses();
//        ClassReInitializer.resetSingleton();
//        ClassReInitializer.getInstance().addInitializedClasses(initializedClasses);
//
//        InspectorManager.resetSingleton();
//        ModifiedTargetStaticFields.resetSingleton();
    }

    public void setClassReader(ClassReader classReader) {
        this.classReader = classReader;
    }

    public ClassReader getClassReader() {
        return classReader;
    }
}