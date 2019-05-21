package generator.testcase.statement;

import generator.ClassReader;
import generator.DataType;
import generator.testcase.CodeUnderTestException;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.variable.VariableReference;
import generator.testcase.variable.VariableReferenceImpl;
import generator.utils.GenericClass;

import javax.xml.crypto.Data;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public abstract class PrimitiveStatement<T> extends AbstractStatement {

    private static final long serialVersionUID = -7721106626421922833L;

    /**
     * The value
     */

    @Override
    public generator.testcase.statement.Statement clone() {
        return null;
    }

    protected transient T value;

    public PrimitiveStatement(TestCase tc, VariableReference varRef, T value) {
        super(tc, varRef.getType());
        this.value = value;
    }

    public PrimitiveStatement(TestCase tc, DataType type, T value) {
        super(tc, new VariableReferenceImpl(tc, type));
        this.value = value;
    }

    /**
     * Access the value
     *
     * @return a T object.
     */
    public T getValue() {
        return value;
    }

    /**
     * Set the value
     *
     * @param val a T object.
     */
    public void setValue(T val) {
        this.value = val;
    }

    public boolean hasMoreThanOneValue() {
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static PrimitiveStatement<?> getPrimitiveStatement(TestCase tc,
                                                              GenericClass genericClass) {
        // TODO This kills the benefit of inheritance.
        // Let each class implement the clone method instead

        DataType type = genericClass.getType();
        PrimitiveStatement<?> statement = null;
        if (type.getDataType().equals("int")) {
            statement = new IntPrimitiveStatement(tc);
        }
        return statement;
    }

    public static PrimitiveStatement<?> getRandomStatement(TestCase tc,
                                                           GenericClass clazz, int position) {

        PrimitiveStatement<?> statement = getPrimitiveStatement(tc, clazz);
        statement.randomize();
        return statement;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        //        PrimitiveStatement<T> clone = (PrimitiveStatement<T>) getPrimitiveStatement(newTestCase,
//                retval.getGenericClass());
        //clone.setValue(value);
        // clone.assertions = copyAssertions(newTestCase, offset);
//        return clone;
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object s) {
        if (this == s)
            return true;
        if (s == null)
            return false;
        if (getClass() != s.getClass())
            return false;

        PrimitiveStatement<?> ps = (PrimitiveStatement<?>) s;
        return false;
        //return (retval.equals(ps.retval) && value.equals(ps.value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 21;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * Add a random delta to the value
     */
    public abstract void delta();

    /**
     * Reset value to default value 0
     */
    public abstract void zero();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean same(Statement s) {
        if (this == s)
            return true;
        if (s == null)
            return false;
        if (getClass() != s.getClass())
            return false;

        PrimitiveStatement<?> ps = (PrimitiveStatement<?>) s;

        boolean sameValue = false;
        if (value == null) {
            sameValue = (ps.value == null);
        } else {
            sameValue = value.equals(ps.value);
        }

//        assert retval != null && ps.retval != null;
//
//        return (sameValue && retval.same(ps.retval));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getCode();
    }

    @SuppressWarnings("unused")
    private void mutateTransformedBoolean(TestCase test) {
    }

    /**
     * Set to a random value
     */
    public abstract void randomize();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignmentStatement() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeClassLoader(ClassLoader loader) {
        super.changeClassLoader(loader);
    }

    @Override
    public Set<VariableReference> getVariableReferences() {
        Set<VariableReference> references = new LinkedHashSet<>();
        references.add(retval);
        return references;
    }

    @Override
    public Throwable execute(Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        Throwable exceptionThrown = null;

        try {
            retval.setObject(scope, value);
        } catch (CodeUnderTestException e) {
            exceptionThrown = e;
        }
        return exceptionThrown;
    }

    @Override
    public void replace(VariableReference var1, VariableReference var2) {
        if (retval.equals(var1)) {
            retval = var2;
        }
    }
}
