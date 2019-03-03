package generator.ga;

import generator.utils.Randomness;

public class UniformMutation extends MutationDistribution {

  private static final long serialVersionUID = -2352083320831156232L;

  public UniformMutation(int sizeOfDistribution) {
    this.sizeOfDistribution = sizeOfDistribution;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean toMutate(int index) {
    if (Randomness.nextDouble() < 1.0 / (double) this.sizeOfDistribution) {
      return true;
    }
    return false;
  }
}
