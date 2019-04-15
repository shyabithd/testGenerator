package generator.ga.populationlimit;

import generator.Properties;
import generator.ga.Chromosome;

import java.util.List;

public class SizePopulationLimit implements PopulationLimit {

	private static final long serialVersionUID = 7978512501601348014L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPopulationFull(List<? extends Chromosome> population) {
		int size = 0;
		for (Chromosome chromosome : population)
			size += chromosome.size();

		return size >= Properties.POPULATION;
	}

}
