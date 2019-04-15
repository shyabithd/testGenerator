package generator.ga;

import java.io.Serializable;

/**
 * Decides when offspring replaces its parents for the next generation
 * 
 * @author Gordon Fraser
 */
public abstract class ReplacementFunction implements Serializable {

	private static final long serialVersionUID = 8507488475265387482L;

	protected boolean maximize = false;

	/**
	 * <p>
	 * Constructor for ReplacementFunction.
	 * </p>
	 * 
	 * @param maximize
	 *            a boolean.
	 */
	public ReplacementFunction(boolean maximize) {
		this.maximize = maximize;
	}

	protected boolean isBetter(Chromosome chromosome1, Chromosome chromosome2) {
		if (maximize) {
			return chromosome1.compareTo(chromosome2) > 0;
		} else {
			return chromosome1.compareTo(chromosome2) < 0;
		}
	}

	protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
		if (maximize) {
			return chromosome1.compareTo(chromosome2) >= 0;
		} else {
			return chromosome1.compareTo(chromosome2) <= 0;
		}
	}

	protected Chromosome getBest(Chromosome chromosome1, Chromosome chromosome2) {
		if (isBetter(chromosome1, chromosome2))
			return chromosome1;
		else
			return chromosome2;
	}

	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
	        Chromosome offspring1, Chromosome offspring2) {
		if (maximize) {
			return compareBestOffspringToBestParent(parent1, parent2, offspring1,
			                                        offspring2) >= 0;
		} else {
			return compareBestOffspringToBestParent(parent1, parent2, offspring1,
			                                        offspring2) <= 0;
		}
	}

	protected int compareBestOffspringToBestParent(Chromosome parent1,
	        Chromosome parent2, Chromosome offspring1, Chromosome offspring2) {

		Chromosome bestOffspring = getBest(offspring1, offspring2);
		Chromosome bestParent = getBest(parent1, parent2);

		return bestOffspring.compareTo(bestParent);
	}

	@Deprecated
	public boolean keepOffspring(Chromosome parent, Chromosome offspring) {
		return isBetterOrEqual(offspring, parent);
	}
}
