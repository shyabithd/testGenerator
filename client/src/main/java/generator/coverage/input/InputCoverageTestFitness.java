package generator.coverage.input;

import generator.Properties;
import generator.ga.archive.Archive;
import generator.testcase.*;

import java.util.Set;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 6630097528288524492L;

    /**
     * Target goal
     */
    private final InputCoverageGoal goal;

    /**
     * Constructor - fitness is specific to a method
     *
     * @param goal the coverage goal
     * @throws IllegalArgumentException
     */
    public InputCoverageTestFitness(InputCoverageGoal goal) throws IllegalArgumentException {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null");
        }
        this.goal = goal;
        // add the observer to TestCaseExecutor if it is not included yet
        boolean hasObserver = false;
        TestCaseExecutor executor = TestCaseExecutor.getInstance();
        for (ExecutionObserver ob : executor.getExecutionObservers()){
        	if (ob instanceof  InputObserver){
        		hasObserver = true;
        		break;
        	}
        }
        if (!hasObserver){
        	InputObserver observer = new InputObserver();
			executor.addObserver(observer);
			logger.info("Added observer for input coverage");
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
     * getValue
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

        for(Set<InputCoverageGoal> coveredGoals : result.getInputGoals().values()) {
            if (!coveredGoals.contains(this.goal)) {
                continue;
            }

            for (InputCoverageGoal coveredGoal : coveredGoals) {
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

    private double calculateDistance(InputCoverageGoal coveredGoal) {
      Number argValue = coveredGoal.getNumericValue();
      return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[Input]: "+goal.toString();
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
        InputCoverageTestFitness other = (InputCoverageTestFitness) obj;
        return this.goal.equals(other.goal);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof InputCoverageTestFitness) {
            InputCoverageTestFitness otherInputFitness = (InputCoverageTestFitness) other;
            return goal.compareTo(otherInputFitness.goal);
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

}
