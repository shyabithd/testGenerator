package generator.ga.populationlimit;

import generator.Properties;
import generator.ga.Chromosome;

import java.util.List;


/**
 * <p>IndividualPopulationLimit class.</p>
 *
 * @author Gordon Fraser
 */
public class IndividualPopulationLimit implements PopulationLimit {

	private static final long serialVersionUID = -3985726226793280031L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPopulationFull(List<? extends Chromosome> population) {
		return population.size() >= Properties.POPULATION;
	}

}
