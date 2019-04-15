package generator.assertion;

import generator.Properties;
import generator.testcase.TestCase;
import generator.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class of execution traces
 * 
 * @author Gordon Fraser
 */
public class OutputTrace<T extends OutputTraceEntry> implements Cloneable {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(OutputTrace.class);

	/** One entry per statement and per variable */
	protected Map<Integer, Map<Integer, T>> trace = new HashMap<Integer, Map<Integer, T>>();

	public synchronized void addEntry(int position, VariableReference var, T entry) {
		if (!trace.containsKey(position))
			trace.put(position, new HashMap<Integer, T>());

		trace.get(position).put(var.getStPosition(), entry);
	}

	public synchronized T getEntry(int position, VariableReference var) {
		if (!trace.containsKey(position)) {
			trace.put(position, new HashMap<Integer, T>());
			return null;
		}

		if (!trace.get(position).containsKey(var.getStPosition()))
			return null;

		return trace.get(position).get(var.getStPosition());
	}

	public boolean containsEntry(int position, VariableReference var) {
		if (!trace.containsKey(position)) {
			trace.put(position, new HashMap<Integer, T>());
			return false;
		}

		if (!trace.get(position).containsKey(var.getStPosition()))
			return false;

		return true;
	}

	public boolean differs(OutputTrace<?> other) {
		for (Integer statement : trace.keySet()) {
			if (other.trace.containsKey(statement)) {
				for (Integer var : trace.get(statement).keySet()) {
					if (trace.get(statement).get(var).differs(other.trace.get(statement).get(var)))
						return true;
				}
			}
		}

		return false;
	}

	public int numDiffer(OutputTrace<?> other) {
		int num = 0;

		for (Integer statement : trace.keySet()) {
			if (other.trace.containsKey(statement)) {
				for (Integer var : trace.get(statement).keySet()) {
					if (trace.get(statement).get(var).differs(other.trace.get(statement).get(var)))
						num++;
				}
			}
		}

		return num;
	}

	public int getAssertions(TestCase test, OutputTrace<?> other) {
		int num = 0;

		for (Integer statement : trace.keySet()) {
			if (other.trace.containsKey(statement)) {
				logger.debug("Other trace contains " + statement);
				for (Integer var : trace.get(statement).keySet()) {
					logger.debug("Variable " + var);
					for (Assertion assertion : trace.get(statement).get(var).getAssertions(other.trace.get(statement).get(var))) {
						test.getStatement(statement).addAssertion(assertion);
						num++;
					}
				}
			}
		}

		return num;
	}

	public int getAllAssertions(TestCase test) {
		int num = 0;

		for (Integer statement : trace.keySet()) {
			for (Integer var : trace.get(statement).keySet()) {
				for (Assertion assertion : trace.get(statement).get(var).getAssertions()) {
					if (test.sizeWithAssertions() >= Properties.MAX_LENGTH_TEST_CASE) {
						return num;
					}
					test.getStatement(statement).addAssertion(assertion);
					num++;
				}
			}
		}

		return num;
	}

	public int getAllAssertions(TestCase test, int statement) {
		int num = 0;

		if (!trace.containsKey(statement))
			return 0;

		for (Integer var : trace.get(statement).keySet()) {
			for (Assertion assertion : trace.get(statement).get(var).getAssertions()) {
				test.getStatement(statement).addAssertion(assertion);
				num++;
			}
		}

		return num;
	}

	public boolean isDetectedBy(Assertion assertion) {

		for (Integer statement : trace.keySet()) {
			for (Integer var : trace.get(statement).keySet()) {
				if (trace.get(statement).get(var).isDetectedBy(assertion))
					return true;
			}
		}

		return false;
	}

	/**
	 * Reset the trace
	 */
	public synchronized void clear() {
		trace.clear();
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized OutputTrace<T> clone() {
		OutputTrace<T> copy = new OutputTrace<T>();
		for (Integer position : trace.keySet()) {
			copy.trace.put(position, new HashMap<Integer, T>());
			for (Integer var : trace.get(position).keySet()) {
				copy.trace.get(position).put(var,
				                             (T) trace.get(position).get(var).cloneEntry());
			}
		}
		return copy;
	}
	
	@Override
	public String toString() {
		return "Output trace of size " + trace.size();
	}
}
