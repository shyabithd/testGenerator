package generator.ga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class SecondaryObjective<T extends Chromosome> implements Serializable {

	private static final long serialVersionUID = -4117187516650844086L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(SecondaryObjective.class);

	public abstract int compareChromosomes(T chromosome1, T chromosome2);

	public abstract int compareGenerations(T parent1, T parent2,
	        T child1, T child2);

}
