package generator.coverage;

import generator.Properties;
import generator.coverage.branch.BranchCoverageFactory;
import generator.coverage.branch.BranchCoverageSuiteFitness;
import generator.coverage.input.InputCoverageFactory;
import generator.coverage.mutation.MutationFactory;
import generator.coverage.output.OutputCoverageFactory;
import generator.testcase.TestFitnessFunction;
import generator.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static generator.Properties.Criterion.*;

public class FitnessFunctions {

    private static Logger logger = LoggerFactory.getLogger(FitnessFunctions.class);

    public static TestSuiteFitnessFunction getFitnessFunction(Properties.Criterion criterion) {
        switch (criterion) {
            case BRANCH:
                return new BranchCoverageSuiteFitness();
            default:
                return new BranchCoverageSuiteFitness();
        }
    }

    public static TestFitnessFactory<? extends TestFitnessFunction> getFitnessFactory(Properties.Criterion criterion) {
        switch (criterion) {
            case WEAKMUTATION:
                return new MutationFactory(false);
            case BRANCH:
                return new BranchCoverageFactory();
//            case EXCEPTION:
//                return new ExceptionCoverageFactory();
//            case METHOD:
//                return new MethodCoverageFactory();
//            case METHODNOEXCEPTION:
//                return new MethodNoExceptionCoverageFactory();
//            case LINE:
//                return new LineCoverageFactory();
//            case ONLYLINE:
//                return new LineCoverageFactory();
            case OUTPUT:
                return new OutputCoverageFactory();
            case INPUT:
                return new InputCoverageFactory();
            default:
                logger.warn("No TestFitnessFactory defined for " + criterion
                        + " using default one (BranchCoverageFactory)");
                return new BranchCoverageFactory();
        }
    }
}
