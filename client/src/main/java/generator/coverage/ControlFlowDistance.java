package generator.coverage;

import generator.ga.FitnessFunction;

/**
 * <p>ControlFlowDistance class.</p>
 *
 * @author Gordon Fraser, Andre Mis
 */
public class ControlFlowDistance implements Comparable<ControlFlowDistance> {

	// TODO make private and redirect all accesses to setter and getter - was
	// too lazy to do concurrency and mutation package right now
	public int approachLevel;
	public double branchDistance;

	/**
	 * Creates the 0-distance, meaning a distance having approachLevel and
	 * branchDistance set to 0
	 */
	public ControlFlowDistance() {
		approachLevel = 0;
		branchDistance = 0.0;
	}

	/**
	 * Can be used to create an arbitrary ControlFlowDistance
	 *
	 * However approachLevel and branchDistance are expected to be positive
	 *
	 * @param approachLevel a int.
	 * @param branchDistance a double.
	 */
	public ControlFlowDistance(int approachLevel, double branchDistance) {
		if (approachLevel < 0 || branchDistance < 0.0)
			throw new IllegalStateException(
			        "expect approachLevel and branchDistance to always be positive");

		this.approachLevel = approachLevel;
		this.branchDistance = branchDistance;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(ControlFlowDistance o) {
		ControlFlowDistance d = o;
		if (approachLevel < d.approachLevel)
			return -1;
		else if (approachLevel > d.approachLevel)
			return 1;
		else {
			if (branchDistance < d.branchDistance)
				return -1;
			else if (branchDistance > d.branchDistance)
				return 1;
			else
				return 0;
		}
	}

	/**
	 * <p>increaseApproachLevel</p>
	 */
	public void increaseApproachLevel() {
		approachLevel++;
		if (approachLevel < 0)
			throw new IllegalStateException(
			        "expect approach Level to always be positive - overflow?");
	}

	/**
	 * <p>Getter for the field <code>approachLevel</code>.</p>
	 *
	 * @return a int.
	 */
	public int getApproachLevel() {
		return approachLevel;
	}

	/**
	 * <p>Setter for the field <code>approachLevel</code>.</p>
	 *
	 * @param approachLevel a int.
	 */
	public void setApproachLevel(int approachLevel) {
		if (approachLevel < 0)
			throw new IllegalArgumentException(
			        "expect approachLevel to always be positive");

		this.approachLevel = approachLevel;
	}

	/**
	 * <p>Getter for the field <code>branchDistance</code>.</p>
	 *
	 * @return a double.
	 */
	public double getBranchDistance() {
		return branchDistance;
	}

	/**
	 * <p>Setter for the field <code>branchDistance</code>.</p>
	 *
	 * @param branchDistance a double.
	 */
	public void setBranchDistance(double branchDistance) {
		if (branchDistance < 0.0)
			throw new IllegalArgumentException("expect branchDistance to be positive");

		this.branchDistance = branchDistance;
	}

	/**
	 * <p>getResultingBranchFitness</p>
	 *
	 * @return a double.
	 */
	public double getResultingBranchFitness() {

		return approachLevel + FitnessFunction.normalize(branchDistance);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Approach = " + approachLevel + ", branch distance = " + branchDistance;
	}
}
