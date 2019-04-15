package generator.utils.generic;

import generator.ClassReader;
import generator.testcase.TestUsageChecker;
import generator.testcase.variable.VariableReference;
import generator.utils.GenericClass;
import generator.utils.LoggingUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.*;
import java.util.List;

/**
 * @author Gordon Fraser
 *
 */
public class GenericConstructor extends GenericAccessibleObject<GenericConstructor> {

	private static final long serialVersionUID = 1361882947700615341L;

	private transient Constructor<?> constructor;

	public GenericConstructor(Constructor<?> constructor, ClassReader clazz) {
		super(new GenericClass(clazz));
		this.constructor = constructor;
	}

	public GenericConstructor(Constructor<?> constructor, GenericClass owner) {
		super(new GenericClass(owner));
		this.constructor = constructor;
	}

	public GenericConstructor(Constructor<?> constructor, ClassReader.DataType type) {
		super(new GenericClass(type));
		this.constructor = constructor;
	}

	@Override
	public GenericConstructor copy() {
		GenericConstructor copy = new GenericConstructor(constructor, new GenericClass(
		        owner));
		copyTypeVariables(copy);
		return copy;
	}

	@Override
	public GenericConstructor copyWithNewOwner(GenericClass newOwner) {
		GenericConstructor copy = new GenericConstructor(constructor, newOwner);
		copyTypeVariables(copy);
		return copy;
	}

	@Override
	public GenericConstructor copyWithOwnerFromReturnType(GenericClass returnType) {
		GenericConstructor copy = new GenericConstructor(constructor, returnType);
		copyTypeVariables(copy);
		return copy;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}


	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getDeclaringClass()
	 */
	@Override
	public Class<?> getDeclaringClass() {
		return constructor.getDeclaringClass();
	}

	/**
	 * Returns the exact parameter types of the given method in the given type.
	 * This may be different from <tt>m.getGenericParameterTypes()</tt> when the
	 * method was declared in a superclass, or <tt>type</tt> has a type
	 * parameter that is used in one of the parameters, or <tt>type</tt> is a
	 * raw type.
	 */
	public ClassReader.DataType[] getExactParameterTypes(Constructor<?> m, ClassReader.DataType type) {
		ClassReader.DataType[] parameterTypes = null; //m.getGenericParameterTypes();


		ClassReader.DataType[] result = new ClassReader.DataType[parameterTypes.length];

		return result;
	}

	public ClassReader.DataType[] getGenericParameterTypes() {
		return (ClassReader.DataType[]) constructor.getGenericParameterTypes();
	}

	@Override
	public ClassReader.DataType getGeneratedType() {
		return getReturnType();
	}

	@Override
	public Class<?> getRawGeneratedType() {
		return constructor.getDeclaringClass();
	}

	@Override
	public ClassReader.DataType getGenericGeneratedType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getName()
	 */
	@Override
	public String getName() {
		return constructor.getName();
	}

	public String getNameWithDescriptor() {
		return "<init>";
	}

	public String getDescriptor() {
		return "";
	}

	@Override
	public int getNumParameters() {
		return constructor.getGenericParameterTypes().length;
	}

	public ClassReader.DataType[] getParameterTypes() {
		ClassReader.DataType[] types = getExactParameterTypes(constructor, owner.getType());
		ClassReader.DataType[] rawTypes = null;//constructor.getParameterTypes();

		// Generic member classes should have the enclosing instance as a parameter
		// but don't for some reason
		if (rawTypes.length != types.length && owner.isParameterizedType()) {
			ClassReader.DataType[] actualTypes = new ClassReader.DataType[rawTypes.length];
			actualTypes[0] = owner.getOwnerType().getType();
			int pos = 1;
			for (ClassReader.DataType parameterType : types) {
				actualTypes[pos++] = parameterType;
			}
			return actualTypes;
		}
		return types;
	}

	public ClassReader.DataType[] getRawParameterTypes() {
		return null;
	}

	public ClassReader.DataType getReturnType() {
		return owner.getType();
	}

	@Override
	public TypeVariable<?>[] getTypeParameters() {
		return constructor.getTypeParameters();
	}

	@Override
	public boolean isAccessible() {
		return TestUsageChecker.canUse(constructor);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#isConstructor()
	 */
	@Override
	public boolean isConstructor() {
		return true;
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(constructor.getModifiers());
	}

	public boolean isOverloaded(List<VariableReference> parameters) {
		Class<?> declaringClass = constructor.getDeclaringClass();
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		boolean isExact = true;
		Class<?>[] parameterClasses = new Class<?>[parameters.size()];
		int num = 0;
		for (VariableReference parameter : parameters) {
			//parameterClasses[num] = parameter.getVariableClass();
			if (!parameterClasses[num].equals(parameterTypes[num])) {
				isExact = false;
				break;
			}
		}
		if (isExact)
			return false;
		try {
			for(Constructor<?> otherConstructor: declaringClass.getConstructors()) {
				if (otherConstructor.equals(constructor))
					continue;

				// If the number of parameters is different we can uniquely identify the constructor
				if(parameterTypes.length != otherConstructor.getParameterCount())
					continue;

				// Only if the parameters are assignable to both constructors do we need to care about overloading
				boolean parametersEqual = true;
				Class<?>[] otherParameterTypes = otherConstructor.getParameterTypes();
				for(int i = 0; i < parameterClasses.length; i++) {
//					if(parameters.get(i).isAssignableTo(parameterTypes[i]) !=
//					   parameters.get(i).isAssignableTo(otherParameterTypes[i])) {
//						parametersEqual = false;
//						break;
//					}
				}
				if(parametersEqual) {
					return true;
				}
			}
		} catch (SecurityException e) {
		}

		return false;
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		// Read/initialize additional fields
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#toString()
	 */
	@Override
	public String toString() {
		return constructor.toGenericString();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		oos.writeObject(constructor.getDeclaringClass().getName());
	}

	@Override
	public boolean isPublic() { return Modifier.isPublic(constructor.getModifiers()); }

	@Override
	public boolean isPrivate() { return Modifier.isPrivate(constructor.getModifiers()); }

	@Override
	public boolean isProtected() { return Modifier.isProtected(constructor.getModifiers()); }

	@Override
	public boolean isDefault() { return !isPublic() && !isPrivate() && !isProtected(); }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constructor == null) ? 0 : constructor.hashCode());
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
		GenericConstructor other = (GenericConstructor) obj;
		if (constructor == null) {
			if (other.constructor != null)
				return false;
		} else if (!constructor.equals(other.constructor))
			return false;
		return true;
	}


}
