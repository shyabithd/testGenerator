package generator.ga.crossover;

import generator.Properties;
import generator.ga.Chromosome;
import generator.ga.ConstructionFailedException;
import generator.utils.Randomness;

/**
 * Implement uniform crossover. In a uniform crossover, we do not divide the
 * chromosome into segments, rather we treat each gene separately. In this,
 * we essentially flip a coin for each chromosome.
 *
 * @author Yan Ge
 */
public class UniformCrossOver extends CrossOverFunction {

	private static final long serialVersionUID = 2981387570766261795L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2)
			throws ConstructionFailedException {

		if (parent1.size() < 2 || parent2.size() < 2) {
			return;
		}

		int maxNumGenes = Math.min(parent1.size(), parent2.size());

		Chromosome t1 = parent1.clone();
		Chromosome t2 = parent2.clone();

		for (int i = 0; i < maxNumGenes; i++) {
			if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
				parent1.crossOver(t2, i);
				parent2.crossOver(t1, i);
			}
		}
	}
}

