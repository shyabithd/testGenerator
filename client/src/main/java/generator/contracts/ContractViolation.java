package generator.contracts;

import generator.ga.ConstructionFailedException;
import generator.testcase.DefaultTestCase;
import generator.testcase.TestCase;
import generator.testcase.TestFactory;
import generator.testcase.statement.AssignmentStatement;
import generator.testcase.statement.ConstructorStatement;
import generator.testcase.statement.MethodStatement;
import generator.testcase.statement.Statement;
import generator.testcase.variable.FieldReference;
import generator.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * ContractViolation class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ContractViolation {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ContractViolation.class);

	private final Contract contract;

	private TestCase test;

	private Statement statement;

	/**
	 * If the statement execution leads to a contract violation with an
	 * undeclared exception this is stored here
	 */
	private final Throwable exception;

	/**
	 * List of all variables involved in the contract violation
	 */
	private final List<VariableReference> variables = new ArrayList<VariableReference>();

	private boolean isMinimized = false;

	public ContractViolation(Contract contract, Statement statement,
	        Throwable exception, VariableReference... variables) {
		this.contract = contract;
		this.test = statement.getTestCase().clone();
		this.test.chop(statement.getPosition() + 1);
		((DefaultTestCase) this.test).setFailing(true);
		this.statement = this.test.getStatement(statement.getPosition());
		for (VariableReference var : variables) {
			this.variables.add(var.clone(this.test));
		}
		this.exception = exception;
	}

	protected VariableReference getVariable(int num) {
		return variables.get(num).clone(this.test);
	}

	public TestCase getTestCase() {
		return test;
	}

	public Contract getContract() {
		return contract;
	}

	public int getPosition() {
		return statement.getPosition();
	}

	public Throwable getException() {
		return exception;
	}

	public boolean isExceptionOfType(Class<?> throwableClass) {
		if(exception == null)
			return false;

		return throwableClass.equals(exception.getClass());
	}

	public boolean resultsFromMethod(String methodName) {
		if(statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
			String target = ms.getMethodName() + ms.getDescriptor();
			return target.equals(methodName);
		} else if(statement instanceof ConstructorStatement) {
			return methodName.startsWith("<init>");
		} else {
			return false;
		}
	}

	public Statement getStatement() {
		return statement;
	}

	/**
	 * Remove all statements that do not contribute to the contract violation
	 */
	public void minimizeTest() {
		if (isMinimized)
			return;

		/** Factory method that handles statement deletion */
		TestFactory testFactory = TestFactory.getInstance();

//		if (Properties.INLINE) {
//			ConstantInliner inliner = new ConstantInliner();
//			inliner.inline(test);
//		}
		TestCase origTest = test.clone();

		List<Integer> positions = new ArrayList<Integer>();

		for (VariableReference var : variables)
			positions.add(var.getStPosition());

		int oldLength = test.size();
		boolean changed = true;
		while (changed) {
			changed = false;

			for (int i = test.size() - 1; i >= 0; i--) {
				// TODO - why??
				if (i >= test.size())
					continue;
				if (positions.contains(i))
					continue;

				try {
					boolean deleted = testFactory.deleteStatement(test, i);
					if(!deleted){
						continue;
					}

					if (!contract.fails(test)) {
						test = origTest.clone();
					} else {
						changed = true;
						for (int j = 0; j < positions.size(); j++) {
							if (positions.get(j) > i) {
								positions.set(j,
								              positions.get(j)
								                      - (oldLength - test.size()));
							}
						}
						origTest = test.clone();
						oldLength = test.size();
					}
				} catch (ConstructionFailedException e) {
					test = origTest.clone();
				}
			}
		}

		statement = test.getStatement(test.size() - 1);
		for (int i = 0; i < variables.size(); i++) {
			variables.set(i, test.getStatement(positions.get(i)).getReturnValue());
		}

		contract.addAssertionAndComments(statement, variables, exception);
		isMinimized = true;
	}

	public boolean same(ContractViolation other) {

		// Same contract?
		if (!contract.getClass().equals(other.contract.getClass()))
			return false;

		// Same type of statement?
		if (!statement.getClass().equals(other.statement.getClass()))
			return false;

		// Same exception type?
		if (exception != null && other.exception != null) {
			if (!exception.getClass().equals(other.exception.getClass()))
				return false;
		}

		// Same method call / constructor?
		if (statement instanceof MethodStatement) {
			MethodStatement ms1 = (MethodStatement) statement;
			MethodStatement ms2 = (MethodStatement) other.statement;
			if (ms1.getMethod().getMethod().equals(ms2.getMethod().getMethod())) {
				return true;
			}
		} else if (statement instanceof ConstructorStatement) {
			ConstructorStatement ms1 = (ConstructorStatement) statement;
			ConstructorStatement ms2 = (ConstructorStatement) other.statement;
			if (ms1.getConstructor().getConstructor().equals(ms2.getConstructor().getConstructor())) {
				return true;
			}
		} else if (statement instanceof AssignmentStatement) {
			VariableReference var1 = statement.getReturnValue();
			VariableReference var2 = other.statement.getReturnValue();
			if (var1 instanceof FieldReference && var2 instanceof FieldReference) {
				if (((FieldReference) var1).getField().getField().equals(((FieldReference) var2).getField().getField()))
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Violated contract: " + contract + " in statement " + statement
		        + " with exception " + exception;
	}

	public void changeClassLoader(ClassLoader classLoader) {
		((DefaultTestCase) test).changeClassLoader(classLoader);
		contract.changeClassLoader(classLoader);
		this.statement = this.test.getStatement(statement.getPosition());
		for (int i = 0; i < variables.size(); i++) {
			variables.set(i, variables.get(i).clone(test));
		}
	}

}
