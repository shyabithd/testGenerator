package generator.coverage.branch;

import generator.ClassReader;
import generator.TestGenerationContext;
import generator.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * BranchCoverageFactory class.
 * </p>
 * 
 * @author Gordon Fraser, Andre Mis
 */
public class BranchCoverageFactory extends
		AbstractFitnessFactory<BranchCoverageTestFitness> {

	private static final Logger logger = LoggerFactory.getLogger(BranchCoverageFactory.class);
	
	
	/**
	 * return coverage goals of the target class or of all the contextual branches, depending on the limitToCUT paramether
	 * @param limitToCUT
	 * @return
	 */
	private List<BranchCoverageTestFitness> computeCoverageGoals(boolean limitToCUT){
		long start = System.currentTimeMillis();
		List<BranchCoverageTestFitness> goals = new ArrayList<BranchCoverageTestFitness>();
		ClassReader classReader = TestGenerationContext.getInstance().getClassReader();
		String className = classReader.getClassName();
		for (String method : BranchPool.getInstance(classReader).getBranchlessMethods(className)) {
			goals.add(createRootBranchTestFitness(classReader.getClassName(), method));
		}
		for (String methodName : BranchPool.getInstance(classReader).knownMethods(className)) {
			for (Branch b : BranchPool.getInstance(classReader).retrieveBranchesInMethod(className, methodName)) {
				if(!b.isInstrumented()) {
					goals.add(createBranchCoverageTestFitness(b, true));
					goals.add(createBranchCoverageTestFitness(b, false));
				}
			}
		}
		// logger.info("Getting branches");
		goalComputationTime = System.currentTimeMillis() - start;
		return goals;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<BranchCoverageTestFitness> getCoverageGoals() {
		return computeCoverageGoals(true);
	}

	public List<BranchCoverageTestFitness> getCoverageGoalsForAllKnownClasses() {
		return computeCoverageGoals(false); 
	}

	public static BranchCoverageTestFitness createBranchCoverageTestFitness(
			Branch b, boolean branchExpressionValue) {

		return new BranchCoverageTestFitness(new BranchCoverageGoal(b,
				branchExpressionValue, b.getClassName(), b.getMethodName()));
	}

	public static BranchCoverageTestFitness createRootBranchTestFitness(
			String className, String method) {

		return new BranchCoverageTestFitness(new BranchCoverageGoal(className,
				method.substring(method.lastIndexOf(".") + 1)));
	}

}

////----------
//List<String> l = new ArrayList<>();
//for (BranchCoverageTestFitness callGraphEntry : goals) {
//	l.add(callGraphEntry.toString());
//}
//File f = new File("/Users/mattia/workspaces/evosuiteSheffield/evosuite/master/evosuite-report/branchgoals.txt");
//f.delete();
//try {
//	Files.write(f.toPath(), l, Charset.defaultCharset(), StandardOpenOption.CREATE);
//} catch (IOException e) { 
//	e.printStackTrace();
//}
////---------- 