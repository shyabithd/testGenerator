package generator.coverage.mutation;

import generator.Properties;
import generator.mutation.Mutation;
import generator.rmi.ClientServices;
import generator.statistics.RuntimeVariable;
import generator.testsuite.AbstractFitnessFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * MutationFactory class.
 * </p>
 * 
 * @author fraser
 */
public class MutationFactory extends AbstractFitnessFactory<MutationTestFitness> {

	private boolean strong = true;

	private List<MutationTestFitness> goals = null;

	/**
	 * <p>
	 * Constructor for MutationFactory.
	 * </p>
	 */
	public MutationFactory() {
	}

	/**
	 * <p>
	 * Constructor for MutationFactory.
	 * </p>
	 * 
	 * @param strongMutation
	 *            a boolean.
	 */
	public MutationFactory(boolean strongMutation) {
		this.strong = strongMutation;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<MutationTestFitness> getCoverageGoals() {
		return getCoverageGoals(null);
	}

	/**
	 * <p>
	 * getCoverageGoals
	 * </p>
	 * 
	 * @param targetMethod
	 *            a {@link String} object.
	 * @return a {@link List} object.
	 */
	public List<MutationTestFitness> getCoverageGoals(String targetMethod) {
		if (goals != null)
			return goals;

		goals = new ArrayList<MutationTestFitness>();

		for (Mutation m : getMutantsLimitedPerClass()) {
			if (targetMethod != null && !m.getMethodName().endsWith(targetMethod))
				continue;

			// We need to return all mutants to make coverage values and bitstrings consistent 
			//if (MutationTimeoutStoppingCondition.isDisabled(m))
			//	continue;
			if (strong)
				goals.add(new StrongMutationTestFitness(m));
			else
				goals.add(new WeakMutationTestFitness(m));
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Mutants, goals.size());

		return goals;
	}
	
	/**
	 * Try to remove mutants per mutation operator until the number of mutants
	 * is acceptable wrt the class limit
	 */
	private List<Mutation> getMutantsLimitedPerClass() {
		List<Mutation> mutants = MutationPool.getMutants();
		String[] operators = { "ReplaceVariable", "InsertUnaryOperator", "ReplaceConstant", "ReplaceArithmeticOperator" };
		if(mutants.size() > Properties.MAX_MUTANTS_PER_CLASS) {
			for(String op : operators) {
				mutants.removeIf(u -> u.getMutationName().startsWith(op));
				if(mutants.size() < Properties.MAX_MUTANTS_PER_CLASS)
					break;
			}
		}
		return mutants;
	}
}
