package generator.strategy;

import generator.Properties;
import generator.coverage.FitnessFunctions;
import generator.ga.stoppingconditions.*;
import generator.rmi.ClientServices;
import generator.statistics.RuntimeVariable;
import generator.testcase.TestFitnessFunction;
import generator.testsuite.TestSuiteChromosome;
import generator.testsuite.TestSuiteFitnessFunction;
import generator.utils.LoggingUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class TestGenerationStrategy {

	/**
	 * Generate a set of tests; assume that all analyses are already completed
	 * @return
	 */
	public abstract TestSuiteChromosome generateTests();
	/** There should only be one */
	protected ZeroFitnessStoppingCondition zeroFitness = new ZeroFitnessStoppingCondition();
	
	/** There should only be one */
	protected StoppingCondition globalTime = new GlobalTimeStoppingCondition();

    protected void sendExecutionStatistics() {
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Statements_Executed, MaxStatementsStoppingCondition.getNumExecutedStatements());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Tests_Executed, MaxTestsStoppingCondition.getNumExecutedTests());
    }
    
    /**
     * Convert criterion names to test suite fitness functions
     * @return
     */
	protected List<TestSuiteFitnessFunction> getFitnessFunctions() {
	    List<TestSuiteFitnessFunction> ffs = new ArrayList<TestSuiteFitnessFunction>();
	    for (int i = 0; i < Properties.CRITERION.length; i++) {
	    	TestSuiteFitnessFunction newFunction = FitnessFunctions.getFitnessFunction(Properties.CRITERION[i]);
	    	
	    	// If this is compositional fitness, we need to make sure
	    	// that all functions are consistently minimization or 
	    	// maximization functions
	    	if(Properties.ALGORITHM != Properties.Algorithm.NSGAII && Properties.ALGORITHM != Properties.Algorithm.SPEA2) {
	    		for(TestSuiteFitnessFunction oldFunction : ffs) {			
	    			if(oldFunction.isMaximizationFunction() != newFunction.isMaximizationFunction()) {
	    				StringBuffer sb = new StringBuffer();
	    				sb.append("* Invalid combination of fitness functions: ");
	    				sb.append(oldFunction.toString());
	    				if(oldFunction.isMaximizationFunction())
	    					sb.append(" is a maximization function ");
	    				else
	    					sb.append(" is a minimization function ");
	    				sb.append(" but ");
	    				sb.append(newFunction.toString());
	    				if(newFunction.isMaximizationFunction())
	    					sb.append(" is a maximization function ");
	    				else
	    					sb.append(" is a minimization function ");
	    				LoggingUtils.getGeneratorLogger().info(sb.toString());
	    				throw new RuntimeException("Invalid combination of fitness functions");
	    			}
	    		}
	    	}
	        ffs.add(newFunction);

	    }

		return ffs;
	}
	
//	/**
//	 * Convert criterion names to factories for test case fitness functions
//	 * @return
//	 */
//	public static List<TestFitnessFactory<? extends TestFitnessFunction>> getFitnessFactories() {
//	    List<TestFitnessFactory<? extends TestFitnessFunction>> goalsFactory = new ArrayList<TestFitnessFactory<? extends TestFitnessFunction>>();
//	    for (int i = 0; i < Properties.CRITERION.length; i++) {
//	        goalsFactory.add(FitnessFunctions.getFitnessFactory(Properties.CRITERION[i]));
//	    }
//
//		return goalsFactory;
//	}
	
	/**
	 * Check if the budget has been used up. The GA will do this check
	 * on its own, but other strategies (e.g. random) may depend on this function.
	 * 
	 * @param chromosome
	 * @param stoppingCondition
	 * @return
	 */
	protected boolean isFinished(TestSuiteChromosome chromosome, StoppingCondition stoppingCondition) {
		if (stoppingCondition.isFinished())
			return true;

		if (Properties.STOP_ZERO) {
			if (chromosome.getFitness() == 0.0)
				return true;
		}

		if (!(stoppingCondition instanceof MaxTimeStoppingCondition)) {
			if (globalTime.isFinished())
				return true;
		}

		return false;
	}
	
	/**
	 * Convert property to actual stopping condition
	 * @return
	 */
	protected StoppingCondition getStoppingCondition() {
		switch (Properties.STOPPING_CONDITION) {
		case MAXGENERATIONS:
			return new MaxGenerationStoppingCondition();
		case MAXFITNESSEVALUATIONS:
			return new MaxFitnessEvaluationsStoppingCondition();
		case MAXTIME:
			return new MaxTimeStoppingCondition();
		case MAXTESTS:
			return new MaxTestsStoppingCondition();
		case MAXSTATEMENTS:
			return new MaxStatementsStoppingCondition();
		default:
			return new MaxGenerationStoppingCondition();
		}
	}

	protected boolean canGenerateTestsForSUT() {
//		if (TestCluster.getInstance().getNumTestCalls() == 0) {
//			if(Properties.P_REFLECTION_ON_PRIVATE <= 0.0 || CFGMethodAdapter.getNumMethods(TestGenerationContext.getInstance().getClassLoaderForSUT()) == 0) {
//				return false;
//			}
//		}
		return true;
	}
}
