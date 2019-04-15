package generator.coverage.output;

import generator.Properties;
import generator.coverage.input.Type;
import generator.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OutputCoverageFactory extends AbstractFitnessFactory<OutputCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(OutputCoverageFactory.class);

    @Override
    public List<OutputCoverageTestFitness> getCoverageGoals() {
        List<OutputCoverageTestFitness> goals = new ArrayList<OutputCoverageTestFitness>();

        long start = System.currentTimeMillis();
        String targetClass = Properties.TARGET_CLASS;
        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }

    public static OutputCoverageTestFitness createGoal(String className, String methodName, Type returnType, String suffix) {
        return new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType, suffix));
    }

    /**
     * Returns list of inspector methods in a given class.
     * An inspector is a cheap-pure method with no arguments.
     *
     * @param className A class name
     */
    public static List<String> getInspectors(String className) {
//        List<String> pureMethods = CheapPurityAnalyzer.getInstance().getPureMethods(className);
        List<String> inspectors = new ArrayList<>();
//        for (String pm : pureMethods) {
//            if ((Type.getArgumentTypes(pm.substring(pm.indexOf('('))).length == 0) &&
//                    ! (pm.substring(0, pm.indexOf("(")).equals("<clinit>")))
//                inspectors.add(pm);
//        }
        return inspectors;
    }
}
