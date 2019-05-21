package generator.reflection;

import com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import generator.ClassReader;
import generator.ga.ConstructionFailedException;
import generator.testcase.CodeUnderTestException;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.TestFactory;
import generator.testcase.statement.MethodStatement;
import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;
import generator.utils.GenericClass;
import generator.utils.generic.GenericMethod;

import java.io.PrintStream;
import generator.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Statement representing the setting of a private field, which is done through reflection in the
 * generated JUnit tests.
 *
 * Created by foo on 20/02/15.
 */
public class PrivateFieldStatement extends MethodStatement {

	private static final long serialVersionUID = 5152490398872348493L;

	private static Method setVariable;

    private transient ClassReader ownerClass;

    private String className;

    private String fieldName;

    private boolean isStaticField = false;

    static {
//        try {
//            //Class<T> klass, T instance, String fieldName, Object value
//            //setVariable = PrivateAccess.class.getMethod("setVariable",Class.class, Object.class,String.class,Object.class);
//        } catch (NoSuchMethodException e) {
//            //should never happen
//            throw new RuntimeException("EvoSuite bug",e);
//        }
    }

    public PrivateFieldStatement(TestCase tc, ClassReader klass , String fieldName, VariableReference callee, VariableReference param)
            throws NoSuchFieldException, IllegalArgumentException, ConstructionFailedException {
        super(tc, null, callee, null);
//        super(
//                tc,
//                new GenericMethod(setVariable, PrivateAccess.class),
//                null, //it is static
//                Arrays.asList(  // setVariable(Class<T> klass, T instance, String fieldName, Object value)
//                        new ConstantValue(tc, new GenericClass(Class.class), klass),  // Class<T> klass
//                        //new ClassPrimitiveStatement(tc,klass).getReturnValue(),  // Class<T> klass
//                        callee, // T instance
//                        new ConstantValue(tc, new GenericClass(String.class), fieldName),  // String fieldName
//                        param // Object value
//                )
//        );
        this.className = klass.getCanonicalName();
        this.fieldName = fieldName;
        this.ownerClass = klass;

        List<GenericClass> parameterTypes = new ArrayList<>();
        parameterTypes.add(new GenericClass(klass));
        this.method.setTypeParameters(parameterTypes);
        determineIfFieldIsStatic(klass, fieldName);
    }

    private void determineIfFieldIsStatic(ClassReader klass, String fieldName) {
            Field f = klass.getDeclaredField(fieldName);
            if (Modifier.isStatic(f.getModifiers()))
                isStaticField = true;
    }

    public boolean isStaticField() {
        return isStaticField;
    }

    public String getOwnerClassName() {
        return className;
    }

    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        try {
            PrivateFieldStatement pf;
            VariableReference owner = parameters.get(1).copy(newTestCase, offset);
            VariableReference value = parameters.get(3).copy(newTestCase, offset);

            pf = new PrivateFieldStatement(newTestCase, ownerClass, fieldName, owner, value);

            return pf;
        } catch(NoSuchFieldException | ConstructionFailedException e) {
            throw new RuntimeException("EvoSuite bug", e);
        }
    }

    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        // just for simplicity
        return false;
        //return super.mutate(test,factory); //tricky, as should do some restrictions
    }
    
	@Override
	public boolean isReflectionStatement() {
		return true;
	}

    @Override
    public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        if(!isStaticField) {
            try {
                Object receiver = parameters.get(1).getObject(scope);
                if (receiver == null)
                    return new CodeUnderTestException(new NullPointerException());
            } catch (CodeUnderTestException e) {
                return e;
            }

        }
        return super.execute(scope, out);
    }
}
