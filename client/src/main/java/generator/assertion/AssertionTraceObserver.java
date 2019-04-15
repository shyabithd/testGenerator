package generator.assertion;

import generator.Properties;
import generator.testcase.CodeUnderTestException;
import generator.testcase.ExecutionObserver;
import generator.testcase.ExecutionTracer;
import generator.testcase.Scope;
import generator.testcase.statement.FieldStatement;
import generator.testcase.statement.MethodStatement;
import generator.testcase.statement.PrimitiveStatement;
import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * <p>
 * Abstract AssertionTraceObserver class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class AssertionTraceObserver<T extends OutputTraceEntry> extends
		ExecutionObserver {

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(AssertionTraceObserver.class);

	protected OutputTrace<T> trace = new OutputTrace<T>();

	protected boolean checkThread() {
		return ExecutionTracer.isThreadNeqCurrentThread();
	}
	
	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public synchronized void output(int position, String output) {
		// Default behavior is to ignore console output

	}


	protected void visitDependencies(Statement statement, Scope scope) {
		Set<VariableReference> dependencies = currentTest.getDependencies(statement.getReturnValue());
		
		if(Properties.isRegression()){
			if (!hasCUT(statement, dependencies)){
				return;
			}
		}
		
		for (VariableReference var : dependencies) {
			if(var.isVoid())
				continue;

			if (!var.isVoid()) {
				try {
					visit(statement, scope, var);
				} catch (CodeUnderTestException e) {
					// ignore
				}
			}
		}
	}

	protected void visitReturnValue(Statement statement, Scope scope) {
		
		if(Properties.isRegression()){
			Set<VariableReference> dependencies = currentTest.getDependencies(statement.getReturnValue());
			if (!hasCUT(statement, dependencies)){
				return;
			}
		}
		
		if (statement.getReturnClass().equals(void.class))
			return;
		
		// No need to assert anything about values just assigned
		if(statement.isAssignmentStatement())
			return;

		try {
			visit(statement, scope, statement.getReturnValue());
		} catch (CodeUnderTestException e) {
			// ignore
		}

	}
	
	
	/*
	 * Whether or not the target has the class under test.
	 * This is to avoid generating assertions for statements
	 * that are not assignable from the CUT.
	 */
	private boolean hasCUT(Statement statement, Set<VariableReference> dependencies){
		boolean hasCUT = false;
		if (statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
//			if (Properties
//					.getTargetClassRegression(
//							ms.getMethod().getDeclaringClass().getClassLoader() == TestGenerationContext.getInstance()
//									.getClassLoaderForSUT()).isAssignableFrom(
//							ms.getMethod().getDeclaringClass()))
				hasCUT = true;
		}
		for (VariableReference var : dependencies) {
//			if (Properties
//					.getTargetClassRegression(
//							var.getVariableClass().getClassLoader() == TestGenerationContext.getInstance()
//									.getClassLoaderForSUT()).isAssignableFrom(
//							var.getVariableClass())) {
//				hasCUT = true;
//				break;
//			}
		}
		return hasCUT;
	}

	protected abstract void visit(Statement statement, Scope scope,
	        VariableReference var) throws CodeUnderTestException;

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#statement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public synchronized void afterStatement(Statement statement, Scope scope,
	        Throwable exception) {
		//if(checkThread())
		//	return;

        // No assertions for primitives
        if(statement instanceof PrimitiveStatement<?>)
            return;


        // By default, no assertions are created for statements that threw exceptions
		if(exception != null)
			return;

		if(statement instanceof FieldStatement) {
			// Only need to check returnvalue here, nothing else can have changed
			visitReturnValue(statement, scope);
		}
		else {
			visitDependencies(statement, scope);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope)
	 */
	@Override
	public synchronized void beforeStatement(Statement statement, Scope scope) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#clear()
	 */
	/** {@inheritDoc} */
	@Override
	public synchronized void clear() {
		//if(!checkThread())
		//	return;

		trace.clear();
	}

	public synchronized OutputTrace<T> getTrace() {
		return trace.clone();
	}

}
