package generator.utils.generic;

import generator.ClassReader;
import generator.DataType;
import generator.Field;
import generator.ga.ConstructionFailedException;
import generator.utils.GenericClass;
import org.apache.commons.lang3.ArrayUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.*;

public class GenericField extends GenericAccessibleObject<GenericField> {

	private static final long serialVersionUID = -2344346234923642901L;

	private transient Field field;

	public GenericField(Field field, GenericClass owner) {
		super(new GenericClass(owner));
		this.field = field;
	}

	public GenericField(Field field, ClassReader owner) {
		super(new GenericClass(owner));
		this.field = field;
	}

	public GenericField(Field field, DataType owner) {
		super(new GenericClass(owner));
		this.field = field;
	}

	@Override
	public GenericField copyWithNewOwner(GenericClass newOwner) {
		return new GenericField(field, newOwner);
	}

	@Override
	public GenericField copyWithOwnerFromReturnType(GenericClass returnType)
	        throws ConstructionFailedException {
		return new GenericField(field,
		        getOwnerClass().getGenericInstantiation(returnType.getTypeVariableMap()));
		/*
		if (returnType.isParameterizedType()) {
			GenericClass newOwner = new GenericClass(
			        getTypeFromExactReturnType(returnType.getType(), getOwnerType()));
			return new GenericField(field, newOwner);
		} else if (returnType.isArray()) {
			GenericClass newOwner = new GenericClass(
			        getTypeFromExactReturnType(returnType.getComponentType(),
			                                   getOwnerType()));
			return new GenericField(field, newOwner);
		} else if (returnType.isAssignableTo(getGeneratedType())) {
			return new GenericField(field, new GenericClass(owner));
		} else {
			throw new RuntimeException("Invalid return type: "
			        + returnType.getClassName() + " for field " + toString());
		}
		*/
	}

	@Override
	public DataType[] getTypeParameters() {

		if(field.getGenericType() instanceof TypeVariable) {
			return ArrayUtils.toArray(field.getGenericType());
		} else {
			return super.getTypeParameters();
		}
	}

	@Override
	public GenericField copy() {
		return new GenericField(field, new GenericClass(owner));
	}

	public Field getField() {
		return field;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getAccessibleObject()
	 */

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getDeclaringClass()
	 */
	@Override
	public Class<?> getDeclaringClass() {
		return field.getClass();
	}

	@Override
	public DataType getGeneratedType() {
		return getFieldType();
	}

	@Override
	public Class<?> getRawGeneratedType() {
		return field.getType().getClass();
	}

	@Override
	public DataType getGenericGeneratedType() {
		return (DataType) field.getGenericType();
	}

	public DataType getFieldType() {
//		return GenericTypeReflector.getExactFieldType(field, owner.getType());
		// 		try {
		// fieldType = field.getGenericType();
		// } catch (java.lang.reflect.GenericSignatureFormatError e) {
		// Ignore
		// fieldType = field.getType();
		// }
		return null;
	}

	public DataType getGenericFieldType() {
		return field.getGenericType();
	}

	@Override
	public boolean isAccessible() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#isField()
	 */
	@Override
	public boolean isField() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#isStatic()
	 */
	@Override
	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	public boolean isFinal() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#getName()
	 */
	@Override
	public String getName() {
		return field.getName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.GenericAccessibleObject#toString()
	 */
	@Override
	public String toString() {
		return field.toGenericString();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		oos.writeObject(field.getName());
		oos.writeObject(field.getName());
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		// Read/initialize additional fields
//		Class<?> methodClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass((String) ois.readObject());
//		String fieldName = (String) ois.readObject();
//
//		try {
//			field = methodClass.getDeclaredField(fieldName);
//			field.setAccessible(true);
//		} catch (SecurityException e) {
//		    throw new IllegalStateException("Unknown field for " + fieldName
//		                                    + " in class " + methodClass.getCanonicalName());
//		} catch (NoSuchFieldException e) {
//            throw new IllegalStateException("Unknown field for " + fieldName
//                                            + " in class " + methodClass.getCanonicalName());
//		}
	}


	@Override
	public boolean isPublic() { return Modifier.isPublic(field.getModifiers()); }

	@Override
	public boolean isPrivate() { return Modifier.isPrivate(field.getModifiers()); }

	@Override
	public boolean isProtected() { return Modifier.isProtected(field.getModifiers()); }

	@Override
	public boolean isDefault() { return !isPublic() && !isPrivate() && !isProtected(); }



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		GenericField other = (GenericField) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}


}
