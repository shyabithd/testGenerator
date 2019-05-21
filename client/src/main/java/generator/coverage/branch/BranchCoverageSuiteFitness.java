package generator.coverage.branch;

import generator.ClassReader;
import generator.TestGenerationContext;
import generator.ga.archive.Archive;
import generator.testcase.ExecutableChromosome;
import generator.testcase.ExecutionResult;
import generator.testcase.TestChromosome;
import generator.testcase.TestFitnessFunction;
import generator.testcase.statement.Statement;
import generator.testsuite.AbstractTestSuiteChromosome;
import generator.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generator.Properties;
import java.util.*;
import java.util.Map.Entry;

public class BranchCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 2991632394620406243L;

	private final static Logger logger = LoggerFactory.getLogger(BranchCoverageSuiteFitness.class);

	// Coverage targets
	public int totalGoals;
	public int totalMethods;
	public int totalBranches;
	private final Set<String> branchlessMethods;
	private final Set<String> methods;

	protected final Set<Integer> branchesId;
	
	// Some stuff for debug output
	public int maxCoveredBranches = 0;
	public int maxCoveredMethods = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	protected final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new LinkedHashMap<Integer, TestFitnessFunction>();
	protected final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new LinkedHashMap<Integer, TestFitnessFunction>();
	private final Map<String, TestFitnessFunction> branchlessMethodCoverageMap = new LinkedHashMap<String, TestFitnessFunction>();

	private final Set<Integer> toRemoveBranchesT = new LinkedHashSet<>();
	private final Set<Integer> toRemoveBranchesF = new LinkedHashSet<>();
	private final Set<String> toRemoveRootBranches = new LinkedHashSet<>();
	
	private final Set<Integer> removedBranchesT = new LinkedHashSet<>();
	private final Set<Integer> removedBranchesF = new LinkedHashSet<>();
	private final Set<String> removedRootBranches = new LinkedHashSet<>();
	
	// Total coverage value, used by Regression
	public double totalCovered = 0.0;	
	
	/**
	 * <p>
	 * Constructor for BranchCoverageSuiteFitness.
	 * </p>
	 */
	
	/**
	 * <p>
	 * Constructor for BranchCoverageSuiteFitness.
	 * </p>
	 */
	public BranchCoverageSuiteFitness() {
		String prefix = Properties.TARGET_CLASS_PREFIX;

		if (prefix.isEmpty())
			prefix = Properties.TARGET_CLASS;

		totalMethods = 0;
		totalBranches = 0;
		branchlessMethods = new HashSet<>();
		methods = new HashSet<>();
		ClassReader.Method[] methods = TestGenerationContext.getInstance().getClassReader().getDeclaredMethods();
		for(ClassReader.Method method : methods) {
			this.methods.add(method.methodName);
			this.branchlessMethods.add(method.methodName);
		}
		branchesId = new LinkedHashSet<>();

		determineCoverageGoals();

		totalGoals = branchCoverageTrueMap.size() + branchCoverageFalseMap.size() + branchlessMethodCoverageMap.size();

		logger.info("Total branch coverage goals: " + totalGoals);
		logger.info("Total branches: " + totalBranches);
		logger.info("Total branchless methods: " + branchlessMethodCoverageMap.size());
		logger.info("Total methods: " + totalMethods + ": " + methods);
	}

	/**
	 * Initialize the set of known coverage goals
	 */
	protected void determineCoverageGoals() {
		List<BranchCoverageTestFitness> goals = new BranchCoverageFactory().getCoverageGoals();
		for (BranchCoverageTestFitness goal : goals) {
			// Skip instrumented branches - we only want real branches
			if(goal.getBranch() != null) {
				if(goal.getBranch().isInstrumented()) {
					continue;
				}
			}
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);
			
			if (goal.getBranch() == null) {
				branchlessMethodCoverageMap.put(goal.getClassName() + "."
				                                        + goal.getMethod(), goal);
			} else {
				branchesId.add(goal.getBranch().getActualBranchId());
				if (goal.getBranchExpressionValue())
					branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), goal);
				else
					branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), goal);
			}
		}
	}

	private void handleConstructorExceptions(TestChromosome test, ExecutionResult result,
											 Map<String, Integer> callCount) {

			if (result.hasTestException() || result.noThrownExceptions()) {
				return;
			}

			Integer exceptionPosition = result.getFirstPositionOfThrownException();
			// TODO: Not sure why that can happen
			if (exceptionPosition >= result.test.size()) {
				return;
			}

			Statement statement = null;
			if (result.test.hasStatement(exceptionPosition)) {
				statement = result.test.getStatement(exceptionPosition);
			}
	}

	protected void handleBranchlessMethods(TestChromosome test, ExecutionResult result, Map<String, Integer> callCount) {
		for (Entry<String, Integer> entry : result.getTrace().getMethodExecutionCount().entrySet()) {

			if (entry.getKey() == null || !methods.contains(entry.getKey()) || removedRootBranches.contains(entry.getKey()))
				continue;
			if (!callCount.containsKey(entry.getKey()))
				callCount.put(entry.getKey(), entry.getValue());
			else {
				callCount.put(entry.getKey(),
						callCount.get(entry.getKey()) + entry.getValue());
			}
			// If a specific target method is set we need to check
			// if this is a target branch or not
			if (branchlessMethodCoverageMap.containsKey(entry.getKey())) {
				TestFitnessFunction goal = branchlessMethodCoverageMap.get(entry.getKey());
				//test.getTestCase().addCoveredGoal(goal);
				toRemoveRootBranches.add(entry.getKey());
				if (Properties.TEST_ARCHIVE) {
					Archive.getArchiveInstance().updateArchive(goal, test, 0.0);
				}
			}
		}
	}

	protected void handlePredicateCount(ExecutionResult result, Map<Integer, Integer> predicateCount) {
		for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
			if (!branchesId.contains(entry.getKey())
					|| (removedBranchesT.contains(entry.getKey())
					&& removedBranchesF.contains(entry.getKey())))
				continue;
			if (!predicateCount.containsKey(entry.getKey()))
				predicateCount.put(entry.getKey(), entry.getValue());
			else {
				predicateCount.put(entry.getKey(),
						predicateCount.get(entry.getKey())
								+ entry.getValue());
			}
		}
	}


	protected void handleTrueDistances(TestChromosome test, ExecutionResult result, Map<Integer, Double> trueDistance) {
		for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {
			if(!branchesId.contains(entry.getKey())||removedBranchesT.contains(entry.getKey())) continue;
			if (!trueDistance.containsKey(entry.getKey()))
				trueDistance.put(entry.getKey(), entry.getValue());
			else {
				trueDistance.put(entry.getKey(),
						Math.min(trueDistance.get(entry.getKey()),
								entry.getValue()));
			}
			BranchCoverageTestFitness goal = (BranchCoverageTestFitness) this.branchCoverageTrueMap.get(entry.getKey());
			assert goal != null;
			if ((Double.compare(entry.getValue(), 0.0) == 0)) {
				//test.getTestCase().addCoveredGoal(goal);
				toRemoveBranchesT.add(entry.getKey());
			}
			if(Properties.TEST_ARCHIVE) {
				Archive.getArchiveInstance().updateArchive(goal, test, entry.getValue());
			}
		}

	}

	protected void handleFalseDistances(TestChromosome test, ExecutionResult result, Map<Integer, Double> falseDistance) {
		for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
			if(!branchesId.contains(entry.getKey())||removedBranchesF.contains(entry.getKey())) continue;
			if (!falseDistance.containsKey(entry.getKey()))
				falseDistance.put(entry.getKey(), entry.getValue());
			else {
				falseDistance.put(entry.getKey(),
						Math.min(falseDistance.get(entry.getKey()),
								entry.getValue()));
			}
			BranchCoverageTestFitness goal = (BranchCoverageTestFitness) this.branchCoverageFalseMap.get(entry.getKey());
			assert goal != null;
			if ((Double.compare(entry.getValue(), 0.0) == 0)) {
				//test.getTestCase().addCoveredGoal(goal);
				toRemoveBranchesF.add(entry.getKey());
			}
			if(Properties.TEST_ARCHIVE) {
				Archive.getArchiveInstance().updateArchive(goal, test, entry.getValue());
			}
		}

	}

	/**
	 * Iterate over all execution results and summarize statistics
	 * 
	 * @param results
	 * @param predicateCount
	 * @param callCount
	 * @param trueDistance
	 * @param falseDistance
	 * @return
	 */
	private boolean analyzeTraces(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, List<ExecutionResult> results,
								  Map<Integer, Integer> predicateCount, Map<String, Integer> callCount,
								  Map<Integer, Double> trueDistance, Map<Integer, Double> falseDistance) {
		boolean hasTimeoutOrTestException = false;
		for (ExecutionResult result : results) {
			if (result.hasTestException()) {
				hasTimeoutOrTestException = true;
				continue;
			}

			TestChromosome test = new TestChromosome();
			test.setTestCase(result.test);
			test.setLastExecutionResult(result);
			test.setChanged(false);

			handleBranchlessMethods(test, result, callCount);
			handlePredicateCount(result, predicateCount);
			handleTrueDistances(test, result, trueDistance);
			handleFalseDistances(test, result, falseDistance);

			// In case there were exceptions in a constructor
			handleConstructorExceptions(test, result, callCount);
		}
		return hasTimeoutOrTestException;
	}
	
	@Override
	public boolean updateCoveredGoals() {
		if (!Properties.TEST_ARCHIVE) {
			return false;
		}
		
		for (String method : toRemoveRootBranches) {
			boolean removed = branchlessMethods.remove(method);
			TestFitnessFunction f = branchlessMethodCoverageMap.remove(method);
			if (removed && f != null) {
				totalMethods--;
				methods.remove(method);
				removedRootBranches.add(method);
				//removeTestCall(f.getTargetClass(), f.getTargetMethod());
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}

		for (Integer branch : toRemoveBranchesT) {
			TestFitnessFunction f = branchCoverageTrueMap.remove(branch);
			if (f != null) {
				removedBranchesT.add(branch);
				if (removedBranchesF.contains(branch)) {
					totalBranches--;
					//if(isFullyCovered(f.getTargetClass(), f.getTargetMethod())) {
					//	removeTestCall(f.getTargetClass(), f.getTargetMethod());
					//}
				}
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}
		for (Integer branch : toRemoveBranchesF) {
			TestFitnessFunction f = branchCoverageFalseMap.remove(branch);
			if (f != null) {
				removedBranchesF.add(branch);
				if (removedBranchesT.contains(branch)) {
					totalBranches--;
					//if(isFullyCovered(f.getTargetClass(), f.getTargetMethod())) {
					//	removeTestCall(f.getTargetClass(), f.getTargetMethod());
					//}
				}
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}
		
		toRemoveRootBranches.clear();
		toRemoveBranchesF.clear();
		toRemoveBranchesT.clear();
		logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Execute all tests and count covered branches
	 */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating branch fitness");
		double fitness = 0.0;

		List<ExecutionResult> results = runTestSuite(suite);
		Map<Integer, Double> trueDistance = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new LinkedHashMap<Integer, Double>();
		Map<Integer, Integer> predicateCount = new LinkedHashMap<Integer, Integer>();
		Map<String, Integer> callCount = new LinkedHashMap<String, Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(suite, results, predicateCount,
		                                                  callCount, trueDistance,
		                                                  falseDistance);

		// Collect branch distances of covered branches
		int numCoveredBranches = 0;

		for (Integer key : predicateCount.keySet()) {
			
			double df = 0.0;
			double dt = 0.0;
			int numExecuted = predicateCount.get(key);
			
			if(removedBranchesT.contains(key))
				numExecuted++;
			if(removedBranchesF.contains(key))
				numExecuted++;
			
			if (trueDistance.containsKey(key)) {
				dt =  trueDistance.get(key);
			}
			if(falseDistance.containsKey(key)){
				df = falseDistance.get(key);
			}
			// If the branch predicate was only executed once, then add 1 
			if (numExecuted == 1) {
				fitness += 1.0;
			} else {
				fitness += normalize(df) + normalize(dt);
			}

			if (falseDistance.containsKey(key)&&(Double.compare(df, 0.0) == 0))
				numCoveredBranches++;

			if (trueDistance.containsKey(key)&&(Double.compare(dt, 0.0) == 0))
				numCoveredBranches++;
		}
		
		// +1 for every branch that was not executed
		fitness += 2 * (totalBranches - predicateCount.size());

		// Ensure all methods are called
		int missingMethods = 0;
		for (String e : methods) {
			if (!callCount.containsKey(e)) {
				fitness += 1.0;
				missingMethods += 1;
			}
		}
		printStatusMessages(suite, numCoveredBranches, totalMethods - missingMethods,
		                    fitness);

		// Calculate coverage
		int coverage = numCoveredBranches;
		for (String e : branchlessMethodCoverageMap.keySet()) {
			if (callCount.keySet().contains(e)) {
				coverage++;
			}

		}

		coverage +=removedBranchesF.size();
		coverage +=removedBranchesT.size();
		coverage +=removedRootBranches.size();
	
 		
		if (totalGoals > 0)
			suite.setCoverage(this, (double) coverage / (double) totalGoals);
		else 
            suite.setCoverage(this, 1);
		
		totalCovered = suite.getCoverage(this);

		suite.setNumOfCoveredGoals(this, coverage);
		suite.setNumOfNotCoveredGoals(this, totalGoals-coverage);
		
		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value "
			        + (totalBranches * 2 + totalMethods));
			fitness = totalBranches * 2 + totalMethods;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coverage <= totalGoals) : "Covered " + coverage + " vs total goals "
		        + totalGoals;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coverage == totalGoals) : "Fitness: " + fitness + ", "
		        + "coverage: " + coverage + "/" + totalGoals;
		assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage(this); 
		return fitness;
	}
	

	
	/*
	 * Max branch coverage value
	 */
	public int getMaxValue() {
		return  totalBranches * 2 + totalMethods;
	}

	/**
	 * Some useful debug information
	 * 
	 * @param coveredBranches
	 * @param coveredMethods
	 * @param fitness
	 */
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredBranches, int coveredMethods, double fitness) {
		if (coveredBranches > maxCoveredBranches) {
			maxCoveredBranches = coveredBranches;
			logger.info("(Branches) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
		if (coveredMethods > maxCoveredMethods) {
			logger.info("(Methods) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			maxCoveredMethods = coveredMethods;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredBranches + "/"
			        + (totalBranches * 2) + " branches and " + coveredMethods + "/"
			        + totalMethods + " methods");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
	}
}
