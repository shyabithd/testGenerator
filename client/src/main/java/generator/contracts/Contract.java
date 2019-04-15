package generator.contracts;

import generator.Properties;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.statement.ConstructorStatement;
import generator.testcase.statement.FieldStatement;
import generator.testcase.statement.MethodStatement;
import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Based on ObjectContract / Randoop
 * 
 * @author Gordon Fraser
 */
public abstract class Contract {

	protected static final Logger logger = LoggerFactory.getLogger(Contract.class);

	protected static class Pair<T> {
		T object1;
		T object2;

		public Pair(T o1, T o2) {
			object1 = o1;
			object2 = o2;
		}
	}

	protected Collection<Object> getAllObjects(Scope scope) {
		// TODO: Assignable classes and subclasses?
		final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
		return scope.getObjects(targetClass);
	}

	protected Collection<VariableReference> getAllVariables(Scope scope) {
		final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
		return scope.getElements(targetClass);
	}

	protected Collection<Pair<Object>> getAllObjectPairs(Scope scope) {
		Set<Pair<Object>> pairs = new HashSet<Pair<Object>>();
		Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
		for (Object o1 : scope.getObjects(targetClass)) {
			for (Object o2 : scope.getObjects(o1.getClass())) {
				pairs.add(new Pair<Object>(o1, o2));
			}
		}
		return pairs;
	}

	protected Collection<Pair<VariableReference>> getAllVariablePairs(Scope scope) {
		Set<Pair<VariableReference>> pairs = new HashSet<Pair<VariableReference>>();
		final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
		List<VariableReference> objects = scope.getElements(targetClass);
		for (int i = 0; i < objects.size(); i++) {
			for (int j = i; j < objects.size(); j++) {
				pairs.add(new Pair<VariableReference>(objects.get(i), objects.get(j)));
			}
		}
		//		for (VariableReference o1 : scope.getElements(Properties.getTargetClass())) {
		//			for (VariableReference o2 : scope.getElements(o1.getVariableClass())) {
		//				pairs.add(new Pair<VariableReference>(o1, o2));
		//			}
		//		}
		return pairs;
	}

	protected boolean isTargetStatement(Statement statement) {
		//if (statement.getReturnClass().equals(Properties.getTargetClass()))
		//	return true;
		if (statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
			final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
			if (targetClass.equals(ms.getMethod().getDeclaringClass()))
				return true;
		} else if (statement instanceof ConstructorStatement) {
			ConstructorStatement cs = (ConstructorStatement) statement;
			final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
			if (targetClass.equals(cs.getConstructor().getDeclaringClass()))
				return true;
		} else if (statement instanceof FieldStatement) {
			FieldStatement fs = (FieldStatement) statement;
			final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
			if (targetClass.equals(fs.getField().getDeclaringClass()))
				return true;
		}

		return false;
	}

	public boolean fails(TestCase test) {
//		ContractChecker.setActive(false);
//		TestCaseExecutor executor = TestCaseExecutor.getInstance();
//		SingleContractChecker checker = new SingleContractChecker(this);
//		executor.addObserver(checker);
//		TestCaseExecutor.runTest(test);
//		executor.removeObserver(checker);
//		//ContractChecker.setActive(true);
//		return !checker.isValid();
		return false;
	}

	public abstract ContractViolation check(Statement statement, Scope scope,
	        Throwable exception);

	/**
	 * Add an assertion to the statement based on the contract. The assertion
	 * should fail on a contract violation, and pass if the contract is
	 * satisfied.
	 * 
	 * @param statement
	 * @param variables
	 * @param exception
	 */
	public abstract void addAssertionAndComments(Statement statement,
												 List<VariableReference> variables, Throwable exception);

	public void changeClassLoader(ClassLoader classLoader) {
		// No-op by default
	}
}
