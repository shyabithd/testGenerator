package generator.reflection;

import generator.ClassReader;
import generator.testcase.CodeUnderTestException;
import generator.testcase.Scope;
import generator.testcase.TestCase;
import generator.testcase.TestFactory;
import generator.testcase.statement.MethodStatement;
import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;
import generator.utils.GenericClass;
import generator.utils.generic.GenericMethod;
import generator.variables.ConstantValue;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case statement representing a reflection call to a private method of the SUT
 *
 * Created by Andrea Arcuri on 22/02/15.
 */
public class PrivateMethodStatement extends MethodStatement {

	private static final long serialVersionUID = -4555899888145880432L;

    private GenericMethod reflectedMethod;

    private boolean isStaticMethod = false;

	public PrivateMethodStatement(TestCase tc, ClassReader klass, ClassReader.Method method, VariableReference callee, List<VariableReference> params, boolean isStatic) {
        super(
                tc,
                new GenericMethod(method, klass),
                null, //it is static
                getReflectionParams(tc,klass,method,callee,params)
        );
        reflectedMethod = new GenericMethod(method, klass);
        isStaticMethod = isStatic;
        List<GenericClass> parameterTypes = new ArrayList<>();
        parameterTypes.add(new GenericClass(klass));
        this.method.setTypeParameters(parameterTypes);
    }

    private static List<VariableReference> getReflectionParams(TestCase tc, ClassReader klass , ClassReader.Method method,
                                                               VariableReference callee, List<VariableReference> inputs) {

        List<VariableReference> list = new ArrayList<>(3 + inputs.size()*2);
        list.add(new ConstantValue(tc,new GenericClass(klass),klass));
        list.add(callee);
        list.add(new ConstantValue(tc, new GenericClass(klass), method.getName()));

        ClassReader.DataType[] parameterTypes = method.getParameterTypes();
        assert(parameterTypes.length == inputs.size());
        for(int parameterNum = 0; parameterNum < parameterTypes.length; parameterNum++) {
            VariableReference vr = inputs.get(parameterNum);
            list.add(vr);
            list.add(new ConstantValue(tc,new GenericClass(klass), parameterTypes[parameterNum]));
        }

        return list;
    }

    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        // just for simplicity
        return false;
        //return super.mutate(test,factory); //tricky, as should do some restrictions
    }

    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        PrivateMethodStatement pm = null;
        List<VariableReference> newParams = new ArrayList<>();
        for(int i = 3; i < parameters.size(); i = i + 2) {
            newParams.add(parameters.get(i).copy(newTestCase, offset));
        }

        VariableReference newCallee = parameters.get(1).copy(newTestCase, offset);
        Class<?> klass = (Class<?>)((ConstantValue)parameters.get(0)).getValue(); // TODO: Make this nice

        //pm = new PrivateMethodStatement(newTestCase, klass, reflectedMethod.getMethod(), newCallee, newParams, isStaticMethod);

        assert pm.parameters.size() == this.parameters.size();

        return pm;
    }

    @Override
    public Throwable execute(final Scope scope, PrintStream out)
            throws InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        if(!isStaticMethod) {
            // If the callee is null, then reflection will only lead to a NPE.
            VariableReference callee = parameters.get(1);
            try {
                Object calleeObject = callee.getObject(scope);
                if(calleeObject == null)
                    return new CodeUnderTestException(new NullPointerException());
            } catch (CodeUnderTestException e) {
                return e;
            }
        }
        return super.execute(scope, out);
    }

    @Override
	public boolean isReflectionStatement() {
		return true;
	}

}
