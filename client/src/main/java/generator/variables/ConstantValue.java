package generator.variables;

import generator.ClassReader;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;
import generator.testcase.variable.VariableReferenceImpl;
import generator.utils.GenericClass;
import generator.utils.NumberFormatter;

import java.lang.reflect.Type;


/**
 * <p>ConstantValue class.</p>
 *
 * @author Gordon Fraser
 */
public class ConstantValue extends VariableReferenceImpl {

	private static final long serialVersionUID = -3760942087575495415L;

    public ConstantValue(TestCase testCase, GenericClass type) {
        super(testCase, type);
    }

    public ConstantValue(TestCase testCase, GenericClass type, Object value) {
        super(testCase, type);
        setValue(value);
    }

	public ConstantValue(TestCase testCase, ClassReader.DataType type) {
		this(testCase, new GenericClass(type));
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference copy(TestCase newTestCase, int offset) {
		ConstantValue ret = new ConstantValue(newTestCase, type);
		ret.setValue(value);
		return ret;
	}

	@Override
	public VariableReference clone(TestCase newTestCase) {
		Statement st = newTestCase.getStatement(getStPosition());
		for(VariableReference var : st.getVariableReferences()) {
			if(same(var)) {
				return var;
			}
		}
		throw new IllegalArgumentException("Constant value not defined in new test");
	}

	private Object value;

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link Object} object.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link Object} object.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The position of the statement, defining this VariableReference, in the
	 * testcase.
	 */
	@Override
	public int getStPosition() {
		for (int i = 0; i < testCase.size(); i++) {
			if (testCase.getStatement(i).references(this)) {
				return i;
			}
		}

		throw new AssertionError(
		        "A ConstantValue position is only defined if the VariableReference is defined by a statement");
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return name for source code representation
	 */
	@Override
	public String getName() {
		if(value == null) {
			return "null";
		} else if(value instanceof Class<?>){
			Class<?> cl = (Class<?>)value;
			String name = cl.getSimpleName();
            return name + ".class";
        } 
		return NumberFormatter.getNumberString(value);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return the actual object represented by this variable for a given scope
	 */
	@Override
	public Object getObject(Scope scope) {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean same(VariableReference r) {
		if (r == null)
			return false;

		if (!this.type.equals(r.getGenericClass()))
			return false;

		if (r instanceof ConstantValue) {
			ConstantValue v = (ConstantValue) r;
			if (this.value == null) {
				if (v.getValue() == null)
					return true;
			} else {
				if (this.value.equals(v.getValue()))
					return true;
			}
		}

		return false;
	}
}
