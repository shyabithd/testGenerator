package generator.testcase;

import java.util.ArrayList;
import java.util.List;

public class MethodCall implements Cloneable {
	public String className;
	public String methodName;
	public List<Integer> lineTrace;
	public List<Integer> branchTrace;
	public List<Double> trueDistanceTrace;
	public List<Double> falseDistanceTrace;
	public List<Integer> defuseCounterTrace;
	public int methodId;
	public int callingObjectID;
	public int callDepth;

	/**
	 * <p>Constructor for MethodCall.</p>
	 *
	 * @param className a {@link String} object.
	 * @param methodName a {@link String} object.
	 * @param methodId a int.
	 * @param callingObjectID a int.
	 * @param callDepth a int.
	 */
	public MethodCall(String className, String methodName, int methodId,
                      int callingObjectID, int callDepth) {
		this.className = className;
		this.methodName = methodName;
		lineTrace = new ArrayList<Integer>();
		branchTrace = new ArrayList<Integer>();
		trueDistanceTrace = new ArrayList<Double>();
		falseDistanceTrace = new ArrayList<Double>();
		defuseCounterTrace = new ArrayList<Integer>();
		this.methodId = methodId;
		this.callingObjectID = callingObjectID;
		this.callDepth = callDepth;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(className);
		ret.append(":");
		ret.append(methodName);
		ret.append("\n");
		// ret.append("Lines: ");
		// for(Integer line : line_trace) {
		// ret.append(" "+line);
		// }
		// ret.append("\n");
		ret.append("Branches: ");
		for (Integer branch : branchTrace) {
			ret.append(" " + branch);
		}
		ret.append("\n");
		ret.append("True Distances: ");
		for (Double distance : trueDistanceTrace) {
			ret.append(" " + distance);
		}
		ret.append("\nFalse Distances: ");
		for (Double distance : falseDistanceTrace) {
			ret.append(" " + distance);
		}
		ret.append("\n");
		return ret.toString();
	}

	/**
	 * <p>explain</p>
	 *
	 * @return a {@link String} object.
	 */
	public String explain() {
		// TODO StringBuilder-explain() functions to construct string templates like explainList()
		StringBuffer r = new StringBuffer();
		r.append(className);
		r.append(":");
		r.append(methodName);
		r.append("\n");
		r.append("Lines: ");
		if (lineTrace == null) {
			r.append("null");
		} else {
			for (Integer line : lineTrace) {
				r.append("\t" + line);
			}
			r.append("\n");
		}
		r.append("Branches: ");
		if (branchTrace == null) {
			r.append("null");
		} else {
			for (Integer branch : branchTrace) {
				r.append("\t" + branch);
			}
			r.append("\n");
		}
		r.append("True Distances: ");
		if (trueDistanceTrace == null) {
			r.append("null");
		} else {
			for (Double distance : trueDistanceTrace) {
				r.append("\t" + distance);
			}
			r.append("\n");
		}
		r.append("False Distances: ");
		if (falseDistanceTrace == null) {
			r.append("null");
		} else {
			for (Double distance : falseDistanceTrace) {
				r.append("\t" + distance);
			}
			r.append("\n");
		}
		r.append("DefUse Trace:");
		if (defuseCounterTrace == null) {
			r.append("null");
		} else {
			for (Integer duCounter : defuseCounterTrace) {
				r.append("\t" + duCounter);
			}
			r.append("\n");
		}
		return r.toString();
	}

	/** {@inheritDoc} */
	@Override
	public MethodCall clone() {
		MethodCall copy = new MethodCall(className, methodName, methodId,
		        callingObjectID, callDepth);
		copy.lineTrace = new ArrayList<Integer>(lineTrace);
		copy.branchTrace = new ArrayList<Integer>(branchTrace);
		copy.trueDistanceTrace = new ArrayList<Double>(trueDistanceTrace);
		copy.falseDistanceTrace = new ArrayList<Double>(falseDistanceTrace);
		copy.defuseCounterTrace = new ArrayList<Integer>(defuseCounterTrace);
		return copy;
	}
}
