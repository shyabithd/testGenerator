package generator.ga.populationlimit;

import generator.Properties;
import generator.ga.Chromosome;
import generator.testsuite.TestSuiteChromosome;

import java.util.List;


/**
 * <p>StatementsPopulationLimit class.</p>
 *
 * @author fraser
 */
public class StatementsPopulationLimit implements PopulationLimit {

	private static final long serialVersionUID = 4794704248615412859L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPopulationFull(List<? extends Chromosome> population) {
		int numStatements = 0;
		for (Chromosome chromosome : population) {
			TestSuiteChromosome suite = (TestSuiteChromosome) chromosome;
			numStatements += suite.totalLengthOfTestCases();
		}
		return numStatements >= Properties.POPULATION;
	}

}
