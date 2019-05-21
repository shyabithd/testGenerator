package generator.testcase.statement;

import generator.DataType;
import generator.testcase.TestCase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

/**
 * <p>Abstract NumericalPrimitiveStatement class.</p>
 *
 * @author Gordon Fraser
 */
public abstract class NumericalPrimitiveStatement<T> extends PrimitiveStatement<T> {

	private static final long serialVersionUID = 476613542969677702L;

	public NumericalPrimitiveStatement(TestCase tc, DataType type, T value) {
		super(tc, type, value);
	}

	/**
	 * Increase value by smallest possible increment
	 */
	public abstract void increment();

	/**
	 * Decrease value by smallest possible increment
	 */
	public abstract void decrement();

	/**
	 * Change value by delta
	 *
	 * @param delta a long.
	 */
	public abstract void increment(long delta);

	/**
	 * Change value by delta
	 *
	 * @param delta a double.
	 */
	public void increment(double delta) {
		increment((long) delta);
	}

	/**
	 * Needed for binary search
	 *
	 * @param min a T object.
	 * @param max a T object.
	 */
	public abstract void setMid(T min, T max);

	/**
	 * Is the value >= 0?
	 *
	 * @return a boolean.
	 */
	public abstract boolean isPositive();

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();		
		oos.writeObject(value);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();
		value = (T) ois.readObject();
	}
}
