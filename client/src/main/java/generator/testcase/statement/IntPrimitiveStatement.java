package generator.testcase.statement;

import generator.DataType;
import generator.Properties;
import generator.TestGenerationContext;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.variable.VariableReference;
import generator.utils.Randomness;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * <p>
 * IntPrimitiveStatement class.
 * </p>
 * 
 * @author fraser
 */
public class IntPrimitiveStatement extends NumericalPrimitiveStatement<Integer> {

	private static final long serialVersionUID = -8616399657291345433L;

	public IntPrimitiveStatement(TestCase tc) {
		super(tc, new DataType("int", TestGenerationContext.getInstance().getClassReader()), 0);
	}


	/** {@inheritDoc} */
	@Override
	public void zero() {
		value = 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#delta()
	 */
	/** {@inheritDoc} */
	@Override
	public void delta() {
		int delta = (int)Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
		value = value + delta;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void increment(long delta) {
		value = value + (int) delta;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	/** {@inheritDoc} */
	@Override
	public void randomize() {
		if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
			value = (int)(Randomness.nextGaussian() * Properties.MAX_INT) ;
		}
//		else {
//			ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();
//			value = constantPool.getRandomInt();
//		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment()
	 */
	/** {@inheritDoc} */
	@Override
	public void increment() {
		increment(1);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void setMid(Integer min, Integer max) {
		value = (int) (min + ((max - min) / 2));
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#decrement()
	 */
	/** {@inheritDoc} */
	@Override
	public void decrement() {
		increment(-1);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#isPositive()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPositive() {
		return value >= 0;
	}

	/** {@inheritDoc} */
	@Override
	public void negate() {
		value = -value;
	}

	/** {@inheritDoc} */
	@Override
	public Integer getValue() {
		return value;
	}
}
