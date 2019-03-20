package generator.testcase.statement;

import generator.Properties;
import generator.assertion.Assertion;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.variable.VariableReference;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
 *  TODO: The length is currently stored in ArrayReference and the ArrayStatement.
 *  This is bound to lead to inconsistencies. 
 */
public class ArrayStatement extends AbstractStatement {

	private static final long serialVersionUID = -2858236370873914156L;

	protected ArrayStatement(TestCase tc) throws IllegalArgumentException {

		super(tc);
	}

	private static int[] createRandom(int dimensions) {
		int[] result = new int[dimensions];
		for (int idx = 0; idx < dimensions; idx++) {
			//result[idx] = Randomness.nextInt(Properties.MAX_ARRAY);
		}
		return result;
	}

	@Override
	public Set<Assertion> copyAssertions(TestCase newTestCase, int offset) {
		Set<Assertion> copy = new LinkedHashSet<Assertion>();
		for (Assertion a : assertions) {
			if (a == null) {
				logger.info("Assertion is null!");
				logger.info("Statement has assertions: " + assertions.size());
			} else
				copy.add(a.copy(newTestCase, offset));
		}
		return copy;
	}
	/** {@inheritDoc} */
	@Override
	public Type getReturnType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getReturnClass()
	 */
	/** {@inheritDoc} */
	@Override
	public Class<?> getReturnClass() {
		return null;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		return null;
	}

	@Override
	public VariableReference getReturnValue() {
		return null;
	}

	@Override
	public void setRetval(VariableReference newRetVal) {

	}

	@Override
	public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public final Statement clone() {
		throw new UnsupportedOperationException("Use statementInterface.clone(TestCase)");
	}

	/**
	 * <p>
	 * determineDimensions
	 * </p>
	 *
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 * @return a int.
	 */
	public static int determineDimensions(java.lang.reflect.Type type) {
		String name = type.toString().replace("class", "").trim();
		int count = 0;
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) == '[') {
				count++;
			}
		}
		return count;
	}

	private int[] lengths;

	/** {@inheritDoc} */
	@Override
	public Statement copy(TestCase newTestCase, int offset) {
		//ArrayStatement copy = new ArrayStatement(newTestCase, retval.getType(), lengths);
		// copy.assertions = copyAssertions(newTestCase, offset);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		ArrayStatement as = (ArrayStatement) s;
		if (!Arrays.equals(lengths, as.lengths))
			return false;


		// if (!Arrays.equals(variables, other.variables))
		// return false;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result =1; //retval.hashCode();
		result = prime * result + Arrays.hashCode(lengths);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#isValid()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isValid() {
		int maxAssignment = 0;
		for (Statement statement : this.tc) {
//			for (VariableReference var : statement.getVariableReferences()) {
//				if (var.getAdditionalVariableReference() == this.retval) {
//					VariableReference currentVar = var;
//					while (currentVar instanceof FieldReference) {
//						currentVar = ((FieldReference) currentVar).getSource();
//					}
//					ArrayIndex index = (ArrayIndex) currentVar;
//					maxAssignment = Math.max(maxAssignment, index.getArrayIndex());
//				}
//			}
		}
		if (maxAssignment > lengths[0]) {
			logger.warn("Max assignment = "+maxAssignment+", length = "+lengths[0]);
			return false;
		}
		return super.isValid();
	}


	/** {@inheritDoc} */
	@Override
	public boolean same(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		ArrayStatement as = (ArrayStatement) s;
		if (!Arrays.equals(lengths, as.lengths))
			return false;

			return false;

	}

	/**
	 * <p>
	 * Setter for the field <code>lengths</code>.
	 * </p>
	 * 
	 * @param lengths
	 *            an array of int.
	 */
	public void setLengths(int[] lengths) {
		this.lengths = new int[lengths.length];
		for (int i = 0; i < lengths.length; i++) {
			this.lengths[i] = lengths[i];
		}
	}

	/**
	 * <p>
	 * setSize
	 * </p>
	 * 
	 * @param size
	 *            a int.
	 */
	public void setSize(int size) {
		/// assert lengths.length == 1;
		this.lengths[0] = size;
	}

	/**
	 * <p>
	 * size
	 * </p>
	 * 
	 * @return a int.
	 */
	public int size() {
		// assert lengths.length == 1;
		return lengths[0];
	}
}
