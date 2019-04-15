package generator.ga.populationlimit;

import generator.ga.Chromosome;

import java.io.Serializable;
import java.util.List;

/**
 * <p>PopulationLimit interface.</p>
 *
 * @author Gordon Fraser
 */
public interface PopulationLimit extends Serializable {
	/**
	 * <p>isPopulationFull</p>
	 *
	 * @param population a {@link List} object.
	 * @return a boolean.
	 */
	public boolean isPopulationFull(List<? extends Chromosome> population);
}
