package generator.coverage.mutation;

import generator.Properties;
import generator.ga.archive.Archive;
import generator.mutation.Mutation;
import generator.testcase.ExecutionResult;
import generator.testcase.TestChromosome;

/**
 * <p>
 * WeakMutationTestFitness class.
 * </p>
 * 
 * @author fraser
 */
public class WeakMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = 7468742584904580204L;

	public WeakMutationTestFitness(Mutation mutation) {
		super(mutation);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.mutation.MutationTestFitness#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		double executionDistance = diameter;

		// Get control flow distance
		if (!result.getTrace().wasMutationTouched(mutation.getId()))
			executionDistance = getExecutionDistance(result);
		else
			executionDistance = 0.0;

		double infectionDistance = 1.0;

		// If executed, but not with reflection
		if (executionDistance <= 0) {
			if(executionDistance < 0) {
				logger.warn("Execution distance less than 0! "+mutation);
				assert(false) : "Invalid execution distance on mutation "+mutation;
				executionDistance = 0.0;
			}
			// Add infection distance
			assert (result.getTrace() != null);
			// assert (result.getTrace().mutantDistances != null);
			assert (result.getTrace().wasMutationTouched(mutation.getId()));
			assert (result.getTrace().getMutationDistance(mutation.getId()) >= 0) : "Infection distance less than 0: " + mutation;
			infectionDistance = normalize(result.getTrace().getMutationDistance(mutation.getId()));
			logger.debug("Infection distance for mutation = " + infectionDistance);
		}

		fitness = infectionDistance + executionDistance;
		logger.debug("Individual fitness: " + " + " + infectionDistance + " + "
		        + executionDistance + " = " + fitness);

		updateIndividual(this, individual, fitness);

//		if (fitness == 0.0) {
//			individual.getTestCase().addCoveredGoal(this);
//		}

		if (Properties.TEST_ARCHIVE) {
			Archive.getArchiveInstance().updateArchive(this, individual, fitness);
		}

		return fitness;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Weak " + mutation.toString();
	}

}
