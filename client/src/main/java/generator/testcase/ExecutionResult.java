package generator.testcase;

import generator.mutation.Mutation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExecutionResult implements Cloneable {

	private static final Logger logger = LoggerFactory.getLogger(ExecutionResult.class);
	
	/** Test case that produced this execution result */
	public TestCase test;

	/** Mutation that was active during execution */
	public Mutation mutation;

	/** Map statement number to raised exception */
	protected Map<Integer, Throwable> exceptions = new HashMap<Integer, Throwable>();

	/** Record for each exception if it was explicitly thrown 
	 * 
	 * <p>
	 * FIXME: internal data structures should never be null...
	 * */
	public Map<Integer, Boolean> explicitExceptions = new HashMap<Integer, Boolean>();

	/** Duration of execution */
	protected long executionTime = 0L;

	/** Number of statements executed */
	protected int executedStatements = 0;

	/** Was there a permission denied during execution? */
	protected boolean hasSecurityException = false;

	/** Set of System properties that were read during test execution */
	protected Set<String> readProperties;
	
	/**
	 * Keep track of whether any System property was written
	 */
	protected boolean wasAnyPropertyWritten;
	
	/*
	 * Regression Object Distance
	 */
	public double regressionObjectDistance = 0;
	
	/**
	 * @return the executedStatements
	 */
	public int getExecutedStatements() {
		return executedStatements;
	}

	/**
	 * @param executedStatements
	 *            the executedStatements to set
	 */
	public void setExecutedStatements(int executedStatements) {
		this.executedStatements = executedStatements;
	}

	// experiment .. tried to remember intermediately calculated ControlFlowDistances .. no real speed up
	//	public Map<Branch, ControlFlowDistance> intermediateDistances;

	public ExecutionResult(TestCase t) {
		mutation = null;
		test = t;
	}

	/**
	 * <p>
	 * Copy the input map data into internal structures
	 * </p>
	 * 
	 * @param data
	 *            a {@link Map} object. It has a mapping from test
	 *            sequence position toward thrown exception
	 */
	public void setThrownExceptions(Map<Integer, Throwable> data) {
		exceptions.clear();
		for (Integer position : data.keySet()) {
			reportNewThrownException(position, data.get(position));
		}
	}


	/**
	 * <p>
	 * getFirstPositionOfThrownException
	 * </p>
	 *
	 * @return a {@link Integer} object.
	 */
	public Integer getFirstPositionOfThrownException() {
		Integer min = null;
		for (Integer position : exceptions.keySet()) {
			if (min == null || position < min) {
				min = position;
			}
		}
		return min;
	}

	/**
	 * <p>
	 * reportNewThrownException
	 * </p>
	 *
	 * @param position
	 *            a {@link Integer} object.
	 * @param t
	 *            a {@link Throwable} object.
	 */
	public void reportNewThrownException(Integer position, Throwable t) {
		exceptions.put(position, t);
	}

	/**
	 * <p>
	 * getPositionsWhereExceptionsWereThrown
	 * </p>
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getPositionsWhereExceptionsWereThrown() {
		return exceptions.keySet();
	}

	/**
	 * <p>
	 * getAllThrownExceptions
	 * </p>
	 *
	 * @return a {@link Collection} object.
	 */
	public Collection<Throwable> getAllThrownExceptions() {
		return exceptions.values();
	}

	/**
	 * <p>
	 * isThereAnExceptionAtPosition
	 * </p>
	 *
	 * @param position
	 *            a {@link Integer} object.
	 * @return a boolean.
	 */
	public boolean isThereAnExceptionAtPosition(Integer position) {
		return exceptions.containsKey(position);
	}

	/**
	 * <p>
	 * noThrownExceptions
	 * </p>
	 *
	 * @return a boolean.
	 */
	public boolean noThrownExceptions() {
		return exceptions.isEmpty();
	}

	/**
	 * <p>
	 * getExceptionThrownAtPosition
	 * </p>
	 *
	 * @param position
	 *            a {@link Integer} object.
	 * @return a {@link Throwable} object.
	 */
	public Throwable getExceptionThrownAtPosition(Integer position) {
		return exceptions.get(position);
	}

	/**
	 * <p>
	 * getNumberOfThrownExceptions
	 * </p>
	 *
	 * @return a int.
	 */
	public int getNumberOfThrownExceptions() {
		return exceptions.size();
	}

	/**
	 * shouldn't be used
	 *
	 * @return a {@link Map} object.
	 */
	@Deprecated
	public Map<Integer, Throwable> exposeExceptionMapping() {
		return exceptions;
	}

	/**
	 *
	 * @return Mapping of statement indexes and thrown exceptions.
	 */
	public Map<Integer, Throwable> getCopyOfExceptionMapping() {
		Map<Integer, Throwable> copy = new HashMap<Integer, Throwable>();
		copy.putAll(exceptions);
		return copy;
	}

	public ExecutionResult(TestCase t, Mutation m) {
		mutation = m;
		test = t;
	}
	/**
	 * Does the test contain an exception caused in the test itself?
	 * 
	 * @return a boolean.
	 */
	public boolean hasTestException() {
		if (test == null)
			return false;

		for (Throwable t : exceptions.values()) {
			if (t instanceof CodeUnderTestException)
				return true;
		}

		return false;
	}

	/**
	 * Is there an undeclared exception in the trace?
	 * 
	 * @return a boolean.
	 */
	public boolean hasUndeclaredException() {
		if (test == null)
			return false;

		for (Integer i : exceptions.keySet()) {
			Throwable t = exceptions.get(i);
			// Exceptions can be placed at test.size(), e.g. for timeouts
			assert i>=0 && i<=test.size() : "Exception "+t+" at position "+i+" in test of length "+test.size()+": "+test.toCode(exceptions);
			if(i >= test.size())
				continue;
			
			if (!test.getStatement(i).getDeclaredExceptions().contains(t.getClass()))
				return true;
		}

		return false;
	}

	/**
	 * check if the test case threw any security exception
	 * 
	 * @return
	 */
	public boolean hasSecurityException() {
		return hasSecurityException;
	}

	public void setSecurityException(boolean value) {
		logger.debug("Changing hasSecurityException from "+hasSecurityException +" to "+value);
		hasSecurityException = value;
	}

	/**
	 * @return the executionTime
	 */
	public long getExecutionTime() {
		return executionTime;
	}

	/**
	 * @param executionTime
	 *            the executionTime to set
	 */
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult clone() {
		ExecutionResult copy = new ExecutionResult(test, mutation);
		copy.exceptions.putAll(exceptions);
		copy.explicitExceptions.putAll(explicitExceptions);
		copy.executionTime = executionTime;
		copy.regressionObjectDistance = regressionObjectDistance;
		if(readProperties!=null){
			copy.readProperties = new LinkedHashSet<String>();
			copy.readProperties.addAll(readProperties);
		}
		copy.wasAnyPropertyWritten = wasAnyPropertyWritten;

		return copy;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String result = "";
		result += "Trace:";
		return result;
	}

	public Set<String> getReadProperties() {
		return readProperties;
	}

	public void setReadProperties(Set<String> readProperties) {
		this.readProperties = readProperties;
	}

	public boolean wasAnyPropertyWritten() {
		return wasAnyPropertyWritten;
	}

	public void setWasAnyPropertyWritten(boolean wasAnyPropertyWritten) {
		this.wasAnyPropertyWritten = wasAnyPropertyWritten;
	}

	public void setTest(TestCase tc) {
		this.test = tc;
	}

}
