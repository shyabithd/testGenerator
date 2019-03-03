package generator.testcase;

import generator.ga.Chromosome;
import generator.ga.LocalSearchObjective;
import generator.mutation.Mutation;
import generator.mutation.MutationExecutionResult;
import generator.testsuite.TestSuiteFitnessFunction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class ExecutableChromosome extends Chromosome {
	private static final long serialVersionUID = 1L;

	protected transient ExecutionResult lastExecutionResult = null;

	protected transient Map<Mutation, MutationExecutionResult> lastMutationResult = new HashMap<Mutation, MutationExecutionResult>();

	/**
	 * <p>Constructor for ExecutableChromosome.</p>
	 */
	public ExecutableChromosome() {
		super();
	}

	public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
		this.lastExecutionResult = lastExecutionResult;
	}

	public ExecutionResult getLastExecutionResult() {
		return lastExecutionResult;
	}

	public void setLastExecutionResult(MutationExecutionResult lastExecutionResult,
	        Mutation mutation) {
		this.lastMutationResult.put(mutation, lastExecutionResult);
	}

	public MutationExecutionResult getLastExecutionResult(Mutation mutation) {
		return lastMutationResult.get(mutation);
	}
	

	/**
	 * <p>clearCachedResults</p>
	 */
	public void clearCachedResults() {
		this.lastExecutionResult = null;
		lastMutationResult.clear();
	}

	/**
	 * <p>clearCachedMutationResults</p>
	 */
	public void clearCachedMutationResults() {
		lastMutationResult.clear();
	}

	protected abstract void copyCachedResults(ExecutableChromosome other);

	@SuppressWarnings("unchecked")
	public abstract boolean localSearch(LocalSearchObjective<? extends Chromosome> objective);

	abstract public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction);
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
    IOException {
		ois.defaultReadObject();
		lastExecutionResult = null;
		lastMutationResult = new HashMap<Mutation, MutationExecutionResult>();
	}
}
