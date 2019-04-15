package generator.ga.crossover;

import generator.ga.Chromosome;
import generator.ga.ConstructionFailedException;
import generator.utils.Randomness;

/**
 * Cross over individuals at relative position
 *
 * @author Gordon Fraser
 */
public class SinglePointRelativeCrossOver extends CrossOverFunction {

	private static final long serialVersionUID = -5320348525459502224L;

	/**
	 * {@inheritDoc}
	 *
	 * The splitting point is not an absolute value but a relative value (eg, at
	 * position 70% of n). For example, if n1=10 and n2=20 and splitting point
	 * is 70%, we would have position 7 in the first and 14 in the second.
	 * Therefore, the offspring d have n<=max(n1,n2)
	 */
	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2)
	        throws ConstructionFailedException {

		if (parent1.size() < 2 || parent2.size() < 2) {
			return;
		}

		Chromosome t1 = parent1.clone();
		Chromosome t2 = parent2.clone();
		// Choose a position in the middle
		float splitPoint = Randomness.nextFloat();

		int pos1 = ((int) Math.floor((t1.size() - 1) * splitPoint)) + 1;
		int pos2 = ((int) Math.floor((t2.size() - 1) * splitPoint)) + 1;

		parent1.crossOver(t2, pos1, pos2);
		parent2.crossOver(t1, pos2, pos1);
	}

}
