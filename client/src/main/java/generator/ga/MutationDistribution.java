package generator.ga;

import generator.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class MutationDistribution implements Serializable {

  private static final long serialVersionUID = -5800252656232641574L;

  /** Constant <code>logger</code> */
  protected static final Logger logger = LoggerFactory.getLogger(MutationDistribution.class);

  protected int sizeOfDistribution;

  /**
   * Check whether a particular chromosome is allowed to be mutated
   * 
   * @param index
   * @return true if mutation is allowed, false otherwise
   */
  public abstract boolean toMutate(int index);

  public static MutationDistribution getMutationDistribution(int sizeOfDistribution) {
    switch (Properties.MUTATION_PROBABILITY_DISTRIBUTION) {
      case UNIFORM:
      default:
        logger.debug("Using uniform mutation distribution");
        return new UniformMutation(sizeOfDistribution);
      case BINOMIAL:
        logger.debug("Using binomial mutation distribution");
        return new BinomialMutation(sizeOfDistribution);
    }
  }
}
