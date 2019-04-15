package generator.ga.crossover;

import generator.ga.Chromosome;
import generator.ga.ConstructionFailedException;
import generator.utils.Randomness;

public class SinglePointFixedCrossOver extends CrossOverFunction {

	private static final long serialVersionUID = 1215946828935020651L;

	/**
	 * {@inheritDoc}
	 *
	 * The splitting point for to individuals p1, p2 is selected within
	 * min(length(p1),length(p2))
	 */
	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2)
	        throws ConstructionFailedException {

		if (parent1.size() < 2 || parent2.size() < 2) {
			return;
		}

		int point = Randomness.nextInt(Math.min(parent1.size(), parent2.size()) - 1) + 1;

		Chromosome t1 = parent1.clone();
		Chromosome t2 = parent2.clone();

		parent1.crossOver(t2, point, point);
		parent2.crossOver(t1, point, point);
	}

}
