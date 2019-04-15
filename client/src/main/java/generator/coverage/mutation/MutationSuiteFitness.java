package generator.coverage.mutation;

import generator.Properties;
import generator.coverage.branch.BranchCoverageSuiteFitness;
import generator.ga.archive.Archive;
import generator.mutation.Mutation;
import generator.testcase.ExecutableChromosome;
import generator.testcase.ExecutionResult;
import generator.testcase.TestCase;
import generator.testcase.TestFitnessFunction;
import generator.testsuite.AbstractTestSuiteChromosome;
import generator.testsuite.TestSuiteFitnessFunction;
import generator.utils.ArrayUtil;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Abstract MutationSuiteFitness class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class MutationSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8320078404661057113L;

	protected final BranchCoverageSuiteFitness branchFitness;

	// target goals
	protected final Map<Integer, MutationTestFitness> mutantMap = new LinkedHashMap<Integer, MutationTestFitness>();
	protected final int numMutants;

	protected final Set<Integer> removedMutants = new LinkedHashSet<Integer>();
	protected final Set<Integer> toRemoveMutants = new LinkedHashSet<Integer>();

	public MutationSuiteFitness() {
		MutationFactory factory = new MutationFactory(
		        ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.STRONGMUTATION));
		branchFitness = new BranchCoverageSuiteFitness();

		for (MutationTestFitness goal : factory.getCoverageGoals()) {
			mutantMap.put(goal.getMutation().getId(), goal);
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);
		}

		this.numMutants = this.mutantMap.size();
	}

	@Override
	public boolean updateCoveredGoals() {
		if (!Properties.TEST_ARCHIVE) {
			return false;
		}

		for (Integer mutant : this.toRemoveMutants) {
			TestFitnessFunction ff = this.mutantMap.remove(mutant);
			if (ff != null) {
				this.removedMutants.add(mutant);
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}

		this.toRemoveMutants.clear();
		logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());

		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		return MutationTestFitness.runTest(test, mutant);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public abstract double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual);
}
