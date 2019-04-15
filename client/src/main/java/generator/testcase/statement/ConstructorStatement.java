package generator.testcase.statement;

import generator.ClassReader;
import generator.testcase.CodeUnderTestException;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.TestFactory;
import generator.testcase.variable.VariableReference;
import generator.Properties;
import generator.utils.Randomness;
import generator.utils.generic.GenericConstructor;
import org.apache.commons.lang3.ClassUtils;

import javax.xml.crypto.Data;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

public class ConstructorStatement extends EntityWithParametersStatement {

	private static final long serialVersionUID = -3035570485633271957L;

	private GenericConstructor constructor;

	private static final List<String> primitiveClasses = Arrays.asList("char", "int", "short",
	                                                             "long", "boolean",
	                                                             "float", "double",
	                                                             "byte");

	public ConstructorStatement(TestCase tc, GenericConstructor constructor,
								List<VariableReference> parameters) {
		super(tc, (ClassReader.DataType) null);
		this.constructor = constructor;
	}

    public ConstructorStatement(TestCase test, GenericConstructor constructor, VariableReference retval, List<VariableReference> parameters) {
        super(test, (ClassReader.DataType) null);
    }


    /**
	 * <p>
	 * Getter for the field <code>constructor</code>.
	 * </p>
	 *
	 * @return a {@link java.lang.reflect.Constructor} object.
	 */
	public GenericConstructor getConstructor() {
		return constructor;
	}

	/**
	 * <p>
	 * Setter for the field <code>constructor</code>.
	 * </p>
	 *
	 * @param constructor
	 *            a {@link java.lang.reflect.Constructor} object.
	 */
	public void setConstructor(GenericConstructor constructor) {
		this.constructor = constructor;
	}

	/**
	 * <p>
	 * getReturnType
	 * </p>
	 *
	 * @param clazz
	 *            a {@link Class} object.
	 * @return a {@link String} object.
	 */
	public static String getReturnType(Class<?> clazz) {
		String retVal = ClassUtils.getShortClassName(clazz);
		if (primitiveClasses.contains(retVal))
			return clazz.getSimpleName();

		return retVal;
	}

	// TODO: Handle inner classes (need instance parameter for newInstance)

	@Override
	public VariableReference getReturnValue() {
		return null;
	}

	@Override
	public void setRetval(VariableReference newRetVal) {

	}

	/** {@inheritDoc} */
	@Override
	public Throwable execute(final Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        InstantiationException, IllegalAccessException {
		//PrintStream old_out = System.out;
		//PrintStream old_err = System.err;
		//System.setOut(out);
		//System.setErr(out);

		logger.trace("Executing constructor " + constructor.toString());
		final Object[] inputs = new Object[parameters.size()];
		Throwable exceptionThrown = null;

		try {
			return super.exceptionHandler(new Executer() {

				@Override
				public void execute() throws InvocationTargetException,
				        IllegalArgumentException, IllegalAccessException,
				        InstantiationException, CodeUnderTestException {


				}

				@Override
				public Set<Class<? extends Throwable>> throwableExceptions() {
					Set<Class<? extends Throwable>> t = new LinkedHashSet<Class<? extends Throwable>>();
					t.add(InvocationTargetException.class);
					return t;
				}
			});

		} catch (InvocationTargetException e) {
			exceptionThrown = e.getCause();
			if (logger.isDebugEnabled()) {
				try {
					logger.debug("Exception thrown in constructor: " + e.getCause());
				}
				//this can happen if SUT throws exception on toString
				catch (Exception ex) {
					logger.debug("Exception thrown in constructor and SUT gives issue when calling e.getCause()",
					             ex);
				}
			}
		}
		return exceptionThrown;
	}

	/** {@inheritDoc} */
	@Override
	public Statement copy(TestCase newTestCase, int offset) {
		ArrayList<VariableReference> new_params = new ArrayList<VariableReference>();
		for (VariableReference r : parameters) {
			new_params.add(r.copy(newTestCase, offset));
		}

		// copy.assertions = copyAssertions(newTestCase, offset);

		return null;
	}


	/**
	 * <p>
	 * getParameterReferences
	 * </p>
	 *
	 * @return a {@link List} object.
	 */
	public List<VariableReference> getParameterReferences() {
		return parameters;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getNumParameters()
	 */
	@Override
	public int getNumParameters() {
		return parameters.size();
	}

	@Override
	public Class<?> getReturnClass() {
		return null;
	}

	@Override
	public ClassReader.DataType getReturnType() {
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

		ConstructorStatement ms = (ConstructorStatement) s;
		if (ms.parameters.size() != parameters.size())
			return false;

		if (!this.constructor.equals(ms.constructor))
			return false;

		for (int i = 0; i < parameters.size(); i++) {
			if (!parameters.get(i).equals(ms.parameters.get(i)))
				return false;
		}

		return retval.equals(ms.retval);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 41;
		int result = 1;
		result = prime * result + ((constructor == null) ? 0 : constructor.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.Statement#getDeclaredExceptions()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		Set<Class<?>> ex = super.getDeclaredExceptions();
		return ex;
	}



	/**
	 * Go through parameters of constructor call and apply local search
	 * 
	 * @param test
	 * @param factory
	 */
	/* (non-Javadoc)
	 * @see org.evosuite.testcase.AbstractStatement#mutate(org.evosuite.testcase.TestCase, org.evosuite.testcase.TestFactory)
	 */
	public boolean mutate(TestCase test, TestFactory factory) {

		if (Randomness.nextDouble() >= Properties.P_CHANGE_PARAMETER)
			return false;

		List<VariableReference> parameters = getParameterReferences();
		if (parameters.isEmpty())
			return false;
		double pParam = 1.0/parameters.size();
		boolean changed = false;
		for(int numParameter = 0; numParameter < parameters.size(); numParameter++) {
			if(Randomness.nextDouble() < pParam) {
				if(mutateParameter(test, numParameter))
					changed = true;
			}
		}
		return changed;
	}


	@Override
	public boolean isAccessible() {
		return super.isAccessible();
	}
	
	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#isValid()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isValid() {
		assert (super.isValid());
		for (VariableReference v : parameters) {
			v.getStPosition();
		}
		return true;
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

		ConstructorStatement ms = (ConstructorStatement) s;
		if (ms.parameters.size() != parameters.size())
			return false;

		if (!this.constructor.equals(ms.constructor))
			return false;

		for (int i = 0; i < parameters.size(); i++) {
			if (!parameters.get(i).same(ms.parameters.get(i)))
				return false;
		}

		return retval.same(ms.retval);
	}

	@Override
	public boolean references(VariableReference returnValue) {
		return false;
	}

	/** {@inheritDoc} */
	public GenericConstructor getAccessibleObject() {
		return constructor;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		super.changeClassLoader(loader);
	}

	@Override
	public String toString()
	{
		return "";
	}

	@Override
	public String getDescriptor() {
		return "sd";
	}

	@Override
	public String getDeclaringClassName() {
		return "";
	}

	@Override
	public String getMethodName() {
		return "<init>";
	}
}
