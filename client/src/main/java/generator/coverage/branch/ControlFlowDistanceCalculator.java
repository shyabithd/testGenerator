package generator.coverage.branch;

import generator.coverage.ControlFlowDistance;
import generator.testcase.ExecutionResult;
import generator.testcase.MethodCall;
import generator.testcase.statement.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControlFlowDistanceCalculator {

	private static Logger logger = LoggerFactory.getLogger(ControlFlowDistanceCalculator.class);

	// DONE hold intermediately calculated ControlFlowDistances in
	// ExecutionResult during computation in order to speed up things -
	// experiment at least 
	// ... did that, but no real speed up observed 

	public static ControlFlowDistance getDistance(ExecutionResult result, Branch branch,
												  boolean value, String className, String methodName) {
		if (result == null || className == null || methodName == null)
			throw new IllegalArgumentException("null given");
		if (branch == null && !value)
			throw new IllegalArgumentException(
			        "expect distance for a root branch to always have value set to true");

		// if branch is null, we will just try to call the method at hand
		if (branch == null)
			return getRootDistance(result, className, methodName);

//		if(value) {
//			if (result.getTrace().getCoveredTrueBranches().contains(branch.getActualBranchId()))
//				return new ControlFlowDistance(0, 0.0);
//		}
//		else {
//			if (result.getTrace().getCoveredFalseBranches().contains(branch.getActualBranchId()))
//                return new ControlFlowDistance(0, 0.0);
//		}

		ControlFlowDistance nonRootDistance = getNonRootDistance(result, branch, value);

		if (nonRootDistance == null)
			throw new IllegalStateException(
			        "expect getNonRootDistance to never return null");

		return nonRootDistance;
	}

	private static ControlFlowDistance getTimeoutDistance(ExecutionResult result,
	        Branch branch) {

		logger.debug("Has timeout!");
		return worstPossibleDistanceForMethod(branch);
	}

	private static ControlFlowDistance worstPossibleDistanceForMethod(Branch branch) {
		ControlFlowDistance d = new ControlFlowDistance();
		if (branch == null) {
			d.setApproachLevel(20);
		}
		return d;
	}

	private static boolean hasConstructorException(ExecutionResult result,
			String className, String methodName) {

		if (result.hasTestException()
				|| result.noThrownExceptions())
			return false;

		Integer exceptionPosition = result.getFirstPositionOfThrownException();
		if(!result.test.hasStatement(exceptionPosition)){
			return false;
		}
		Statement statement = result.test.getStatement(exceptionPosition);

		return false;
	}
	
	private static ControlFlowDistance getRootDistance(ExecutionResult result,
	        String className, String methodName) {

		ControlFlowDistance d = new ControlFlowDistance();

		if(hasConstructorException(result, className, methodName)) {
			return d;
		}

		d.increaseApproachLevel();
		return d;
	}

	private static ControlFlowDistance getNonRootDistance(ExecutionResult result,
	        Branch branch, boolean value) {

		if (branch == null)
			throw new IllegalStateException(
			        "expect this method only to be called if this goal does not try to cover the root branch");

//		String className = branch.getClassName();
//		String methodName = branch.getMethodName();
//
		ControlFlowDistance r = new ControlFlowDistance();
//		r.setApproachLevel(branch.getInstruction().getActualCFG().getDiameter() + 1);
//
//		// Minimal distance between target node and path
//		for (MethodCall call : result.getTrace().getMethodCalls()) {
//			if (call.className.equals(className) && call.methodName.equals(methodName)) {
//				ControlFlowDistance d2;
//				Set<Branch> handled = new HashSet<Branch>();
//				//				result.intermediateDistances = new HashMap<Branch,ControlFlowDistance>();
//				d2 = getNonRootDistance(result, call, branch, value, className,
//				                        methodName, handled);
//				if (d2.compareTo(r) < 0) {
//					r = d2;
//				}
//			}
//		}

		return r;
	}

	private static ControlFlowDistance getNonRootDistance(ExecutionResult result,
														  MethodCall call, Branch branch, boolean value, String className,
														  String methodName, Set<Branch> handled) {

		if (branch == null)
			throw new IllegalStateException(
			        "expect getNonRootDistance() to only be called if this goal's branch is not a root branch");
		if (call == null)
			throw new IllegalArgumentException("null given");

		//		ControlFlowDistance r = result.intermediateDistances.get(branch);

		if (handled.contains(branch)) {
			//			if(r== null)
			return worstPossibleDistanceForMethod(branch);
			//			else {
			//				return r;
			//			}
		}
		handled.add(branch);

		List<Double> trueDistances = call.trueDistanceTrace;
		List<Double> falseDistances = call.falseDistanceTrace;

		// IDEA:
		// if this goal's branch is traced in the given path, return the
		// true_/false_distance, depending on this.value
		// otherwise, look at all Branches this.branch is control dependent on
		// and return 1 + minimum of the branch coverage goal distance over all
		// such branches taking as value the branchExpressionValue

		Set<Integer> branchTracePositions = determineBranchTracePositions(call, branch);

		if (!branchTracePositions.isEmpty()) {

			// branch was traced in given path
			ControlFlowDistance r = new ControlFlowDistance(0, Double.MAX_VALUE);

			for (Integer branchTracePosition : branchTracePositions)
				if (value)
					r.setBranchDistance(Math.min(r.getBranchDistance(),
					                             trueDistances.get(branchTracePosition)));
				else
					r.setBranchDistance(Math.min(r.getBranchDistance(),
					                             falseDistances.get(branchTracePosition)));

			if (r.getBranchDistance() == Double.MAX_VALUE)
				throw new IllegalStateException("should be impossible");

			//			result.intermediateDistances.put(branch, r);
			return r;
		}

		ControlFlowDistance controlDependenceDistance = getControlDependenceDistancesFor(result,
		                                                                                 call,
		                                                                                 className,
		                                                                                 methodName,
		                                                                                 handled);

		controlDependenceDistance.increaseApproachLevel();

		//		result.intermediateDistances.put(branch, controlDependenceDistance);

		return controlDependenceDistance;
	}

	private static ControlFlowDistance getControlDependenceDistancesFor(
	        ExecutionResult result, MethodCall call,
	        String className, String methodName, Set<Branch> handled) {

		Set<ControlFlowDistance> cdDistances = getDistancesForControlDependentBranchesOf(result,
		                                                                                 call,
		                                                                                 className,
		                                                                                 methodName,
		                                                                                 handled);

		if (cdDistances == null)
			throw new IllegalStateException("expect cdDistances to never be null");

		return Collections.min(cdDistances);
	}

	/**
	 * Returns a set containing the ControlFlowDistances in the given result for
	 * all branches the given instruction is control dependent on
	 * 
	 * @param handled
	 */
	private static Set<ControlFlowDistance> getDistancesForControlDependentBranchesOf(
	        ExecutionResult result, MethodCall call,
	        String className, String methodName, Set<Branch> handled) {

		Set<ControlFlowDistance> r = new HashSet<ControlFlowDistance>();

		if (r.isEmpty()) {
			// instruction only dependent on root branch
			// since this method is called by getNonRootDistance(MethodCall)
			// which in turn is only called when a MethodCall for this branch's
			// method was found in the given result, i can safely assume that
			// the 0-distance is a control dependence distance for the given
			// instruction ... right?
			r.add(new ControlFlowDistance());
		}

		return r;
	}

	private static Set<Integer> determineBranchTracePositions(MethodCall call,
	        Branch branch) {

		Set<Integer> r = new HashSet<Integer>();
		List<Integer> path = call.branchTrace;
		for (int pos = 0; pos < path.size(); pos++) {
			if (path.get(pos) == branch.getActualBranchId()) { //.getActualBranchId()); {
				r.add(pos);
			}
		}
		return r;
	}

}
