package generator.testcase.variable;

import generator.ClassReader;
import generator.DataType;
import generator.testcase.TestCase;
import generator.utils.GenericClass;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class defines an reference to an array element. E.g. foo[3]
 * 
 * @author Sebastian Steenbuck
 */
public class ArrayIndex extends VariableReferenceImpl {

	private static final long serialVersionUID = -4492869536935582711L;

	/**
	 * Index in the array
	 */
	private List<Integer> indices;

	/**
	 * If this variable is contained in an array, this is the reference to the
	 * array
	 */
	protected ArrayReference array = null;

	public ArrayIndex(TestCase testCase, ArrayReference array, int index) {
		this(testCase, array, Collections.singletonList(index));
	}

	public ArrayIndex(TestCase testCase, ArrayReference array, List<Integer> indices) {
		super(testCase, new GenericClass(getReturnType(array, indices.size())));
		this.array = array;
		setArrayIndices(indices);
	}

	private static DataType getReturnType(ArrayReference array, int indicesCnt) {
		assert indicesCnt >= 1;
		DataType result = array.getComponentType();
		// Class<?> result = (Class<?>) array.getComponentType();
		for (int idx = 1; idx < indicesCnt; idx++) {
			//result = GenericTypeReflector.getArrayComponentType(result);
			//result = result.getComponentType();
		}
		return result;
	}

	/**
	 * <p>
	 * Getter for the field <code>array</code>.
	 * </p>
	 *
	 * @return a {@link ArrayReference} object.
	 */
	public ArrayReference getArray() {
		return array;
	}

	/**
	 * <p>
	 * Setter for the field <code>array</code>.
	 * </p>
	 *
	 * @param r
	 *            a {@link ArrayReference} object.
	 */
	public void setArray(ArrayReference r) {
		array = r;
	}

	/**
	 * Return true if variable is an array
	 *
	 * @return a boolean.
	 */
	public boolean isArrayIndex() {
		return true;
	}

	/**
	 * <p>
	 * getArrayIndex
	 * </p>
	 *
	 * @return a int.
	 */
	public int getArrayIndex() {
		assert indices.size() == 1;
		return indices.get(0);
	}

	/**
	 * <p>
	 * setArrayIndex
	 * </p>
	 *
	 * @param index
	 *            a int.
	 */
	public void setArrayIndex(int index) {
		assert indices.size() == 1;
		indices.set(0, index);
	}

	/** {@inheritDoc} */
	@Override
	public int getStPosition() {
		assert (array != null);
		for (int i = 0; i < testCase.size(); i++) {
			if (testCase.getStatement(i).getReturnValue().equals(this)) {
				return i;
			}
		}

		//notice that this case is only reached if no AssignmentStatement was used to assign to the array index (as in that case the for loop would have found something)
		//Therefore the array must have been assigned in some method and we can return the method call

		//throw new AssertionError(
		//        "A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");

		return array.getStPosition();

		//throw new AssertionError("A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return name for source code representation
	 */
	@Override
	public String getName() {
		String result = array.getName();
		for (int index : indices) {
			result += "[" + index + "]";
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean same(VariableReference r) {
		if (r == null)
			return false;

		if (!(r instanceof ArrayIndex))
			return false;

		ArrayIndex other = (ArrayIndex) r;
		if (this.getStPosition() != r.getStPosition())
			return false;

		if (!this.array.same(other.getArray()))
			return false;

		if (!indices.equals(other.indices))
			return false;

		return true;
	}

	private Integer getIntValue(Object object) {
		if (object==null) {
			return null;
		} else if (object instanceof Number) {
			return ((Number) object).intValue();
		} else if (object instanceof Character) {
			return new Integer(((Character) object).charValue());
		} else
			return 0;
	}

	private Short getShortValue(Object object) {
		if (object==null) {
			return null;
		} else if (object instanceof Number) {
			return (short)((Number) object).intValue();
		} else if (object instanceof Character) {
			return new Short((short)((Character) object).charValue());
		} else
			return 0;
	}

	private Byte getByteValue(Object object) {
		if (object==null) {
			return null;
		} else if (object instanceof Number) {
			return (byte)((Number) object).intValue();
		} else if (object instanceof Character) {
			return new Byte((byte)((Character) object).charValue());
		} else
			return 0;
	}

	private Long getLongValue(Object object) {
		if (object==null) {
			return null;
		} else if (object instanceof Number) {
			return ((Number) object).longValue();
		} else if (object instanceof Character) {
			return new Long(((Character) object).charValue());
		} else
			return 0L;
	}

	private Float getFloatValue(Object object) {
		if (object==null) {
			return null;
		} else if (object instanceof Number) {
			return ((Number) object).floatValue();
		} else if (object instanceof Character) {
			return new Float(((Character) object).charValue());
		} else
			return 0F;
	}

	private Double getDoubleValue(Object object) {
		if (object==null) {
			return null;
		} else if (object instanceof Number) {
			return ((Number) object).doubleValue();
		} else if (object instanceof Character) {
			return new Double(((Character) object).charValue());
		} else
			return 0.0;
	}

	private Character getCharValue(Object object) {
		if (object==null) {
			return null;
		} else if (object instanceof Character) {
			return ((Character) object).charValue();
		} else if (object instanceof Number) {
			return (char) ((Number) object).intValue();
		} else
			return '0';
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference copy(TestCase newTestCase, int offset) {
		ArrayReference otherArray = (ArrayReference) newTestCase.getStatement(array.getStPosition()
		                                                                              + offset).getReturnValue();
		//must be set as we only use this to clone whole testcases
		return new ArrayIndex(newTestCase, otherArray, indices);
	}

	@Override
	public VariableReference clone(TestCase newTestCase) {
		return new ArrayIndex(newTestCase, (ArrayReference)array.clone(newTestCase), indices);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#getAdditionalVariableReference()
	 */
	/** {@inheritDoc} */
	@Override
	public VariableReference getAdditionalVariableReference() {
		if (array.getAdditionalVariableReference() == null)
			return array;
		else
			return array.getAdditionalVariableReference();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#setAdditionalVariableReference(org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void setAdditionalVariableReference(VariableReference var) {
		assert (var instanceof ArrayReference);
		array = (ArrayReference) var;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replaceAdditionalVariableReference(VariableReference var1,
	        VariableReference var2) {
		if (array.equals(var1)) {
			if (var2 instanceof ArrayReference) {
				array = (ArrayReference) var2;
			}
			// EvoSuite might try to replace this with a field reference
			// but for this we have FieldStatements, which would give us
			// ArrayReferences.
			// Such a replacement should only happen as part of a graceful delete
		} else
			array.replaceAdditionalVariableReference(var1, var2);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((array == null) ? 0 : array.hashCode());
		// TODO: indices shouldn't normally be null
		//       but it sometimes happens for StrongMutation...
		result = prime * result + ((indices == null) ? 0 : indices.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayIndex other = (ArrayIndex) obj;
		if (array == null) {
			if (other.array != null)
				return false;
		} else if (!array.equals(other.array))
			return false;
		if (!indices.equals(other.indices))
			return false;
		return true;
	}

	/**
	 * <p>
	 * setArrayIndices
	 * </p>
	 *
	 * @param indices
	 *            a {@link List} object.
	 */
	public void setArrayIndices(List<Integer> indices) {
		this.indices = new ArrayList<Integer>();
		for (Integer i : indices)
			this.indices.add(i);
	}

	/**
	 * <p>
	 * getArrayIndices
	 * </p>
	 *
	 * @return a {@link List} object.
	 */
	public List<Integer> getArrayIndices() {
		return indices;
	}
}
