package generator.mutation;

public class MutationExecutionResult {

	private int numAssertions = 0;

	private double impact = 0.0;

	boolean hasTimeout = false;

	boolean hasException = false;

	public MutationExecutionResult clone() {
		MutationExecutionResult result = new MutationExecutionResult();
		result.numAssertions = numAssertions;
		result.impact = impact;
		result.hasTimeout = hasTimeout;
		result.hasException = hasException;

		return result;
	}

	/**
	 * <p>Getter for the field <code>numAssertions</code>.</p>
	 *
	 * @return the numAssertions
	 */
	public int getNumAssertions() {
		return numAssertions;
	}

	/**
	 * <p>Setter for the field <code>numAssertions</code>.</p>
	 *
	 * @param numAssertions
	 *            the numAssertions to set
	 */
	public void setNumAssertions(int numAssertions) {
		this.numAssertions = numAssertions;
	}

	/**
	 * <p>Getter for the field <code>impact</code>.</p>
	 *
	 * @return the impact
	 */
	public double getImpact() {
		return impact;
	}

	/**
	 * <p>Setter for the field <code>impact</code>.</p>
	 *
	 * @param impact
	 *            the impact to set
	 */
	public void setImpact(double impact) {
		this.impact = impact;
	}

	/**
	 * <p>hasTimeout</p>
	 *
	 * @return the hasTimeout
	 */
	public boolean hasTimeout() {
		return hasTimeout;
	}

	/**
	 * <p>Setter for the field <code>hasTimeout</code>.</p>
	 *
	 * @param hasTimeout
	 *            the hasTimeout to set
	 */
	public void setHasTimeout(boolean hasTimeout) {
		this.hasTimeout = hasTimeout;
	}

	/**
	 * <p>hasException</p>
	 *
	 * @return the hasException
	 */
	public boolean hasException() {
		return hasException;
	}

	/**
	 * <p>Setter for the field <code>hasException</code>.</p>
	 *
	 * @param hasException a boolean.
	 */
	public void setHasException(boolean hasException) {
		this.hasException = hasException;
	}
}
