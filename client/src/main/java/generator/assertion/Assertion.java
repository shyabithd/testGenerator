package generator.assertion;

import generator.mutation.Mutation;
import generator.testcase.TestCase;
import generator.testcase.statement.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Assertion implements Serializable {

	private static final long serialVersionUID = 1617423211706717599L;

	/** Expected value of variable */
	protected Object value;

	/** Statement to which the assertion is added */
	protected Statement statement;
	
	/** Assertion Comment */
	protected String comment;

	protected transient Set<Mutation> killedMutants = new LinkedHashSet<Mutation>();

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(Assertion.class);
	
	public boolean hasComment(){
		return (this.comment != null);
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public String getComment(){
		return " " + comment.replace('\n', ' ').replace('\r', ' ');
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Assertion other = (Assertion) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public void addKilledMutation(Mutation m) {
		killedMutants.add(m);
	}

	public Set<Mutation> getKilledMutations() {
		return killedMutants;
	}

	/**
	 * Setter for statement to which assertion is added
	 */
	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	/**
	**/
	public Statement getStatement() {
		return statement;
	}

	/**
	 * Getter for value object
	 * 
	 * @return a {@link Object} object.
	 */
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * This method returns the Java Code
	 *
	 * @return a {@link String} object.
	 */
	public abstract String getCode();

	/**
	 * {@inheritDoc}
	 *
	 * Return a copy of the assertion
	 */
	@Override
	public final Assertion clone() {
		throw new UnsupportedOperationException("Use Assertion.clone(TestCase)");
	}

	/**
	 * Return a copy of the assertion, which is valid in newTestCase
	 *
	 */
	public Assertion clone(TestCase newTestCase) {
		return copy(newTestCase, 0);
	}

	/**
	 * Return a copy of the assertion, which is valid in newTestCase
	 *
	 */
	public abstract Assertion copy(TestCase newTestCase, int offset);

	public void changeClassLoader(ClassLoader loader) {
		// Need to replace the classloader for enums
		if(value != null) {
			if(value.getClass().isEnum()) {
				Object[] constants = value.getClass().getEnumConstants();
				int pos = 0;
				for (pos = 0; pos < constants.length; pos++) {
					if (constants[pos].equals(value)) {
						break;
					}
				}

				try {
					Class<?> enumClass = loader.loadClass(value.getClass().getName());
					constants = enumClass.getEnumConstants();
					if(constants.length > 0)
						value = constants[pos];
					else
						logger.warn("Error changing classloader for enum constant "+value);
				} catch (ClassNotFoundException e) {
					logger.warn("Error changing classloader for enum constant "+value);
				}
			}
		}
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
    IOException {
		ois.defaultReadObject();

		killedMutants = new LinkedHashSet<Mutation>();
	}

}
