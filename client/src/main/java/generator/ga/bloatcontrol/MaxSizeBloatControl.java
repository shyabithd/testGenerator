package generator.ga.bloatcontrol;

import generator.Properties;
import generator.ga.Chromosome;

public class MaxSizeBloatControl implements BloatControlFunction {

	private static final long serialVersionUID = -8241127914702360972L;

	/**
	 * {@inheritDoc}
	 *
	 * Check whether the chromosome is bigger than the max length constant
	 */
	@Override
	public boolean isTooLong(Chromosome chromosome) {
		return chromosome.size() > Properties.MAX_SIZE;
	}

}
