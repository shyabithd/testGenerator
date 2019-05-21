package generator.testcase.variable;

import generator.ClassReader;
import generator.DataType;
import generator.testcase.TestCase;

import java.lang.reflect.Type;

/**
 * Special case of VariableInstance pointing to null
 *
 * @author Gordon Fraser
 */
public class NullReference extends VariableReferenceImpl {

	private static final long serialVersionUID = -6172885297590386463L;

	public NullReference(TestCase testCase, DataType type) {
		super(testCase, type);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference clone() {
		throw new UnsupportedOperationException();
	}
}
