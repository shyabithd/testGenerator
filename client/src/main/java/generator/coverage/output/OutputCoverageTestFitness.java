package generator.coverage.output;

import generator.coverage.input.Type;
import generator.ga.archive.Archive;
import generator.testcase.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import generator.Properties;

import java.util.Set;

public class OutputCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 1383064944691491355L;

	protected static final Logger logger = LoggerFactory.getLogger(OutputCoverageTestFitness.class);

	/**
	 * Target goal
	 */
	private final OutputCoverageGoal goal;

	/**
	 * Constructor - fitness is specific to a method
	 *
	 * @param goal the coverage goal
	 * @throws IllegalArgumentException
	 */
	public OutputCoverageTestFitness(OutputCoverageGoal goal) throws IllegalArgumentException {
		if (goal == null) {
			throw new IllegalArgumentException("goal cannot be null");
		}
		this.goal = goal;
		// add the observer to TestCaseExecutor if it is not included yet
		boolean hasObserver = false;
		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		for (ExecutionObserver ob : executor.getExecutionObservers()){
			if (ob instanceof  OutputObserver){
				hasObserver = true;
				break;
			}
		}
		if (!hasObserver){
			OutputObserver observer = new OutputObserver();
			executor.addObserver(observer);
			logger.info("Added observer for output coverage");
		}
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getClassName() {
		return goal.getClassName();
	}

	/**
	 * <p>
	 * getMethod
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getMethod() {
		return goal.getMethodName();
	}

	/**
	 * <p>
	 * getValue
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public Type getType() {
		return goal.getType();
	}

	/**
	 * <p>
	 * getValueDescriptor
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getValueDescriptor() {
		return goal.getValueDescriptor();
	}

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 1.0;

		for(Set<OutputCoverageGoal> coveredGoals : result.getOutputGoals().values()) {
			if (!coveredGoals.contains(this.goal)) {
				continue;
			}

			for (OutputCoverageGoal coveredGoal : coveredGoals) {
				if (coveredGoal.equals(this.goal)) {
					double distance = this.calculateDistance(coveredGoal);
					if (distance < 0.0) {
						continue;
					} else {
						fitness = distance;
						break;
					}
				}
			}
		}

		assert fitness >= 0.0;
		updateIndividual(this, individual, fitness);

		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}

		if (Properties.TEST_ARCHIVE) {
			Archive.getArchiveInstance().updateArchive(this, individual, fitness);
		}

		return fitness;
	}

	private double calculateDistance(OutputCoverageGoal coveredGoal) {

		return 0.0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "[Output]: "+goal.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int iConst = 13;
		return 51 * iConst + goal.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutputCoverageTestFitness other = (OutputCoverageTestFitness) obj;
		return this.goal.equals(other.goal);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof OutputCoverageTestFitness) {
			OutputCoverageTestFitness otherOutputFitness = (OutputCoverageTestFitness) other;
			return goal.compareTo(otherOutputFitness.goal);
		}
		return compareClassName(other);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return getMethod();
	}

	/*
	 * TODO: Move somewhere else into a utility class
	 */
	private static final Class<?> getClassForName(String type)
	{
		try
		{
			if( type.equals("boolean"))
			{
				return Boolean.TYPE;
			}
			else if(type.equals("byte"))
			{
				return Byte.TYPE;
			}
			else if( type.equals("char"))
			{
				return Character.TYPE;
			}
			else if( type.equals("double"))
			{
				return Double.TYPE;
			}
			else if(type.equals("float"))
			{
				return Float.TYPE;
			}
			else if(type.equals("int"))
			{
				return Integer.TYPE;
			}
			else if( type.equals("long"))
			{
				return Long.TYPE;
			}
			else if(type.equals("short"))
			{
				return Short.TYPE;
			}
			else if(type.equals("String") ||type.equals("Boolean") || type.equals("Short") ||type.equals("Long") ||
					type.equals("Integer") || type.equals("Float") || type.equals("Double") ||type.equals("Byte") ||
					type.equals("Character") )
			{
				return Class.forName("java.lang." + type);
			}

			//			if(type.endsWith(";") && ! type.startsWith("["))
			//			{
			//				type = type.replaceFirst("L", "");
			//				type = type.replace(";", "");
			//			}

			if(type.endsWith("[]"))
			{
				type = type.replace("[]", "");
				return Class.forName("[L" + type + ";");
			}
			else
			{
				return Class.forName(type);
			}
		}
		catch (final ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
}
