package generator.testsuite;

import generator.Properties;
import generator.coverage.TestFitnessFactory;
import generator.testcase.ExecutionTracer;
import generator.testcase.TestCase;
import generator.testcase.TestChromosome;
import generator.testcase.TestFitnessFunction;

/**
 * Historical concrete TestFitnessFactories only implement the getGoals() method
 * of TestFitnessFactory. Those old Factories can just extend these
 * AstractFitnessFactory to support the new method getFitness()
 * 
 * @author Sebastian Steenbuck
 */
public abstract class AbstractFitnessFactory<T extends TestFitnessFunction> implements
		TestFitnessFactory<T> {

	/**
	 * A concrete factory can store the time consumed to initially compute all
	 * coverage goals in this field in order to track this information in
	 * SearchStatistics.
	 */
	public static long goalComputationTime = 0l;

	
	protected boolean isCUT(String className) {
		if (!Properties.TARGET_CLASS.equals("")
				&& !(className.equals(Properties.TARGET_CLASS) || className
						.startsWith(Properties.TARGET_CLASS + "$"))) {
			return false;
		}
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public double getFitness(TestSuiteChromosome suite) {

		ExecutionTracer.enableTraceCalls();

		int coveredGoals = 0;
		for (T goal : getCoverageGoals()) {
			for (TestChromosome test : suite.getTestChromosomes()) {
				if (goal.isCovered((TestCase) test)) {
					coveredGoals++;
					break;
				}
			}
		}

		ExecutionTracer.disableTraceCalls();

		return getCoverageGoals().size() - coveredGoals;
	}
}
