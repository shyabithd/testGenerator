package generator.utils.generic;

import generator.ClassReader;
import generator.DataType;
import generator.ga.ConstructionFailedException;
import generator.testcase.TestUsageChecker;
import generator.testcase.variable.VariableReference;
import generator.utils.GenericClass;
import generator.utils.LoggingUtils;
import utils.Inputs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericMethod extends GenericAccessibleObject<GenericMethod> {

	private static final long serialVersionUID = 6091851133071150237L;

	private transient ClassReader.Method method;

	public GenericMethod(ClassReader.Method method, GenericClass type) {
		super(new GenericClass(type));
		this.method = method;
		Inputs.checkNull(method, type);
	}

	public GenericMethod(ClassReader.Method method, ClassReader type) {
		super(new GenericClass(type));
		this.method = method;
		Inputs.checkNull(method, type);
	}

	public GenericMethod(ClassReader.Method method, DataType type) {
		super(new GenericClass(type));
		this.method = method;
		Inputs.checkNull(method, type);
	}

	@Override
	public GenericMethod copyWithNewOwner(GenericClass newOwner) {
		GenericMethod copy = new GenericMethod(method, newOwner);
		copyTypeVariables(copy);
		return copy;
	}

	@Override
	public GenericMethod copyWithOwnerFromReturnType(GenericClass returnType)
	        throws ConstructionFailedException {
		GenericClass newOwner = getOwnerClass().getGenericInstantiation(returnType.getTypeVariableMap());
		GenericMethod copy = new GenericMethod(method, newOwner);
		copyTypeVariables(copy);
		return copy;
	}

	@Override
	public GenericMethod copy() {
		GenericMethod copy = new GenericMethod(method, new GenericClass(owner));
		copyTypeVariables(copy);
		return copy;
	}

	public ClassReader.Method getMethod() {
		return method;
	}


	@Override
	public Class<?> getDeclaringClass() {
		return method.getClass();
	}

	public DataType[] getParameterTypes() {
		return getExactParameterTypes(method, owner.getType());
	}

	public List<GenericClass> getParameterClasses() {
		List<GenericClass> parameters = new ArrayList<>();

		if(logger.isDebugEnabled()) {
			logger.debug("Parameter types: " + Arrays.asList(method.getGenericParameterTypes()));
		}

		for (DataType parameterType : getParameterTypes()) {
			logger.debug("Adding parameter: {}", parameterType);
			parameters.add(new GenericClass(parameterType));
		}
		return parameters;
	}

	public DataType[] getGenericParameterTypes() {
		return method.getGenericParameterTypes();
	}

	public DataType[] getRawParameterTypes() {
		return method.getParameterTypes();
	}

	@Override
	public DataType getGeneratedType() {
		return getReturnType();
	}

	public DataType getReturnType() {
		DataType returnType = getExactReturnType(method, owner.getType());
		if (returnType == null) {
			LoggingUtils.getGeneratorLogger().info("Exact return type is null for {} with owner {}",method, owner);
			for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
				LoggingUtils.getGeneratorLogger().info(elem.toString());
			}
			assert (false);

			returnType = method.getGenericReturnType();
		}
		return returnType;
	}

	@Override
	public DataType getGenericGeneratedType() {
		return method.getGenericReturnType();
	}

	@Override
	public Class<?> getRawGeneratedType() {
		return method.getReturnType().getClass();
	}

	/**
	 * Returns the exact return type of the given method in the given type. This
	 * may be different from <tt>m.getGenericReturnType()</tt> when the method
	 * was declared in a superclass, or <tt>type</tt> has a type parameter that
	 * is used in the return type, or <tt>type</tt> is a raw type.
	 */
	protected DataType getExactReturnType(ClassReader.Method m, DataType type) throws IllegalArgumentException{
		Inputs.checkNull(m,type);

		DataType returnType = m.getGenericReturnType();
		DataType exactDeclaringType = null;

		if (exactDeclaringType == null) { // capture(type) is not a subtype of m.getDeclaringClass()
			logger.info("The method " + m + " is not a member of type " + type
			        + " - declared in " + m.getDeclaringClass());
			return m.getReturnType();
		}

		//if (exactDeclaringType.equals(type)) {
		//	logger.debug("Returntype: " + returnType + ", " + exactDeclaringType);
		//	return returnType;
		//}

		return mapTypeParameters(returnType, exactDeclaringType);
	}

	/**
	 * Returns the exact parameter types of the given method in the given type.
	 * This may be different from <tt>m.getGenericParameterTypes()</tt> when the
	 * method was declared in a superclass, or <tt>type</tt> has a type
	 * parameter that is used in one of the parameters, or <tt>type</tt> is a
	 * raw type.
	 */
	public DataType[] getExactParameterTypes(ClassReader.Method m, DataType type) {
		DataType[] parameterTypes = m.getGenericParameterTypes();
		DataType exactDeclaringType = null;
		if (exactDeclaringType == null) { // capture(type) is not a subtype of m.getDeclaringClass()
			logger.info("The method " + m + " is not a member of type " + type
			        + " - declared in " + m.getDeclaringClass());
			return m.getParameterTypes();
		}

		DataType[] result = new DataType[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			result[i] = mapTypeParameters(parameterTypes[i], exactDeclaringType);
		}
		return result;
	}

	@Override
	public DataType[] getTypeParameters() {
		return method.getTypeParameters();
	}

	@Override
	public boolean isAccessible() {
		return TestUsageChecker.canUse(method);
	}


	@Override
	public boolean isMethod() {
		return true;
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(method.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}
	
	public boolean isOverloaded() {
		String methodName = getName();
		return false;
	}


	public boolean isOverloaded(List<VariableReference> parameters) {
		String methodName = getName();
		boolean isExact = true;
		if (isExact) {
			return false;
		}

		return false;
	}

	@Override
	public int getNumParameters() {
		return method.getGenericParameterTypes().length;
	}

	public boolean isGenericMethod() {
		return getNumParameters() > 0;
	}


	@Override
	public String getName() {
		return method.getName();
	}

	public String getNameWithDescriptor() {
		return method.getName() ;
	}

	public String getDescriptor() {
		return "sd";
	}

	@Override
	public String toString() {
		return method.toGenericString();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		oos.writeObject(method.getDeclaringClass());
		oos.writeObject(method.getName());
	}

	@Override
	public boolean isPublic() { return Modifier.isPublic(method.getModifiers()); }

	@Override
	public boolean isPrivate() { return Modifier.isPrivate(method.getModifiers()); }

	@Override
	public boolean isProtected() { return Modifier.isProtected(method.getModifiers()); }

	@Override
	public boolean isDefault() { return !isPublic() && !isPrivate() && !isProtected(); }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericMethod other = (GenericMethod) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}


}
