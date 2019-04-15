package generator.coverage;

import generator.testcase.TestFitnessFunction;
import generator.testsuite.TestSuiteChromosome;

import java.util.List;

public interface TestFitnessFactory<T extends TestFitnessFunction> {

	/**
	 * Generate a list of goals to cover
	 * 
	 * @return a {@link List} object.
	 */
	public List<T> getCoverageGoals();

	public double getFitness(TestSuiteChromosome suite);
}
