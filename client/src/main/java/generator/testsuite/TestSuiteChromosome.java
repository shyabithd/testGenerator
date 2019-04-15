package generator.testsuite;

import generator.ga.Chromosome;
import generator.ga.ChromosomeFactory;
import generator.ga.LocalSearchObjective;
import generator.ga.SecondaryObjective;
import generator.testcase.TestCase;
import generator.testcase.TestChromosome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TestSuiteChromosome extends AbstractTestSuiteChromosome<TestChromosome> {

	/** Secondary objectives used during ranking */
	private static final List<SecondaryObjective<?>> secondaryObjectives = null; //new
	// ArrayList<SecondaryObjective<?>>();
	private static int secondaryObjIndex = 0;
	private static final long serialVersionUID = 88380759969800800L;

	public TestSuiteChromosome(ChromosomeFactory<TestChromosome> testChromosomeFactory) {

	}

	public static void ShuffleSecondaryObjective() {
		Collections.shuffle(secondaryObjectives);
	}
	
	public static int getSecondaryObjectivesSize(){
		return secondaryObjectives.size();
	}
	
	public static boolean isFirstSecondaryObjectiveEnabled(){
		return secondaryObjIndex == 0;
	}
	
	public static void disableFirstSecondaryObjective() {
		if (secondaryObjIndex != 1)
			secondaryObjIndex = 1;
	}
	
	public static void enableFirstSecondaryObjective() {
		if (secondaryObjIndex != 0)
			secondaryObjIndex = 0;
	}

	public static void reverseSecondaryObjective() {
		Collections.reverse(secondaryObjectives);
	}

	public static void removeAllSecondaryObjectives() {
		secondaryObjectives.clear();
	}

	/**
	 * <p>
	 * Constructor for TestSuiteChromosome.
	 * </p>
	 */
	public TestSuiteChromosome() {
		super();
	}

	public boolean localSearch(LocalSearchObjective<? extends Chromosome> objective) {
		return false;
	}

	public TestChromosome addTest(TestCase test) {
		TestChromosome c = new TestChromosome();
		c.setTestCase(test);
		//addTest(c);

		return c;
	}

	
	public void clearMutationHistory() {
//		for(TestChromosome test : tests) {
//			test.getMutationHistory().clear();
//		}
	}

	/**
	 * Remove all tests
	 */
	public void clearTests() {
//		tests.clear();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Create a deep copy of this test suite
	 */
	@Override
	public TestSuiteChromosome clone() {
		return new TestSuiteChromosome();
	}

	@Override
	public <T extends Chromosome> int compareSecondaryObjective(T o) {
		return 0;
	}

	/**
	 * For manual algorithm
	 * 
	 * @param testCase
	 *            to remove
	 */
	public void deleteTest(TestCase testCase) {
		if (testCase != null) {
		}
	}

	/**
	 * <p>
	 * getTests
	 * </p>
	 *
	 * @return a {@link List} object.
	 */
	public List<TestCase> getTests() {
		List<TestCase> testcases = new ArrayList<TestCase>();

		return testcases;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Determine relative ordering of this chromosome to another chromosome If
	 * fitness is equal, the shorter chromosome comes first
	 */
	/*
	 * public int compareTo(Chromosome o) { if(RANK_LENGTH && getFitness() ==
	 * o.getFitness()) { return (int) Math.signum((length() -
	 * ((TestSuiteChromosome)o).length())); } else return (int)
	 * Math.signum(getFitness() - o.getFitness()); }
	 */
 
}
