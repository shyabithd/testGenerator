package generator.coverage.branch;

import generator.coverage.ControlFlowDistance;
import generator.testcase.ExecutionResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A single branch coverage goal Either true/false evaluation of a jump
 * condition, or a method entry
 * 
 * @author Gordon Fraser, Andre Mis
 */
public class BranchCoverageGoal implements Serializable, Comparable<BranchCoverageGoal> {

	private static final long serialVersionUID = 2962922303111452419L;
	
	private transient Branch branch;
	
	private final boolean value;
	private final String className;
	private final String methodName;
	
	
	/**
	 * The line number in the source code. This information is stored in the bytecode if the
	 * code was compiled in debug mode. If no info, we would get a negative value (e.g., -1) here.
	 */
	private int lineNumber;

	public int getId() {
		return branch.getActualBranchId();

	}

	public BranchCoverageGoal(Branch branch, boolean value, String className,
	        String methodName) {
		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");
		if (branch == null && !value)
			throw new IllegalArgumentException(
			        "expect goals for a root branch to always have value set to true");

		this.branch = branch;
		this.value = value;

		this.className = className;
		this.methodName = methodName;

		if (branch != null) {
			lineNumber = 0;
		}
	}

	public BranchCoverageGoal(Branch branch, boolean value, String className,
	                          String methodName, int lineNumber) {

		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");

		if (branch == null && !value)
			throw new IllegalArgumentException("expect goals for a root branch to always have value set to true");

		this.branch = branch;
		this.value = value;
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
	}

	/**
	 * Methods that have no branches don't need a cfg, so we just set the cfg to
	 * null
	 *
	 * @param className
	 *            a {@link String} object.
	 * @param methodName
	 *            a {@link String} object.
	 */
	public BranchCoverageGoal(String className, String methodName) {
		this.branch = null;
		this.value = true;

		this.className = className;
		this.methodName = methodName;
		lineNumber = 0;
	}

	/**
	 * @return the branch
	 */
	public Branch getBranch() {
		return branch;
	}

	/**
	 * @return the value
	 */
	public boolean getValue() {
		return value;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isConnectedTo(BranchCoverageGoal goal) {
		if (branch == null || goal.branch == null) {
			// one of the goals targets a root branch
			return goal.methodName.equals(methodName) && goal.className.equals(className);
		}

		// TODO map this to new CDG !

		return false;
	}

	public ControlFlowDistance getDistance(ExecutionResult result) {

		ControlFlowDistance r = ControlFlowDistanceCalculator.getDistance(result, branch, value,
				className, methodName);
		return r;
	}

	/**
	 * 
	 * @return
	 */
	public int hashCodeWithoutValue() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (branch == null ? 0 : branch.getActualBranchId());
		result = prime * result;
		result = prime * result + className.hashCode();
		result = prime * result + methodName.hashCode();
		return result;
	}
	
	// inherited from Object

	/**
	 * {@inheritDoc}
	 * 
	 * Readable representation
	 */
	@Override
	public String toString() {
		String name = className + "." + methodName + ":";
		if (branch != null) {
			name += " " + branch.toString();
			if (value)
				name += " - true";
			else
				name += " - false";
		} else
			name += " root-Branch";

		return name;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (branch == null ? 0 : branch.getActualBranchId());
		result = prime * result;
		// TODO sure you want to call hashCode() on the cfg? doesn't that take
		// long?
		// Seems redundant -- GF
		/*
		result = prime
		        * result
		        + ((branch == null) ? 0
		                : branch.getInstruction().getActualCFG().hashCode());
		                */
		result = prime * result + className.hashCode();
		result = prime * result + methodName.hashCode();
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		BranchCoverageGoal other = (BranchCoverageGoal) obj;
		// are we both root goals?
		if (this.branch == null) {
			if (other.branch != null)
				return false;
			else
				// i don't have to check for value at this point, because if
				// branch is null we are talking about the root branch here
				return this.methodName.equals(other.methodName)
				        && this.className.equals(other.className);
		}
		// well i am not, if you are we are different
		if (other.branch == null)
			return false;

		// so we both have a branch to cover, let's look at that branch and the
		// way we want it to be evaluated
		if (!this.branch.equals(other.branch))
			return false;
		else {
			return this.value == other.value;
		}
	}

	@Override
	public int compareTo(BranchCoverageGoal o) {
		int diff = lineNumber - o.lineNumber;
		if(diff == 0) {
			return 0;
			// TODO: this code in some cases leads to the violation of the compare
			// contract. I still have to figure out why - mattia
//			// Branch can only be null if this is a branchless method
//			if(branch == null || o.getBranch() == null)
//				return 0;
//			
//			// If on the same line, order by appearance in bytecode
//			return branch.getActualBranchId() - o.getBranch().getActualBranchId();
		} else {
			return diff;
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		if (branch != null)
			oos.writeInt(branch.getActualBranchId());
		else
			oos.writeInt(-1);
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		int branchId = ois.readInt();
	}

}
