package generator.coverage.mutation;

import generator.coverage.ControlFlowDistance;
import generator.coverage.branch.BranchCoverageGoal;
import generator.ga.stoppingconditions.MaxStatementsStoppingCondition;
import generator.mutation.Mutation;
import generator.testcase.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Abstract MutationTestFitness class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class MutationTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 596930765039928708L;

	protected transient Mutation mutation;
	
	protected int mutantId;

	protected final Set<BranchCoverageGoal> controlDependencies = new HashSet<BranchCoverageGoal>();

	protected final int diameter = 1;


	public MutationTestFitness(Mutation mutation) {
		this.mutation = mutation;
		this.mutantId = mutation.getId();
		//controlDependencies.addAll(mutation.getControlDependencies());
//		ActualControlFlowGraph cfg = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getActualCFG(mutation.getClassName(),
//		                                                                                                        mutation.getMethodName());
//		diameter = cfg.getDiameter();
	}

	public Mutation getMutation() {
		return mutation;
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public static ExecutionResult runTest(TestCase test, Mutation mutant) {

		ExecutionResult result = new ExecutionResult(test, mutant);

		try {
			if (mutant != null)
				logger.debug("Executing test for mutant " + mutant.getId() + ": \n"
				        + test.toCode());
			else
				logger.debug("Executing test witout mutant");

			if (mutant != null)
				MutationObserver.activateMutation(mutant);
			result = TestCaseExecutor.getInstance().execute(test);
			if (mutant != null)
				MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			if (!result.noThrownExceptions()) {
				num = result.getFirstPositionOfThrownException();
			}

			//if (mutant == null)
			MaxStatementsStoppingCondition.statementsExecuted(num);

		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

	protected double getExecutionDistance(ExecutionResult result) {
		double fitness = 0.0;
		if (!result.getTrace().wasMutationTouched(mutation.getId()))
			fitness += diameter;

		// Get control flow distance
		if (controlDependencies.isEmpty()) {
			// If mutant was not executed, this can be either because of an exception, or because the method was not executed

			String key = mutation.getClassName() + "." + mutation.getMethodName();
			if (result.getTrace().getCoveredMethods().contains(key)) {
				logger.debug("Target method " + key + " was executed");
			} else {
				logger.debug("Target method " + key + " was not executed");
				fitness += diameter;
			}
		} else {
			ControlFlowDistance cfgDistance = null;
			for (BranchCoverageGoal dependency : controlDependencies) {
				logger.debug("Checking dependency...");
				ControlFlowDistance distance = dependency.getDistance(result);
				if (cfgDistance == null)
					cfgDistance = distance;
				else {
					if (distance.compareTo(cfgDistance) < 0)
						cfgDistance = distance;
				}
			}
			if (cfgDistance != null) {
				logger.debug("Found control dependency");
				fitness += cfgDistance.getResultingBranchFitness();
			}
		}

		return fitness;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	/** {@inheritDoc} */
	@Override
	public abstract double getFitness(TestChromosome individual, ExecutionResult result);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return mutation.toString();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof MutationTestFitness) {
			return mutation.compareTo(((MutationTestFitness) other).getMutation());
		}
		return compareClassName(other);
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controlDependencies == null) ? 0 : controlDependencies.hashCode());
		result = prime * result + diameter;
		result = prime * result + mutantId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutationTestFitness other = (MutationTestFitness) obj;
		if (controlDependencies == null) {
			if (other.controlDependencies != null)
				return false;
		} else if (!controlDependencies.equals(other.controlDependencies))
			return false;
		if (diameter != other.diameter)
			return false;
		if (mutantId != other.mutantId) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return mutation.getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return mutation.getMethodName();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeInt(mutation.getId());
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		mutantId = ois.readInt();
		this.mutation = MutationPool.getMutant(mutantId);
	}
}
