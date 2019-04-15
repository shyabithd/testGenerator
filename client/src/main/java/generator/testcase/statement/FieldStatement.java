package generator.testcase.statement;

import generator.ClassReader;
import generator.Properties;
import generator.testcase.CodeUnderTestException;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.TestFactory;
import generator.testcase.variable.ArrayReference;
import generator.testcase.variable.VariableReference;
import generator.testcase.variable.VariableReferenceImpl;
import generator.utils.Randomness;
import generator.utils.generic.GenericField;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Statement that accesses an instance/class field
 * 
 * @author Gordon Fraser
 */
public class FieldStatement extends AbstractStatement {

	private static final long serialVersionUID = -4944610139232763790L;

	protected GenericField field;
	protected VariableReference source;

	public FieldStatement(TestCase tc, GenericField field, VariableReference source) {
		super(tc);
		this.field = field;
		this.source = source;
		if (retval.getComponentType() != null) {
			retval = new ArrayReference(tc, retval.getGenericClass(), 0);
		}
	}

	public FieldStatement(TestCase tc, GenericField field, VariableReference source,
	        VariableReference ret_var) {
		super(tc);
		assert (tc.size() > ret_var.getStPosition()); //as an old statement should be replaced by this statement
		this.field = field;
		this.source = source;
	}

	public VariableReference getSource() {
		return source;
	}

	//@Override
	public boolean mutate(TestCase test, TestFactory factory) {

		if (Randomness.nextDouble() >= Properties.P_CHANGE_PARAMETER)
			return false;

		if (!isStatic()) {
			VariableReference source = getSource();
			List<VariableReference> objects = test.getObjects(source.getType(),
			                                                  getPosition());
			objects.remove(source);

			if (!objects.isEmpty()) {
				setSource(Randomness.choice(objects));
				return true;
			}
		}

		return false;
	}

	public void setSource(VariableReference source) {
		this.source = source;
	}

	@Override
	public boolean isAccessible() {
		if(!field.isAccessible())
			return false;
		
		return super.isAccessible();
	}
	
	/**
	 * <p>
	 * isStatic
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isStatic() {
		return field.isStatic();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#getNumParameters()
	 */
	@Override
	public int getNumParameters() {
		if (isStatic())
			return 0;
		else
			return 1;
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
	public Statement copy(TestCase newTestCase, int offset) {
		if (field.isStatic()) {
			FieldStatement s = new FieldStatement(newTestCase, field.copy(), null);
			s.getReturnValue().setType(retval.getType()); // Actual type may have changed, e.g. subtype
			// s.assertions = copyAssertions(newTestCase, offset);
			return s;
		} else {
			VariableReference newSource = source.copy(newTestCase, offset);
			FieldStatement s = new FieldStatement(newTestCase, field.copy(), newSource);
			s.getReturnValue().setType(retval.getType()); // Actual type may have changed, e.g. subtype
			// s.assertions = copyAssertions(newTestCase, offset);
			return s;
		}

	}

	/** {@inheritDoc} */
	@Override
	public Throwable execute(final Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		Throwable exceptionThrown = null;

		try {
			return super.exceptionHandler(new Executer() {

				@Override
				public void execute() throws InvocationTargetException,
				        IllegalArgumentException, IllegalAccessException,
				        InstantiationException, CodeUnderTestException {
					Object source_object;
					try {
						source_object = (field.isStatic()) ? null
						        : null;

						if (!field.isStatic() && source_object == null) {
							throw new CodeUnderTestException(new NullPointerException());
						}
						//} catch (CodeUnderTestException e) {
						//	throw CodeUnderTestException.throwException(e.getCause());
					} catch (CodeUnderTestException e) {
						throw e;
					}

//					Object ret = field.getField().get(source_object);
//					if(ret!=null && !retval.isAssignableFrom(ret.getClass())) {
//						throw new CodeUnderTestException(new ClassCastException());
//					}
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
		}
		return exceptionThrown;
	}

	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new LinkedHashSet<VariableReference>();
		references.add(retval);
		if (!isStatic()) {
			references.add(source);
			if (source.getAdditionalVariableReference() != null)
				references.add(source.getAdditionalVariableReference());
		}
		return references;

	}

	@Override
	public VariableReference getReturnValue() {
		return null;
	}

	@Override
	public void setRetval(VariableReference newRetVal) {

	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	//@Override
	public void replace(VariableReference var1, VariableReference var2) {
		if (!field.isStatic()) {
			if (source.equals(var1))
				source = var2;
			else
				source.replaceAdditionalVariableReference(var1, var2);
		}
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

		FieldStatement fs = (FieldStatement) s;
		if (!field.isStatic())
			return source.equals(fs.source) && retval.equals(fs.retval)
			        && field.equals(fs.field);
		else
			return retval.equals(fs.retval) && field.equals(fs.field);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 51;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	/**
	 * <p>
	 * Getter for the field <code>field</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.reflect.Field} object.
	 */
	public GenericField getField() {
		return field;
	}

	/**
	 * <p>
	 * Setter for the field <code>field</code>.
	 * </p>
	 * 
	 * @param field
	 *            a {@link java.lang.reflect.Field} object.
	 */
	public void setField(GenericField field) {
		// assert (this.field.getType().equals(field.getType()));
		this.field = field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	/** {@inheritDoc} */
	//@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
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

		FieldStatement fs = (FieldStatement) s;
		if (!field.isStatic())
			return source.same(fs.source) && retval.same(fs.retval)
			        && field.equals(fs.field);
		else
			return retval.same(fs.retval) && field.equals(fs.field);
	}

	@Override
	public boolean references(VariableReference returnValue) {
		return false;
	}

	/** {@inheritDoc} */
	//@Override
	public GenericField getAccessibleObject() {
		return field;
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
		//field.changeClassLoader(loader);
		super.changeClassLoader(loader);
	}
}
