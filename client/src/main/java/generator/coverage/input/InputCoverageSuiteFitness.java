package generator.coverage.input;

import generator.ga.archive.Archive;
import generator.Properties;
import generator.testcase.*;
import generator.testsuite.AbstractTestSuiteChromosome;
import generator.testsuite.TestSuiteFitnessFunction;

import java.util.*;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = -6571466037158036014L;

    private final int totalGoals;
    private final Set<InputCoverageTestFitness> inputCoverageMap = new LinkedHashSet<>();

    private Set<InputCoverageTestFitness> toRemoveGoals = new LinkedHashSet<>();
    private Set<InputCoverageTestFitness> removedGoals  = new LinkedHashSet<>();

    // Some stuff for debug output
    private int maxCoveredGoals = 0;
    private double bestFitness = Double.MAX_VALUE;

    public InputCoverageSuiteFitness() {
        // Add observer
        TestCaseExecutor executor = TestCaseExecutor.getInstance();
        InputObserver observer = new InputObserver();
        executor.addObserver(observer);
        //TODO: where to remove observer?: executor.removeObserver(observer);

        determineCoverageGoals();

        totalGoals = inputCoverageMap.size();
    }

    /**
     * Initialize the set of known coverage goals
     */
    private void determineCoverageGoals() {
        List<InputCoverageTestFitness> goals = new InputCoverageFactory().getCoverageGoals();
        for (InputCoverageTestFitness goal : goals) {
            inputCoverageMap.add(goal);
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);

        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Execute all tests and count covered input goals
     */
    @Override
    public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
        logger.trace("Calculating test suite fitness");
        double fitness = 0.0;

        List<ExecutionResult> results = runTestSuite(suite);

        boolean hasTimeoutOrTestException = false;
        for (ExecutionResult result : results) {
            if (result.hasTestException()) {
                hasTimeoutOrTestException = true;
                break;
            }
        }

        Set<TestFitnessFunction> setOfCoveredGoals = new LinkedHashSet<>();
        if (hasTimeoutOrTestException) {
            logger.info("Test suite has timed out, setting fitness to max value " + totalGoals);
            fitness = totalGoals;
        } else
            fitness = computeDistance(results, setOfCoveredGoals);

        int coveredGoals = setOfCoveredGoals.size() + removedGoals.size();

        if (totalGoals > 0)
            suite.setCoverage(this, (double) coveredGoals / (double) totalGoals);
        else
            suite.setCoverage(this, 1.0);

        suite.setNumOfCoveredGoals(this, coveredGoals);

        printStatusMessages(suite, coveredGoals, fitness);
        updateIndividual(this, suite, fitness);

        assert (coveredGoals <= totalGoals) : "Covered " + coveredGoals + " vs total goals " + totalGoals;
        assert (fitness >= 0.0);
        assert (fitness != 0.0 || coveredGoals == totalGoals) : "Fitness: " + fitness + ", "
                + "coverage: " + coveredGoals + "/" + totalGoals;
        assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
                + suite.getCoverage(this);

        return fitness;
    }

    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE) {
            return false;
        }

        for (InputCoverageTestFitness goal : this.toRemoveGoals) {
            if (this.inputCoverageMap.remove(goal)) {
                this.removedGoals.add(goal);
            } else {
                throw new IllegalStateException("goal to remove not found");
            }
        }

        this.toRemoveGoals.clear();
        logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());

        return true;
    }

    private double computeDistance(List<ExecutionResult> results, Set<TestFitnessFunction> setOfCoveredGoals) {

        Map<InputCoverageTestFitness, Double> mapDistances = new LinkedHashMap<InputCoverageTestFitness, Double>();
        for (InputCoverageTestFitness testFitness : this.inputCoverageMap) {
            mapDistances.put(testFitness, 1.0);
        }

        for (ExecutionResult result : results) {
            if (result.hasTestException()) {
                continue;
            }

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

            Iterator<InputCoverageTestFitness> it = this.inputCoverageMap.iterator();
            while (it.hasNext()) {
                InputCoverageTestFitness testFitness = it.next();

                if (!mapDistances.containsKey(testFitness)) {
                    continue;
                }

                double distance = testFitness.getFitness(test, result); // archive is updated by the TestFitnessFunction class

                mapDistances.put(testFitness, Math.min(distance, mapDistances.get(testFitness)));

                if (distance == 0.0) {
                    mapDistances.remove(testFitness);
                    setOfCoveredGoals.add(testFitness); // helper to count the number of covered goals
                    this.toRemoveGoals.add(testFitness); // goal to not be considered by the next iteration of the evolutionary algorithm
                }
            }
        }

        double distance = 0.0;
        if (!mapDistances.isEmpty()) {
            distance = mapDistances.values().stream().reduce(Double::sum).get().doubleValue();
        }
        return distance;
    }

    /**
     * Some useful debug information
     *
     * @param coveredGoals
     * @param fitness
     */
    private void printStatusMessages(
            AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
            int coveredGoals, double fitness) {
        if (coveredGoals > maxCoveredGoals) {
            logger.info("(Input Goals) Best individual covers " + coveredGoals + "/"
                    + totalGoals + " input goals");
            maxCoveredGoals = coveredGoals;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
        if (fitness < bestFitness) {
            logger.info("(Fitness) Best individual covers " + coveredGoals + "/"
                    + totalGoals + " input goals");
            bestFitness = fitness;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
    }

}
