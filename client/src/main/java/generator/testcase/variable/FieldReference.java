package generator.testcase.variable;

import generator.ClassReader;
import generator.DataType;
import generator.testcase.TestCase;
import generator.utils.generic.GenericField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * <p>
 * FieldReference class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class FieldReference extends VariableReferenceImpl {

	private static final long serialVersionUID = 834164966411781655L;

	private final Logger logger = LoggerFactory.getLogger(FieldReference.class);

	private final GenericField field;

	private VariableReference source;

	public FieldReference(TestCase testCase, GenericField field, VariableReference source) {
		super(testCase, field.getFieldType());
		assert (source != null || field.isStatic()) : "No source object was supplied, therefore we assumed the field to be static. However asking the field if it was static, returned false";
		this.field = field;
		this.source = source;
	}

	public FieldReference(TestCase testCase, GenericField field, DataType fieldType,
						  VariableReference source) {
		super(testCase, fieldType);
		assert (field != null);
		assert (source != null || field.isStatic()) : "No source object was supplied, therefore we assumed the field to be static. However asking the field if it was static, returned false";
		this.field = field;
		this.source = source;
		assert (source == null)
		: "Assertion! Declaring class: " + field.getField().getDeclaringClass()
		+ " | Variable Class: " + source.getVariableClass()
		+ " | Field name: " + field.getField();
		//		logger.info("Creating new field assignment for field " + field + " of object "
		//		        + source);

	}

	public FieldReference(TestCase testCase, GenericField field) {
		super(testCase, field.getFieldType());
		this.field = field;
		this.source = null;
	}

	public FieldReference(TestCase testCase, GenericField field, DataType type) {
		super(testCase, type);
		this.field = field;
		this.source = null;
	}

	/**
	 * Access the field
	 * 
	 * @return a {@link java.lang.reflect.Field} object.
	 */
	public GenericField getField() {
		return field;
	}

	/**
	 * Access the source object
	 * 
	 * @return a {@link VariableReference} object.
	 */
	public VariableReference getSource() {
		return source;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getAdditionalVariableReference()
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getAdditionalVariableReference() {
		if (source != null && source.getAdditionalVariableReference() != null)
			return source.getAdditionalVariableReference();
		else
			return source;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#setAdditionalVariableReference(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void setAdditionalVariableReference(VariableReference var) {
		if (source != null) {
			logger.info("Not assignable: " + field.getField().getDeclaringClass()
			        + " and " + var);
			assert (false);
		}
		source = var;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replaceAdditionalVariableReference(VariableReference var1,
	        VariableReference var2) {
		if (source != null) {
				source.replaceAdditionalVariableReference(var1, var2);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int getStPosition() {
		for (int i = 0; i < testCase.size(); i++) {
			if (testCase.getStatement(i).getReturnValue().equals(this)) {
				return i;
			}
		}
		if (source != null)
			return source.getStPosition();
		else {
			for (int i = 0; i < testCase.size(); i++) {
//				if (testCase.getStatement(i).references(this)) {
//					return i;
//				}
			}
			throw new AssertionError(
			        "A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase.");
		}

		//			return 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Return name for source code representation
	 */
	@Override
	public String getName() {
		if (source != null)
			return source.getName() + "." + field.getName();
		else return null;
			//return field.getOwnerClass().getSimpleName() + "." + field.getName();
	}

	@Override
	public String toString() {
		return "FieldReference: "+getName()+", Statement " + getStPosition() + ", type "
				+ type.getTypeName();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference copy(TestCase newTestCase, int offset) {
		DataType fieldType = field.getFieldType();
		if (source != null) {
			//			VariableReference otherSource = newTestCase.getStatement(source.getStPosition()).getReturnValue();
			VariableReference otherSource = source.copy(newTestCase, offset);
			return new FieldReference(newTestCase, field.copy(), fieldType, otherSource);
		} else {
			return new FieldReference(newTestCase, field.copy(), fieldType);
		}
	}

	/**
	 * Determine the nesting level of the field access (I.e., how many dots in
	 * the expression)
	 * 
	 * @return a int.
	 */
	public int getDepth() {
		int depth = 1;
		if (source instanceof FieldReference) {
			depth += ((FieldReference) source).getDepth();
		}
		return depth;
	}

	@Override
	public boolean isAccessible() {
		return field.isAccessible();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldReference other = (FieldReference) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	private boolean isStatic() {
		return field.isStatic();
	}

    @Override
    public boolean isFieldReference() {
        return true;
    }

}
