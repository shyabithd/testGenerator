package generator.ga.crossover;

import generator.ga.Chromosome;
import generator.ga.ConstructionFailedException;
import generator.utils.Randomness;

public class SinglePointCrossOver extends CrossOverFunction {

	private static final long serialVersionUID = 2881387570766261795L;

	/**
	 * {@inheritDoc}
	 *
	 * A different splitting point is selected for each individual
	 */
	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2)
	        throws ConstructionFailedException {

		if (parent1.size() < 2 || parent2.size() < 2) {
			return;
		}
		// Choose a position in the middle
		int point1 = Randomness.nextInt(parent1.size() - 1) + 1;
		int point2 = Randomness.nextInt(parent2.size() - 1) + 1;

		Chromosome t1 = parent1.clone();
		Chromosome t2 = parent2.clone();

		parent1.crossOver(t2, point1, point2);
		parent2.crossOver(t1, point2, point1);
	}

}
