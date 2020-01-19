package generator.testsuite;

import generator.Properties;
import generator.ga.ChromosomeFactory;
import generator.testcase.TestChromosome;
import generator.utils.Randomness;

/**
 * <p>TestSuiteChromosomeFactory class.</p>
 *
 * @author Gordon Fraser
 */
public class TestSuiteChromosomeFactory implements ChromosomeFactory<TestSuiteChromosome> {

	private static final long serialVersionUID = -3769862881038106087L;

	/** Factory to manipulate and generate method sequences */
	protected ChromosomeFactory<TestChromosome> testChromosomeFactory;

	/**
	 * <p>Constructor for TestSuiteChromosomeFactory.</p>
	 */
	public TestSuiteChromosomeFactory() {
		testChromosomeFactory = new RandomLengthTestFactory();
	}

	public TestSuiteChromosomeFactory(ChromosomeFactory<TestChromosome> testFactory) {
		testChromosomeFactory = testFactory;

		// test_factory = new RandomLengthTestFactory();
		// test_factory = new AllMethodsChromosomeFactory();
		// test_factory = new OUMTestChromosomeFactory();
	}

	public void setTestFactory(ChromosomeFactory<TestChromosome> factory) {
		testChromosomeFactory = factory;
	}

	/** {@inheritDoc} */
	@Override
	public TestSuiteChromosome getChromosome() {

		TestSuiteChromosome chromosome = new TestSuiteChromosome(testChromosomeFactory);
		chromosome.clearTests();
		// ((AllMethodsChromosomeFactory)test_factory).clear();

		int numTests;
		//if (!Properties.GOALORI) {
			//numTests = Randomness.nextInt(Properties.MIN_INITIAL_TESTS,
			//		Properties.MAX_INITIAL_TESTS + 1);
		//} else {
			numTests = Properties.getTargetClassRegression(true).getDeclaredMethods().length;
		//}
		for (int i = 0; i < numTests; i++) {
			TestChromosome test = testChromosomeFactory.getChromosome();
			chromosome.addTest(test);
			//chromosome.tests.add(test);
		}
		// logger.info("Covered methods: "+((AllMethodsChromosomeFactory)test_factory).covered.size());
		// logger.trace("Generated new test suite:"+chromosome);
		return chromosome;
	}
}
