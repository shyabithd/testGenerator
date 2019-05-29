package generator.testsuite;

import generator.ClassReader;
import generator.Properties;
import generator.ga.ChromosomeFactory;
import generator.testcase.*;
import generator.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <p>
 * RandomLengthTestFactory class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class RandomLengthTestFactory implements ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = -5202578461625984100L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(RandomLengthTestFactory.class);

	/**
	 * Create a random individual
	 * 
	 * @param size
	 */
	private TestCase getRandomTestCase(int size) {
		boolean tracerEnabled = ExecutionTracer.isEnabled();
		if (tracerEnabled)
			ExecutionTracer.disable();

		TestCase test = getNewTestCase();
		int num = 0;

		// Choose a random length in 0 - size
		int length = Randomness.nextInt(size);
		while (length == 0)
			length = Randomness.nextInt(size);

		TestFactory testFactory = TestFactory.getInstance();

		// Then add random stuff
		while (test.size() < length && num < Properties.MAX_ATTEMPTS) {
			testFactory.insertRandomStatement(test, test.size() - 1);
			num++;
		}
		generateAsserts(test);
		if (logger.isDebugEnabled())
			logger.debug("Randomized test case:" + test.toCode());

		if (tracerEnabled)
			ExecutionTracer.enable();

		return test;
	}

	/**
	 * Generate a random asserts
	 */
	private void generateAsserts(TestCase testCase) {
		ClassReader classReader = Properties.getTargetClassRegression(true);
		String nativeClass = classReader.getNativeClass();
		String mainMethod = Properties.mainMethodWithoutBraces.concat(System.lineSeparator()).concat(testCase.toJavaCode()).concat("}");
		nativeClass = nativeClass.replace(Properties.mainMethod, mainMethod.replaceAll(System.lineSeparator(), System.lineSeparator()+"\t\t"));

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(classReader.getDefinedclassName() + "Clzz.java"))) {
			writer.flush();
			writer.write(nativeClass);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Properties.executeCommand(Properties.nativeClassExec);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Generate a random chromosome
	 */
	@Override
	public TestChromosome getChromosome() {
		TestChromosome c = new TestChromosome();
		c.setTestCase(getRandomTestCase(Properties.CHROMOSOME_LENGTH));
		return c;
	}

	protected TestCase getNewTestCase() {
		return new DefaultTestCase();
	}

}
