package generator.coverage.branch;

import java.io.Serializable;

public class Branch implements Serializable, Comparable<Branch> {

	private static final long serialVersionUID = -4732587925060748263L;

	private final int actualBranchId;

	private boolean isSwitch = false;

	private String className;
	private String methodName;

	// for switch branches this value indicates to which case of the switch this
	// branch belongs. if this value is null and this is in fact a switch this
	// means this branch is the default: case of that switch
	private Integer targetCaseValue = null;


	/** Keep track of branches that were introduced as part of TT */
	private boolean isInstrumented = false;

	public String getClassName() { return className; }
	public String getMethodName() { return methodName; }
	public Branch(String className, String methodName, int lineNo) {
		this.className = className;
		this.methodName = methodName;
		this.actualBranchId = lineNo;
	}

	/**
	 * <p>
	 * Getter for the field <code>actualBranchId</code>.
	 * </p>
	 *
	 * @return a int.
	 */
	public int getActualBranchId() {
		return actualBranchId;
	}

	/**
	 * <p>
	 * isDefaultCase
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean isDefaultCase() {
		return isSwitch && targetCaseValue == null;
	}

	/**
	 * <p>
	 * isActualCase
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean isActualCase() {
		return isSwitch && targetCaseValue != null;
	}

	/**
	 * <p>
	 * Getter for the field <code>targetCaseValue</code>.
	 * </p>
	 *
	 * @return a {@link Integer} object.
	 */
	public Integer getTargetCaseValue() {
		// in order to avoid confusion when targetCaseValue is null
		if (!isSwitch)
			throw new IllegalStateException(
			        "method only allowed to be called on non-switch-Branches");

		return targetCaseValue; // null for default case
	}

	/**
	 * <p>
	 * isSwitchCaseBranch
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isSwitchCaseBranch() {
		return isSwitch;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actualBranchId;
		//result = prime * result + ((instruction == null) ? 0 : instruction.hashCode());
		result = prime * result + (isSwitch ? 1231 : 1237);
		result = prime * result
		        + ((targetCaseValue == null) ? 0 : targetCaseValue.hashCode());
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
		Branch other = (Branch) obj;
		if (actualBranchId != other.actualBranchId)
			return false;
//		if (instruction == null) {
//			if (other.instruction != null)
//				return false;
//		} else if (!instruction.equals(other.instruction))
//			return false;
		if (isSwitch != other.isSwitch)
			return false;
		if (targetCaseValue == null) {
			if (other.targetCaseValue != null)
				return false;
		} else if (!targetCaseValue.equals(other.targetCaseValue))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
//		String r = "I" + instruction.getInstructionId();
//		r += " Branch " + getActualBranchId();
//		r += " " + instruction.getInstructionType();
//		if (isSwitch) {
//			r += " L" + instruction.getLineNumber();
//			if (targetCaseValue != null)
//				r += " Case " + targetCaseValue;
//			else
//				r += " Default-Case";
//		} else
//			r += " L" + instruction.getLineNumber();
		String r="";
		return r;
	}

	/**
	 * <p>
	 * isInstrumented
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isInstrumented() {
		return isInstrumented;
	}

	/**
	 * <p>
	 * setInstrumented
	 * </p>
	 * 
	 * @param isInstrumented
	 *            a boolean.
	 */
	public void setInstrumented(boolean isInstrumented) {
		this.isInstrumented = isInstrumented;
	}

	@Override
	public int compareTo(Branch o) {

		return 0;
	}
}
