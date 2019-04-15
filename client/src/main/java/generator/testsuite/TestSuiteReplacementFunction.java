package generator.testsuite;

import generator.Properties;
import generator.ga.Chromosome;
import generator.ga.ReplacementFunction;

/**
 * <p>
 * TestSuiteReplacementFunction class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestSuiteReplacementFunction extends ReplacementFunction {

	private static final long serialVersionUID = -8472469271120247395L;

	/**
	 * <p>
	 * Constructor for TestSuiteReplacementFunction.
	 * </p>
	 * 
	 * @param maximize
	 *            a boolean.
	 */
	public TestSuiteReplacementFunction(boolean maximize) {
		super(maximize);
	}

	/**
	 * <p>
	 * Constructor for TestSuiteReplacementFunction.
	 * </p>
	 */
	public TestSuiteReplacementFunction() {
		super(false);
	}

	public int getLengthSum(AbstractTestSuiteChromosome<?> chromosome1,
	        AbstractTestSuiteChromosome<?> chromosome2) {
		return chromosome1.totalLengthOfTestCases()
		        + chromosome2.totalLengthOfTestCases();
	}

	/** {@inheritDoc} */
	@Override
	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
								 Chromosome offspring1, Chromosome offspring2) {

		// -1 if offspring has lower fitness, +1 if parent has lower fitness
		int cmp = compareBestOffspringToBestParent(parent1, parent2, offspring1,
		                                           offspring2);

		if (Properties.CHECK_PARENTS_LENGTH) {

			int offspringLength = getLengthSum((AbstractTestSuiteChromosome<?>) offspring1,
			                                   (AbstractTestSuiteChromosome<?>) offspring2);
			int parentLength = getLengthSum((AbstractTestSuiteChromosome<?>) parent1,
			                                (AbstractTestSuiteChromosome<?>) parent2);

			//if equivalent, only accept if it does not increase the length
			if (cmp == 0 && offspringLength <= parentLength) {
				return true;
			} else {
				if (maximize) {
					return cmp > 0;
				} else {
					return cmp < 0;
				}
			}
		} else {
			//default check
			if (maximize) {
				return cmp >= 0;
			} else {
				return cmp <= 0;
			}
		}
	}
}
