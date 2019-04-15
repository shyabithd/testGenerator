package generator.ga.crossover;

import generator.ga.Chromosome;
import generator.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Cross over two individuals
 * 
 * @author Gordon Fraser
 */
public abstract class CrossOverFunction implements Serializable {

	private static final long serialVersionUID = -4765602400132319324L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(CrossOverFunction.class);

	public abstract void crossOver(Chromosome parent1, Chromosome parent2)
	        throws ConstructionFailedException;

}
