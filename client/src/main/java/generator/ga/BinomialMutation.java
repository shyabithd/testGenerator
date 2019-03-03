package generator.ga;

import generator.utils.Randomness;

import java.util.LinkedHashSet;
import java.util.Set;

public class BinomialMutation extends MutationDistribution {

  private static final long serialVersionUID = 9013772318848850918L;

  private Set<Integer> bitsToBeModified;

  public BinomialMutation(int sizeOfDistribution) {
    int numBits = howManyBits(sizeOfDistribution, 1.0 / (double) sizeOfDistribution);
    this.bitsToBeModified = new LinkedHashSet<Integer>();
    while (this.bitsToBeModified.size() < numBits) {
      this.bitsToBeModified.add(Randomness.nextInt(0, sizeOfDistribution));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean toMutate(int index) {
    if (this.bitsToBeModified.contains(index)) {
      return true;
    }
    return false;
  }

  /**
   * Number of bits to be mutated (in our context, number of test cases to be mutated) according to
   * a binomial distribution.
   * 
   * @param numTrials
   * @param probability
   * @return number of test cases to be mutated
   */
  private int howManyBits(int numTrials, double probability) {
    int numberBits = 0;
    for (int i = 0; i < numTrials; i++) {
      if (Randomness.nextDouble() <= probability) {
        numberBits++;
      }
    }
    return numberBits;
  }
}
