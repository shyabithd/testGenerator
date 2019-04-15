package generator.coverage.input;

import generator.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import generator.Properties;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageFactory extends AbstractFitnessFactory<InputCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(InputCoverageFactory.class);

    @Override
    public List<InputCoverageTestFitness> getCoverageGoals() {
        List<InputCoverageTestFitness> goals = new ArrayList<InputCoverageTestFitness>();

        long start = System.currentTimeMillis();
        String targetClass = Properties.TARGET_CLASS;

        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }

    public static InputCoverageTestFitness createGoal(String className, String methodName, int argIndex, String descriptor) {
        return new InputCoverageTestFitness(new InputCoverageGoal(className, methodName, argIndex, null, descriptor));
    }
}
