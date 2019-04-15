package generator.testcase.statement;

import generator.ClassReader;
import generator.assertion.Assertion;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.variable.VariableReference;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public interface Statement {

	/**
	 * Add a new assertion to statement
	 * 
	 * @param assertion
	 *            Assertion to be added
	 */
	public void addAssertion(Assertion assertion);
	
	/**
	 * A statement can have a textual comment that will be included
	 * in the JUnit output
	 * 
	 * @param comment
	 */
	public void addComment(String comment);

	public Set<VariableReference> getVariableReferences();
	public VariableReference getReturnValue();
	public void setRetval(VariableReference newRetVal);
	public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException;
	/**
	 * Class instances are bound to a class loader - if we want to reexecute a
	 * test on a different classloader we need to be able to change the class of
	 * the reflection object
	 * 
	 * @param loader
	 *            a {@link ClassLoader} object.
	 */
	public void changeClassLoader(ClassLoader loader);

	/**
	 * Create deep copy of statement
	 *
	 * @return a {@link Statement} object.
	 */
	public Statement clone();

	/**
	 * <p>
	 * clone
	 * </p>
	 *
	 * @param newTestCase
	 *            the testcase in which this statement will be inserted
	 * @return a {@link Statement} object.
	 */
	public Statement clone(TestCase newTestCase);

	/**
	 * <p>
	 * copy
	 * </p>
	 *
	 * @param newTestCase
	 *            the testcase in which this statement will be inserted
	 * @param offset
	 *            a int.
	 * @return a {@link Statement} object.
	 */
	public Statement copy(TestCase newTestCase, int offset);

	/**
	 * <p>
	 * copyAssertions
	 * </p>
	 *
	 * @param newTestCase
	 *            the testcase in which this statement will be inserted
	 * @param offset
	 *            a int.
	 * @return a {@link Set} object.
	 */
	public Set<Assertion> copyAssertions(TestCase newTestCase, int offset);

	/**
	 * {@inheritDoc}
	 *
	 * Equality check
	 */
	@Override
	public boolean equals(Object s);

	/**
	 * Get Java code representation of assertions
	 *
	 * @return String representing all assertions attached to this statement
	 */
	public String getAssertionCode();

	/**
	 * Return list of assertions
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Assertion> getAssertions();

	/**
	 * Create a string representing the statement as Java code
	 *
	 * @return a {@link String} object.
	 */
	public String getCode();

	/**
	 * Create a string representing the statement as Java code
	 *
	 * @param exception
	 *            a {@link Throwable} object.
	 * @return a {@link String} object.
	 */
	public String getCode(Throwable exception);

	/**
	 * Retrieve comment for this statement
	 * @return
	 */
	public String getComment();


	/**
	 * <p>
	 * getDeclaredExceptions
	 * </p>
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Class<?>> getDeclaredExceptions();

	/**
	 * Retrieve the number of parameters of this statement
	 *
	 * @return
	 */
	public int getNumParameters();

	/**
	 * <p>
	 * getPosition
	 * </p>
	 *
	 * @return a int.
	 */
	public int getPosition();

	/**
	 * <p>
	 * getReturnClass
	 * </p>
	 *
	 * @return Raw class of return value
	 */
	public Class<?> getReturnClass();

	/**
	 * <p>
	 * getReturnType
	 * </p>
	 *
	 * @return Generic type of return value
	 */
	public ClassReader.DataType getReturnType();

	/**
	 * Retrieve the test case this statement is part of
	 *
	 * @return
	 */
	public TestCase getTestCase();

	/**
	 * Check if there are assertions
	 *
	 * @return True if there are assertions
	 */
	public boolean hasAssertions();

	/**
	 * {@inheritDoc}
	 *
	 * Generate hash code
	 */
	@Override
	public int hashCode();

	/**
	 * Determine if the underlying reflection object is currently accessible
	 * @return
	 */
	public boolean isAccessible();

	/**
	 * Returns true if this statement should be handled as an
	 * AssignmentStatement. This method was added to allow the wrapping of
	 * AssignmentStatements (in which case "a instanceof AssignmentStatement" is
	 * no longer working)
	 *
	 * @return a boolean.
	 */
	public boolean isAssignmentStatement();

	public boolean isReflectionStatement();

	/**
	 * Tests if the throwable defined by t is declared to be thrown by the
	 * underlying type. Obviously this can only return true for methods and
	 * constructors.
	 *
	 * @param t
	 *            a {@link Throwable} object.
	 * @return a boolean.
	 */
	public boolean isDeclaredException(Throwable t);

	/**
	 * Various consistency checks. This method might also return with an
	 * assertionError Functionality might depend on the status of
	 * enableAssertions in this JVM
	 *
	 * @return a boolean.
	 */
	public boolean isValid();

	public void removeAssertion(Assertion assertion);

	/**
	 * Delete all assertions attached to this statement
	 */
	public void removeAssertions();

	/**
	 * Allows the comparing of Statements between TestCases. I.e. this is a more
	 * semantic comparison than the one done by equals. E.g. two Variable are
	 * equal if they are at the same position and they reference to objects of
	 * the same type.
	 *
	 * @param s
	 *            a {@link Statement} object.
	 * @return a boolean.
	 */
	public boolean same(Statement s);

	/**
	 * Sets the set of assertions to statement
	 *
	 * @param assertions
	 *            a {@link Set} object.
	 */
	public void setAssertions(Set<Assertion> assertions);

    boolean references(VariableReference returnValue);

	void replace(VariableReference var1, VariableReference var2);
}
