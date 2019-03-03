package generator.testcase;

import generator.assertion.Assertion;
import generator.ga.ConstructionFailedException;
import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;
import generator.utils.Listenable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TestCase extends Iterable<Statement>, Cloneable, Listenable<Void> {

	/**
	 * Get an unique id representing this test.
	 * This is mainly useful for debugging.
	 *
	 * @return
	 */
	public int getID();

	public void accept(TestVisitor visitor);

	/**
	 * Copy all the assertions from other test case
	 *
	 * @param other
	 *            The other test case
	 */
	public void addAssertions(TestCase other);

	/**
	 * Append new statement at end of test case
	 *
	 * @param statement
	 *            New statement
	 * @return VariableReference of return value
	 */
	public VariableReference addStatement(Statement statement);

	/**
	 * Add new statement at position and fix following variable references
	 *
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement. Notice that the test might choose to
	 *         modify the statement you inserted. You should use the returned
	 *         variable reference and not use references
	 */
	public VariableReference addStatement(Statement statement, int position);

	/**
	 * <p>addStatements</p>
	 *
	 * @param statements a {@link List} object.
	 */
	public void addStatements(List<? extends Statement> statements);

	/**
	 * Remove all statements after a given position
	 *
	 * @param length
	 *            Length of the test case after chopping
	 */
	public void chop(int length);

	public int sliceFor(VariableReference var);

	/**
	 * Remove all covered goals
	 */
	public void clearCoveredGoals();


	public boolean contains(Statement statement);

	public TestCase clone();

	/**
	 * Determine the set of classes that are accessed by the test case
	 *
	 * @return Set of accessed classes
	 */
	public Set<Class<?>> getAccessedClasses();

	/**
	 * Get all assertions that exist for this test case
	 *
	 * @return List of assertions
	 *
	 *         TODO: Also return ExceptionAssertion?
	 */
	public List<Assertion> getAssertions();

	/**
	 * <p>getDeclaredExceptions</p>
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Class<?>> getDeclaredExceptions();

	/**
	 * Determine the set of variables that var depends on
	 *
	 * @param var
	 *            Variable to check for
	 * @return Set of dependency variables
	 */
	public Set<VariableReference> getDependencies(VariableReference var);

	/**
	 * Get the last object of the defined type
	 *
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference getLastObject(Type type)
			throws ConstructionFailedException;

	/**
	 * Get the last object of the defined type
	 *
	 * @param type
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference getLastObject(Type type, int position)
			throws ConstructionFailedException;

	/**
	 * Get all objects up to position satisfying constraint
	 *
	 * @param position a int.
	 * @return a {@link List} object.
	 */
	public List<VariableReference> getObjects(int position);

	/**
	 * Get all objects up to position satisfying constraint
	 *
	 * @param type a {@link Type} object.
	 * @param position a int.
	 * @return a {@link List} object.
	 */
	public List<VariableReference> getObjects(Type type, int position);

	public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position)
	        throws ConstructionFailedException;

	public VariableReference getRandomNonNullObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * Get a random object matching type
	 *
	 * @return Random object
	 * @throws ConstructionFailedException if any.
	 */
	public VariableReference getRandomObject();

	public VariableReference getRandomObject(int position);

	public VariableReference getRandomObject(Type type)
	        throws ConstructionFailedException;

	public VariableReference getRandomObject(Type type, int position)
	        throws ConstructionFailedException;

	/**
	 * Determine the set of variables that depend on var
	 *
	 * @param var
	 *            Variable to check for
	 * @return Set of dependent variables
	 */
	public Set<VariableReference> getReferences(VariableReference var);

	public VariableReference getReturnValue(int position);

	/**
	 * Access statement by index
	 *
	 * @param position
	 *            Index of statement
	 * @return Statement at position
	 */
	public Statement getStatement(int position);

	/**
	 * Check if there is a statement at the given position.
	 *
	 * @param position
	 *            Index of statement
	 * @return Whether or not there is a statement at the given position.
	 */
	public boolean hasStatement(int position);

	/**
	 * Check if there are any assertions
	 *
	 * @return True if there are assertions
	 */
	public boolean hasAssertions();

	/**
	 * <p>hasCastableObject</p>
	 *
	 * @param type a {@link Type} object.
	 * @return a boolean.
	 */
	public boolean hasCastableObject(Type type);

	/**
	 * Check if the test case has an object of a given class
	 *
	 * @param type
	 *            Type to look for
	 * @param position
	 *            Upper bound up to which the test is checked
	 * @return True if there is something usable
	 */
	public boolean hasObject(Type type, int position);

	/**
	 * Check if var is referenced after its definition
	 *
	 * @param var
	 *            Variable to check for
	 * @return True if there is a use of var
	 */
	public boolean hasReferences(VariableReference var);


	/**
	 * Check if all methods/fields accessed are accessible also for the current SUT
	 *
	 * @return
	 */
	public boolean isAccessible();

	/**
	 * <p>isEmpty</p>
	 *
	 * @return true if size()==0
	 */
	public boolean isEmpty();

	public boolean isFailing();

	public void setFailing();

	/**
	 * Check if this test case is a prefix of t
	 *
	 * <p>
	 * A test case {@code A} is a prefix of a test case {@code B} if
	 * and only if the first {@code length(A)} statements of {@code B} are
	 * equal to ones of {@code A}, in the same order.
	 * In other words, {@code B} can be seen as an extension of {@code A}.
	 *
	 * @param t
	 *            Test case to check against
	 * @return True if this test is a prefix of t
	 */
	public boolean isPrefix(TestCase t);

	/**
	 * A test can be unstable if its assertions fail, eg due to non-determinism,
	 * non-properly handled static variables and side effects on environment, etc
	 *
	 * @return
	 */
	public boolean isUnstable();

	/**
	 * Check if test case is valid (executable)
	 *
	 * @return a boolean.
	 */
	public boolean isValid();

	/**
	 * Remove statement at position and fix variable references
	 *
	 * @param position a int.
	 */
	public void remove(int position);

	/**
	 * Remove assertion from test case
	 */
	public void removeAssertion(Assertion assertion);

	/**
	 * Remove all assertions from test case
	 */
	public void removeAssertions();

	/**
	 * Replace a VariableReference with another one
	 *
	 * @param var1
	 *            The old variable
	 * @param var2
	 *            The new variable
	 */
	public void replace(VariableReference var1, VariableReference var2);


	/**
	 * Set new statement at position
	 *
	 * @param statement
	 *            New statement
	 * @param position
	 *            Position at which to add
	 * @return Return value of statement. Notice that the test might choose to
	 *         modify the statement you inserted. You should use the returned
	 *         variable reference and not use references
	 */
	public VariableReference setStatement(Statement statement, int position);

	/**
	 * Define whether this test case is unstable or not
	 *
	 * @param unstable
	 */
	public void setUnstable(boolean unstable);

	/**
	 * <p>size</p>
	 *
	 * @return Number of statements
	 */
	public int size();

	/**
	 * Get number of statements plus the number of assertions
	 *
	 * @return Number of statements plus number of assertions
	 */
	public int sizeWithAssertions();

	/**
	 * Get Java code representation of the test case
	 *
	 * @return Code as string
	 */
	public String toCode();

	/**
	 * Get Java code representation of the test case
	 *
	 * @return Code as string
	 * @param exceptions a {@link Map} object.
	 */
	public String toCode(Map<Integer, Throwable> exceptions);
	
}
