package generator.testcase;

import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class of all execution observers
 * 
 * @author Gordon Fraser
 */
public abstract class ExecutionObserver {

	/** The test case being monitored and executed */
	protected static TestCase currentTest = null;

	/** Constant <code>WRAPPER_TYPES</code> */
	protected static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(
	        Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class,
	                      Integer.class, Long.class, Float.class, Double.class,
	                      Void.class));

	/**
	 * <p>
	 * isWrapperType
	 * </p>
	 * 
	 * @param clazz
	 *            a {@link Class} object.
	 * @return a boolean.
	 */
	protected static boolean isWrapperType(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
	}

	public static void setCurrentTest(TestCase test) {
		currentTest = test;
	}

	public static TestCase getCurrentTest() {
		return currentTest;
	}

	/**
	 * This is called with the console output of each statement
	 *
	 * @param position
	 *            a int.
	 * @param output
	 *            a {@link String} object.
	 */
	public abstract void output(int position, String output);

	/**
	 * Called immediately before a statement is executed
	 *
	 * @param statement
	 * @param scope
	 */
	public abstract void beforeStatement(Statement statement, Scope scope);

	public abstract void afterStatement(Statement statement, Scope scope,
	        Throwable exception);


	public abstract void testExecutionFinished(ExecutionResult r, Scope s);

	/**
	 * Need a way to clear previously produced results
	 */
	public abstract void clear();

	protected Set<VariableReference> getDependentVariables(Statement statement) {
		Set<VariableReference> dependencies = new HashSet<VariableReference>();
		for (VariableReference var : statement.getVariableReferences()) {
			dependencies.add(var);
			dependencies.addAll(currentTest.getDependencies(var));
		}
		return dependencies;
	}
}
