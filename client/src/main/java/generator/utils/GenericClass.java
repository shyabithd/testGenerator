package generator.utils;

import generator.ga.ConstructionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericClass implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(GenericClass.class);

	private static List<String> primitiveClasses = Arrays.asList("char", "int", "short",
	                                                             "long", "boolean",
	                                                             "float", "double",
	                                                             "byte");

	private static final long serialVersionUID = -3307107227790458308L;

	/**
	 * Set of wrapper classes
	 */
	private static final Set<Class<?>> WRAPPER_TYPES = new LinkedHashSet<Class<?>>(
	        Arrays.asList(Boolean.class, Character.class, Byte.class, Short.class,
	                      Integer.class, Long.class, Float.class, Double.class,
	                      Void.class));

	protected static Type addTypeParameters(Class<?> clazz) {
		return null;
	}

	/**
	 * Returns the erasure of the given type.
	 */
	private static Class<?> erase(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof TypeVariable) {
			TypeVariable<?> tv = (TypeVariable<?>) type;
			if (tv.getBounds().length == 0)
				return Object.class;
			else
				return erase(tv.getBounds()[0]);
		} else {
			// TODO at least support CaptureType here
			throw new RuntimeException("not supported: " + type.getClass());
		}
	}

	private static Class<?> getClass(String name) throws ClassNotFoundException {
		return null;
	}

	private static Class<?> getClass(String name, ClassLoader loader)
	        throws ClassNotFoundException {
		if (name.equals("void"))
			return void.class;
		else if (name.equals("int") || name.equals("I"))
			return int.class;
		else if (name.equals("short") || name.equals("S"))
			return short.class;
		else if (name.equals("long") || name.equals("J"))
			return long.class;
		else if (name.equals("float") || name.equals("F"))
			return float.class;
		else if (name.equals("double") || name.equals("D"))
			return double.class;
		else if (name.equals("boolean") || name.equals("Z"))
			return boolean.class;
		else if (name.equals("byte") || name.equals("B"))
			return byte.class;
		else if (name.equals("char") || name.equals("C"))
			return char.class;
		else if (name.startsWith("[")) {
			Class<?> componentType = getClass(name.substring(1, name.length()), loader);
			Object array = Array.newInstance(componentType, 0);
			return array.getClass();
		} else if (name.startsWith("L") && name.endsWith(";")) {
			return getClass(name.substring(1, name.length() - 1), loader);
		} else if (name.endsWith(";")) {
			return getClass(name.substring(0, name.length() - 1), loader);
		} else if (name.endsWith(".class")) {
			return getClass(name.replace(".class", ""), loader);
		} else
			return loader.loadClass(name);
	}

	/**
	 * <p>
	 * isAssignable
	 * </p>
	 * 
	 * @param lhsType
	 *            a {@link Type} object.
	 * @param rhsType
	 *            a {@link Type} object.
	 * @return a boolean.
	 */
	public static boolean isAssignable(Type lhsType, Type rhsType) {
		if (rhsType == null || lhsType == null)
			return false;
		return true;
	}

	public static boolean isMissingTypeParameters(Type type) {
		if (type instanceof Class) {
			for (Class<?> clazz = (Class<?>) type; clazz != null; clazz = clazz.getEnclosingClass()) {
				if (clazz.getTypeParameters().length != 0)
					return true;
			}
			return false;
		} else if (type instanceof ParameterizedType) {
			return false;
		} else if(type instanceof GenericArrayType) {
			return false;
		} else if(type instanceof TypeVariable) {
			return false;
		} else if(type instanceof WildcardType) {
			return false;
		} else {
			throw new AssertionError("Unexpected type " + type.getClass());
		}
	}

	/**
	 * <p>
	 * isSubclass
	 * </p>
	 *
	 * @param superclass
	 *            a {@link Type} object.
	 * @param subclass
	 *            a {@link Type} object.
	 * @return a boolean.
	 */
	public static boolean isSubclass(Type superclass, Type subclass) {
//		List<Class<?>> superclasses = ClassUtils.getAllSuperclasses((Class<?>) subclass);
//		List<Class<?>> interfaces = ClassUtils.getAllInterfaces((Class<?>) subclass);
//		if (superclasses.contains(superclass) || interfaces.contains(superclass)) {
//			return true;
//		}

		return false;
	}

	transient Class<?> rawClass = null;

	transient Type type = null;

	/**
	 * Generate a generic class by setting all generic parameters to their
	 * parameter types
	 *
	 * @param clazz
	 *            a {@link Class} object.
	 */
	public GenericClass(Class<?> clazz) {
		this.type = addTypeParameters(clazz); //GenericTypeReflector.addWildcardParameters(clazz);
		this.rawClass = clazz;
	}

	public GenericClass(GenericClass copy) {
		this.type = copy.type;
		this.rawClass = copy.rawClass;
	}

	/**
	 * Generate a generic class by from a type
	 *
	 * @param type
	 *            a {@link Type} object.
	 */
	public GenericClass(Type type) {
		if (type instanceof Class<?>) {
			this.type = addTypeParameters((Class<?>) type); //GenericTypeReflector.addWildcardParameters((Class<?>) type);
			this.rawClass = (Class<?>) type;
		} else {
			if (!handleGenericArraySpecialCase(type)) {
				this.type = type;
				try {
					this.rawClass = erase(type);
				} catch (RuntimeException e) {
					// If there is an unresolved capture type in here
					// we delete it and replace with a wildcard
					this.rawClass = Object.class;
				}
			}
		}
	}

	/**
	 * Generate a GenericClass with this exact generic type and raw class
	 *
	 * @param type
	 * @param clazz
	 */
	public GenericClass(Type type, Class<?> clazz) {
		this.type = type;
		this.rawClass = clazz;
		handleGenericArraySpecialCase(type);
	}

	/**
	 * Determine if there exists an instantiation of the type variables such
	 * that the class matches otherType
	 *
	 * @param otherType
	 *            is the class we want to generate
	 * @return
	 */
	public boolean canBeInstantiatedTo(GenericClass otherType) {

		if (isPrimitive() && otherType.isWrapperType())
			return false;

		if (isAssignableTo(otherType))
			return true;

		if (!isTypeVariable() && !otherType.isTypeVariable()) {
			try {
				if (otherType.isGenericSuperTypeOf(this))
					return true;
			} catch (RuntimeException e) {
				// FIXME: GentyRef sometimes throws:
				// java.lang.RuntimeException: not implemented: class sun.reflect.generics.reflectiveObjects.TypeVariableImpl
				// While I have no idea why, it should be safe to proceed if we can ignore this type
				return false;
			}

		}

		Class<?> otherRawClass = otherType.getRawClass();
		if (otherRawClass.isAssignableFrom(rawClass)) {
			//logger.debug("Raw classes are assignable: " + otherType + ", have: "
			//        + toString());
			Map<TypeVariable<?>, Type> typeMap = otherType.getTypeVariableMap();

			//logger.debug(typeMap.toString());
			try {
				GenericClass instantiation = getGenericInstantiation(typeMap);
				if (equals(instantiation)) {
					//logger.debug("Instantiation is equal to original, so I think we can't assign: "
					//        + instantiation);
					if (hasWildcardOrTypeVariables())
						return false;
					else
						return true;
				}
				//logger.debug("Checking instantiation: " + instantiation);
				return instantiation.canBeInstantiatedTo(otherType);
			} catch (Throwable e) {
				logger.debug("Failed to instantiate " + toString());
				return false;
			}
		}

		return false;
	}

	/**
	 * <p>
	 * changeClassLoader
	 * </p>
	 *
	 * @param loader
	 *            a {@link ClassLoader} object.
	 */
	public void changeClassLoader(ClassLoader loader) {
		try {
			if (rawClass != null)
				rawClass = getClass(rawClass.getName(), loader);
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				// GenericClass rawType = new GenericClass(pt.getRawType());
				// rawType.changeClassLoader(loader);
				GenericClass ownerType = null;
				if (pt.getOwnerType() != null) {
					ownerType = new GenericClass(pt.getOwnerType());
					// ownerType.type = pt.getOwnerType();
					ownerType.changeClassLoader(loader);
				}
				List<GenericClass> parameterClasses = new ArrayList<GenericClass>();
				for (Type parameterType : pt.getActualTypeArguments()) {
					GenericClass parameter = new GenericClass(parameterType);
					// parameter.type = parameterType;
					parameter.changeClassLoader(loader);
					parameterClasses.add(parameter);
				}
				Type[] parameterTypes = new Type[parameterClasses.size()];
				for (int i = 0; i < parameterClasses.size(); i++)
					parameterTypes[i] = parameterClasses.get(i).getType();
			} else if (type instanceof GenericArrayType) {
				GenericClass componentClass = getComponentClass();
				componentClass.changeClassLoader(loader);
				//this.type = GenericArrayTypeImpl.createArrayType(componentClass.getType());
			} else if (type instanceof WildcardType) {
				Type[] oldUpperBounds = ((WildcardType) type).getUpperBounds();
				Type[] oldLowerBounds = ((WildcardType) type).getLowerBounds();
				Type[] upperBounds = new Type[oldUpperBounds.length];
				Type[] lowerBounds = new Type[oldLowerBounds.length];

				for (int i = 0; i < oldUpperBounds.length; i++) {
					GenericClass bound = new GenericClass(oldUpperBounds[i]);
					// bound.type = oldUpperBounds[i];
					bound.changeClassLoader(loader);
					upperBounds[i] = bound.getType();
				}
				for (int i = 0; i < oldLowerBounds.length; i++) {
					GenericClass bound = new GenericClass(oldLowerBounds[i]);
					// bound.type = oldLowerBounds[i];
					bound.changeClassLoader(loader);
					lowerBounds[i] = bound.getType();
				}
				//this.type = new WildcardTypeImpl(upperBounds, lowerBounds);
			} else if (type instanceof TypeVariable<?>) {
				for (TypeVariable<?> newVar : rawClass.getTypeParameters()) {
					if (newVar.getName().equals(((TypeVariable<?>) type).getName())) {
						this.type = newVar;
						break;
					}
				}
			} else {
				this.type = addTypeParameters(rawClass); //GenericTypeReflector.addWildcardParameters(raw_class);
			}
		} catch (ClassNotFoundException e) {
			logger.warn("Class not found: " + rawClass + " - keeping old class loader ",
			            e);
		} catch (SecurityException e) {
			logger.warn("Class not found: " + rawClass + " - keeping old class loader ",
			            e);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericClass other = (GenericClass) obj;
		//return type.equals(other.type);
		return getTypeName().equals(other.getTypeName());
		/*
		if (raw_class == null) {
			if (other.raw_class != null)
				return false;
		} else if (!raw_class.equals(other.raw_class))
			return false;
			*/
		/*
		if (type == null) {
		    if (other.type != null)
			    return false;
		} else if (!type.equals(other.type))
		    return false;
		    */
		// return true;
	}

	public Class<?> getBoxedType() {
		if (isPrimitive()) {
			if (rawClass.equals(int.class))
				return Integer.class;
			else if (rawClass.equals(byte.class))
				return Byte.class;
			else if (rawClass.equals(short.class))
				return Short.class;
			else if (rawClass.equals(long.class))
				return Long.class;
			else if (rawClass.equals(float.class))
				return Float.class;
			else if (rawClass.equals(double.class))
				return Double.class;
			else if (rawClass.equals(char.class))
				return Character.class;
			else if (rawClass.equals(boolean.class))
				return Boolean.class;
			else if (rawClass.equals(void.class))
				return Void.class;
			else
				throw new RuntimeException("Unknown unboxed type: " + rawClass);
		}
		return rawClass;
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getClassName() {
		return rawClass.getName();
	}

	public GenericClass getComponentClass() {
		if (type instanceof GenericArrayType) {
			GenericArrayType arrayType = (GenericArrayType) type;
			Type componentType = arrayType.getGenericComponentType();
			Class<?> rawComponentType = rawClass.getComponentType();
			return new GenericClass(componentType, rawComponentType);
		} else {
			return new GenericClass(rawClass.getComponentType());
		}
	}

	/**
	 * <p>
	 * getComponentName
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getComponentName() {
		return rawClass.getComponentType().getSimpleName();
	}

	public Collection<GenericClass> getGenericBounds() {
		Set<GenericClass> bounds = new LinkedHashSet<GenericClass>();

		if (isRawClass() || !hasWildcardOrTypeVariables()) {
			return bounds;
		}

		if (isWildcardType()) {
			getGenericWildcardBounds(bounds);
		} else if (isArray()) {
			bounds.addAll(getComponentClass().getGenericBounds());
		} else if (isTypeVariable()) {
			getGenericTypeVarBounds(bounds);
		} else if (isParameterizedType()) {
			getGenericParameterizedTypeBounds(bounds);
		}
		return bounds;
	}

	private void getGenericWildcardBounds(Collection<GenericClass> bounds) {
		for(Type t : ((WildcardType)type).getUpperBounds()) {
			bounds.add(new GenericClass(t));
		}
		for(Type t : ((WildcardType)type).getLowerBounds()) {
			bounds.add(new GenericClass(t));
		}
	}

	private void getGenericTypeVarBounds(Collection<GenericClass> bounds) {
		for(Type t : ((TypeVariable<?>)type).getBounds()) {
			bounds.add(new GenericClass(t));
		}
	}

	private void getGenericParameterizedTypeBounds(Collection<GenericClass> bounds) {
		for(TypeVariable<?> typeVar : getTypeVariables()) {
			for(Type t : typeVar.getBounds()) {
				bounds.add(new GenericClass(t));
			}
		}
	}


	public GenericClass getGenericInstantiation() throws ConstructionFailedException
	{
		return getGenericInstantiation(new HashMap<TypeVariable<?>, Type>());
	}


	public GenericClass getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap) throws ConstructionFailedException {
		return getGenericInstantiation(typeMap, 0);
	}

	private GenericClass getGenericInstantiation(Map<TypeVariable<?>, Type> typeMap,
	        int recursionLevel) throws ConstructionFailedException {

		logger.debug("Instantiation " + toString() + " with type map " + typeMap);
		// If there are no type variables, create copy
		if (isRawClass() || !hasWildcardOrTypeVariables() || recursionLevel > 12) {
			logger.debug("Nothing to replace: " + toString() + ", " + isRawClass() + ", "
			        + hasWildcardOrTypeVariables());
			return new GenericClass(this);
		}

		if (isWildcardType()) {
			logger.debug("Is wildcard type.");
			return getGenericWildcardInstantiation(typeMap, recursionLevel);
		} else if (isArray()) {
			return getGenericArrayInstantiation(typeMap, recursionLevel);
		} else if (isTypeVariable()) {
			logger.debug("Is type variable ");
			return getGenericTypeVariableInstantiation(typeMap, recursionLevel);
		} else if (isParameterizedType()) {
			logger.debug("Is parameterized type");
			return getGenericParameterizedTypeInstantiation(typeMap, recursionLevel);
		}
		// TODO

		return null;
	}

	private GenericClass getGenericArrayInstantiation(Map<TypeVariable<?>, Type> typeMap,
	        int recursionLevel) throws ConstructionFailedException {
		GenericClass componentClass = getComponentClass().getGenericInstantiation();
		return getWithComponentClass(componentClass);
	}

	private GenericClass getGenericTypeVariableInstantiation(
	        Map<TypeVariable<?>, Type> typeMap, int recursionLevel)  throws ConstructionFailedException {
		if (typeMap.containsKey(type)) {
			logger.debug("Type contains {}: {}", toString(), typeMap);
			if(typeMap.get(type) == type) {
				// FIXXME: How does this happen?
				// throw new ConstructionFailedException("Type points to itself");
			}
			//TODO: If typeMap.get(type) is a wildcard we need to keep the bounds of the
			//      type variable in mind anyway, so this needs to be rewritten/fixed.
			GenericClass selectedClass = new GenericClass(typeMap.get(type)).getGenericInstantiation(typeMap,
					recursionLevel + 1);
			if (!selectedClass.satisfiesBoundaries((TypeVariable<?>) type)) {
				logger.debug("Cannot be instantiated to: {}", selectedClass);
			} else {
				logger.debug("Can be instantiated to: {}", selectedClass);
				return selectedClass;
			}
		}
		logger.debug("Type map does not contain {}: {}", toString(), typeMap);

//		GenericClass selectedClass = CastClassManager.getInstance().selectCastClass((TypeVariable<?>) type,
//				recursionLevel < Properties.MAX_GENERIC_DEPTH,
//				typeMap);

//		if (selectedClass == null) {
//			throw new ConstructionFailedException("Unable to instantiate "
//					+ toString());
//		}
		//logger.debug("Getting instantiation of type variable {}: {}", toString());
		Map<TypeVariable<?>, Type> extendedMap = new HashMap<TypeVariable<?>, Type>(
				typeMap);
		extendedMap.putAll(getTypeVariableMap());
		for (Type bound : ((TypeVariable<?>) type).getBounds()) {
			logger.debug("Current bound of variable {}: {}", type, bound);
			GenericClass boundClass = new GenericClass(bound);
			extendedMap.putAll(boundClass.getTypeVariableMap());
			if(boundClass.isParameterizedType()) {
				Class<?> boundRawClass = boundClass.getRawClass();
//				if(boundRawClass.isAssignableFrom(selectedClass.getRawClass())) {
//					Map<TypeVariable<?>, Type> xmap = TypeUtils.determineTypeArguments(selectedClass.getRawClass(), (ParameterizedType) boundClass.getType());
//					extendedMap.putAll(xmap);
//				}
			}
		}

		logger.debug("Updated type variable map to {}", extendedMap);

		GenericClass instantiation = null;//= selectedClass.getGenericInstantiation(extendedMap,
				//recursionLevel + 1);
		typeMap.put((TypeVariable<?>) type, instantiation.getType());
		return instantiation;

	}


	private GenericClass getGenericWildcardInstantiation(
	        Map<TypeVariable<?>, Type> typeMap, int recursionLevel) {
		return null;
//		GenericClass selectedClass = CastClassManager.getInstance().selectCastClass((WildcardType) type,
//		                                                                            recursionLevel < Properties.MAX_GENERIC_DEPTH,
//		                                                                            typeMap);
//		return selectedClass.getGenericInstantiation(typeMap, recursionLevel + 1);
	}

	public List<GenericClass> getInterfaces() {
		List<GenericClass> ret = new ArrayList<GenericClass>();
		for(Class<?> intf : rawClass.getInterfaces()) {
			ret.add(new GenericClass(intf));
		}
		return ret;
	}

	private GenericClass getGenericParameterizedTypeInstantiation(
	        Map<TypeVariable<?>, Type> typeMap, int recursionLevel) throws ConstructionFailedException {

		// FIXME: This negatively affects coverage. Why was it added?
		//
		//		if(isClass() && !hasTypeVariables()) {
		//			return this;
		//		}

		List<TypeVariable<?>> typeParameters = getTypeVariables();

		Type[] parameterTypes = new Type[typeParameters.size()];
		Type ownerType = null;

		int numParam = 0;

		for (GenericClass parameterClass : getParameterClasses()) {
			logger.debug("Current parameter to instantiate",  parameterClass);
			/*
			 * If the parameter is a parameterized type variable such as T extends Map<String, K extends Number>
			 * then the boundaries of the parameters of the type variable need to be respected
			 */
			if (!parameterClass.hasWildcardOrTypeVariables()) {
				logger.debug("Parameter has no wildcard or type variable");
				parameterTypes[numParam++] = parameterClass.getType();
			} else {
				logger.debug("Current parameter has type variables: " + parameterClass);

				Map<TypeVariable<?>, Type> extendedMap = new HashMap<TypeVariable<?>, Type>(
				        typeMap);
				extendedMap.putAll(parameterClass.getTypeVariableMap());
				if(!extendedMap.containsKey(typeParameters.get(numParam)) && !parameterClass.isTypeVariable())
					extendedMap.put(typeParameters.get(numParam), parameterClass.getType());
				logger.debug("New type map: " + extendedMap);

				if (parameterClass.isWildcardType()) {
					logger.debug("Is wildcard type, here we should value the wildcard boundaries");
					logger.debug("Wildcard boundaries: "+parameterClass.getGenericBounds());
					logger.debug("Boundaries of underlying var: "+Arrays.asList(typeParameters.get(numParam).getBounds()));
					GenericClass parameterInstance = parameterClass.getGenericWildcardInstantiation(extendedMap, recursionLevel + 1);
					//GenericClass parameterTypeClass = new GenericClass(typeParameters.get(numParam));
//					if(!parameterTypeClass.isAssignableFrom(parameterInstance)) {
					if(!parameterInstance.satisfiesBoundaries(typeParameters.get(numParam))) {
						throw new ConstructionFailedException("Invalid generic instance");
					}
					//GenericClass parameterInstance = new GenericClass(
					//        typeParameters.get(numParam)).getGenericInstantiation(extendedMap,
					//                                                              recursionLevel + 1);
					parameterTypes[numParam++] = parameterInstance.getType();
				} else {
					logger.debug("Is not wildcard but type variable? "
					        + parameterClass.isTypeVariable());

					GenericClass parameterInstance = parameterClass.getGenericInstantiation(extendedMap,
					                                                                        recursionLevel + 1);
					parameterTypes[numParam++] = parameterInstance.getType();
				}
			}
		}

		if (hasOwnerType()) {
			GenericClass ownerClass = getOwnerType().getGenericInstantiation(typeMap,
			                                                                 recursionLevel);
			ownerType = ownerClass.getType();
		}

//		return new GenericClass(new ParameterizedTypeImpl(rawClass, parameterTypes,
//		        ownerType));
		return null;
	}

	/**
	 * Retrieve number of generic type parameters
	 *
	 * @return
	 */
	public int getNumParameters() {
		if (type instanceof ParameterizedType) {
			return Arrays.asList(((ParameterizedType) type).getActualTypeArguments()).size();
		}
		return 0;
	}

	/**
	 * Retrieve the generic owner
	 *
	 * @return
	 */
	public GenericClass getOwnerType() {
		return new GenericClass(((ParameterizedType) type).getOwnerType());
	}

	/**
	 * Retrieve list of actual parameters
	 *
	 * @return
	 */
	public List<Type> getParameterTypes() {
		if (type instanceof ParameterizedType) {
			return Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
		}
		return new ArrayList<Type>();
	}

	/**
	 * Retrieve list of parameter classes
	 *
	 * @return
	 */
	public List<GenericClass> getParameterClasses() {
		if (type instanceof ParameterizedType) {
			List<GenericClass> parameters = new ArrayList<GenericClass>();
			for (Type parameterType : ((ParameterizedType) type).getActualTypeArguments()) {
				parameters.add(new GenericClass(parameterType));
			}
			return parameters;
		}
		return new ArrayList<GenericClass>();
	}

	/**
	 * <p>
	 * getRawClass
	 * </p>
	 *
	 * @return a {@link Class} object.
	 */
	public Class<?> getRawClass() {
		return rawClass;
	}

	public GenericClass getRawGenericClass() {
		return new GenericClass(rawClass);
	}

	/**
	 * <p>
	 * Getter for the field <code>type</code>.
	 * </p>
	 *
	 * @return a {@link Type} object.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * <p>
	 * getTypeName
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getTypeName() {
		return "";
	}

	private Map<TypeVariable<?>, Type> typeVariableMap = null;

	public Map<TypeVariable<?>, Type> getTypeVariableMap() {
		if(typeVariableMap != null)
			return typeVariableMap;
		//logger.debug("Getting type variable map for " + type);
		List<TypeVariable<?>> typeVariables = getTypeVariables();
		List<Type> types = getParameterTypes();
		Map<TypeVariable<?>, Type> typeMap = new LinkedHashMap<TypeVariable<?>, Type>();
		try {
			if (rawClass.getSuperclass() != null
			        && !rawClass.isAnonymousClass()
			        && !rawClass.getSuperclass().isAnonymousClass()
			        && !(hasOwnerType() && getOwnerType().getRawClass().isAnonymousClass())) {
				GenericClass superClass = null;//= getSuperClass();
				//logger.debug("Superclass of " + type + ": " + superClass);
				Map<TypeVariable<?>, Type> superMap = superClass.getTypeVariableMap();
				//logger.debug("Super map after " + superClass + ": " + superMap);
				typeMap.putAll(superMap);
			}
			for(Class<?> interFace : rawClass.getInterfaces()) {
				GenericClass interFaceClass = new GenericClass(interFace);
				//logger.debug("Interface of " + type + ": " + interFaceClass);
				Map<TypeVariable<?>, Type> superMap = interFaceClass.getTypeVariableMap();
				//logger.debug("Super map after " + superClass + ": " + superMap);
				typeMap.putAll(superMap);
			}
			if(isTypeVariable()) {
				for(Type boundType : ((TypeVariable<?>)type).getBounds()) {
					GenericClass boundClass = new GenericClass(boundType);
					typeMap.putAll(boundClass.getTypeVariableMap());
				}
			}

		} catch (Exception e) {
			logger.debug("Exception while getting type map: " + e);
		}
		for (int i = 0; i < typeVariables.size(); i++) {
			if (types.get(i) != typeVariables.get(i)) {
				typeMap.put(typeVariables.get(i), types.get(i));
			}
		}

		//logger.debug("Type map: " + typeMap);
		typeVariableMap = typeMap;
		return typeMap;
	}

	/**
	 * Return a list of type variables of this type, or an empty list if this is
	 * not a parameterized type
	 *
	 * @return
	 */
	public List<TypeVariable<?>> getTypeVariables() {
		List<TypeVariable<?>> typeVariables = new ArrayList<TypeVariable<?>>();
		if (type instanceof ParameterizedType) {
			//logger.debug("Type variables of "+rawClass+": ");
			//for(TypeVariable<?> var : rawClass.getTypeParameters()) {
			//	logger.debug("Var "+var+" of "+var.getGenericDeclaration());
			//}
			typeVariables.addAll(Arrays.asList(rawClass.getTypeParameters()));
		}
		return typeVariables;
	}

	public Class<?> getUnboxedType() {
		if (isWrapperType()) {
			if (rawClass.equals(Integer.class))
				return int.class;
			else if (rawClass.equals(Byte.class))
				return byte.class;
			else if (rawClass.equals(Short.class))
				return short.class;
			else if (rawClass.equals(Long.class))
				return long.class;
			else if (rawClass.equals(Float.class))
				return float.class;
			else if (rawClass.equals(Double.class))
				return double.class;
			else if (rawClass.equals(Character.class))
				return char.class;
			else if (rawClass.equals(Boolean.class))
				return boolean.class;
			else if (rawClass.equals(Void.class))
				return void.class;
			else
				throw new RuntimeException("Unknown boxed type: " + rawClass);
		}
		return rawClass;
	}

	public GenericClass getWithComponentClass(GenericClass componentClass) {
		if (type instanceof GenericArrayType) {
//			return new GenericClass(
//			        GenericArrayTypeImpl.createArrayType(componentClass.getType()),
//			        rawClass);
		} else {
			return new GenericClass(type, rawClass);
		}
		return null;
	}

	public GenericClass getWithGenericParameterTypes(List<GenericClass> parameters) {
		Type[] typeArray = new Type[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			typeArray[i] = parameters.get(i).getType();
		}
		Type ownerType = null;
		if (type instanceof ParameterizedType) {
			ownerType = ((ParameterizedType) type).getOwnerType();
		}

//		return new GenericClass(new ParameterizedTypeImpl(rawClass, typeArray, ownerType));
		return null;
	}

	public GenericClass getWithOwnerType(GenericClass ownerClass) {
		if (type instanceof ParameterizedType) {
			ParameterizedType currentType = (ParameterizedType) type;
//			return new GenericClass(new ParameterizedTypeImpl(rawClass,
//			        currentType.getActualTypeArguments(), ownerClass.getType()));
		}

		return new GenericClass(type);
	}

	/**
	 * If this is a LinkedList<?> and the super class is a List<Integer> then
	 * this returns a LinkedList<Integer>
	 *
	 * @param superClass
	 * @return
	 * @throws ConstructionFailedException
	 */
	public GenericClass getWithParametersFromSuperclass(GenericClass superClass)
	        throws ConstructionFailedException {
		GenericClass exactClass = new GenericClass(type);
		if (!(type instanceof ParameterizedType)) {
			exactClass.type = type;
			return exactClass;
		}
		ParameterizedType pType = (ParameterizedType) type;

//		if (superClass.isParameterizedType()) {
//			Map<TypeVariable<?>, Type> typeMap = TypeUtils.determineTypeArguments(rawClass,
//			                                                                      (ParameterizedType) superClass.getType());
//			return getGenericInstantiation(typeMap);
//		}

		Class<?> targetClass = superClass.getRawClass();
		Class<?> currentClass = rawClass;
		Type[] parameterTypes = new Type[superClass.getNumParameters()];
		superClass.getParameterTypes().toArray(parameterTypes);

		if (targetClass.equals(currentClass)) {
			logger.info("Raw classes match, setting parameters to: "
			        + superClass.getParameterTypes());
			//exactClass.type = new ParameterizedTypeImpl(currentClass, parameterTypes,
			  //      pType.getOwnerType());
		} else {
			Type ownerType = pType.getOwnerType();
			Map<TypeVariable<?>, Type> superTypeMap = superClass.getTypeVariableMap();
			Type[] origArguments = pType.getActualTypeArguments();
			Type[] arguments = new Type[origArguments.length];
			// For some reason, doing this would lead to arguments being
			// of component type TypeVariable, which would lead to
			// ArrayStoreException if we try to assign a WildcardType
			//Type[] arguments = Arrays.copyOf(origArguments, origArguments.length);
			for(int i = 0; i < origArguments.length; i++)
				arguments[i] = origArguments[i];
			List<TypeVariable<?>> variables = getTypeVariables();
			for (int i = 0; i < arguments.length; i++) {
				TypeVariable<?> var = variables.get(i);
				if (superTypeMap.containsKey(var)) {
					arguments[i] = superTypeMap.get(var);
					logger.info("Setting type variable " + var + " to "
					        + superTypeMap.get(var));
				} else if (arguments[i] instanceof WildcardType
				        && i < parameterTypes.length) {
					logger.info("Replacing wildcard with " + parameterTypes[i]);
//					logger.info("Lower Bounds: "
//					        + Arrays.asList(TypeUtils.getImplicitLowerBounds((WildcardType) arguments[i])));
//					logger.info("Upper Bounds: "
//					        + Arrays.asList(TypeUtils.getImplicitUpperBounds((WildcardType) arguments[i])));
//					logger.info("Type variable: " + variables.get(i));
//					if (!TypeUtils.isAssignable(parameterTypes[i], arguments[i])) {
//						logger.info("Not assignable to bounds!");
//						return null;
//					} else {
//						boolean assignable = false;
//						for (Type bound : variables.get(i).getBounds()) {
//							if (TypeUtils.isAssignable(parameterTypes[i], bound)) {
//								assignable = true;
//								break;
//							}
//						}
//						if (!assignable) {
//							logger.info("Not assignable to type variable!");
//							return null;
//						}
//					}
					arguments[i] = parameterTypes[i];
				}
			}
//			GenericClass ownerClass = new GenericClass(ownerType).getWithParametersFromSuperclass(superClass);
//			if (ownerClass == null)
//				return null;
//			exactClass.type = new ParameterizedTypeImpl(currentClass, arguments,
//			        ownerClass.getType());
		}

		//return exactClass;
		return null;
	}

	private boolean handleGenericArraySpecialCase(Type type) {
		if (type instanceof GenericArrayType) {
			// There is some weird problem with generic methods and the component type can be null
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			if (componentType == null) {
				this.rawClass = Object[].class;
				this.type = this.rawClass;
				return true;
			}
		}

		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getTypeName().hashCode();
		//result = prime * result + ((raw_class == null) ? 0 : raw_class.hashCode());
		//result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public boolean hasOwnerType() {
		if (type instanceof ParameterizedType)
			return ((ParameterizedType) type).getOwnerType() != null;
		else
			return false;
	}

	public boolean hasTypeVariables() {
		if (isParameterizedType()) {
			return hasTypeVariables((ParameterizedType) type);
		}

		if (isTypeVariable())
			return true;

		return false;
	}

	private boolean hasTypeVariables(ParameterizedType parameterType) {
		for (Type t : parameterType.getActualTypeArguments()) {
			if (t instanceof TypeVariable)
				return true;
			else if (t instanceof ParameterizedType) {
				if (hasTypeVariables((ParameterizedType) t))
					return true;
			}
		}

		return false;
	}

	public boolean hasWildcardOrTypeVariables() {
		if (isTypeVariable() || isWildcardType())
			return true;

		if (hasWildcardTypes())
			return true;

		if (hasTypeVariables())
			return true;

		if (hasOwnerType()) {
			if (getOwnerType().hasWildcardOrTypeVariables())
				return true;
		}

		if (type instanceof GenericArrayType) {
			if (getComponentClass().hasWildcardOrTypeVariables())
				return true;
		}

		return false;
	}

	private boolean hasWildcardType(ParameterizedType parameterType) {
		for (Type t : parameterType.getActualTypeArguments()) {
			if (t instanceof WildcardType)
				return true;
			else if (t instanceof ParameterizedType) {
				if (hasWildcardType((ParameterizedType) t))
					return true;
			}
		}

		return false;
	}

	public boolean hasWildcardTypes() {
		if (isParameterizedType()) {
			return hasWildcardType((ParameterizedType) type);
		}

		if (isWildcardType())
			return true;

		return false;
	}

	/**
	 * True if this represents an abstract class
	 *
	 * @return
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(rawClass.getModifiers());
	}

	/**
	 * True if this is an anonymous class
	 *
	 * @return
	 */
	public boolean isAnonymous() {
		return rawClass.isAnonymousClass();
	}

	/**
	 * Return true if variable is an array
	 *
	 * @return a boolean.
	 */
	public boolean isArray() {
		return rawClass.isArray();
	}

	/**
	 * <p>
	 * isAssignableFrom
	 * </p>
	 *
	 * @param rhsType
	 *            a {@link GenericClass} object.
	 * @return a boolean.
	 */
	public boolean isAssignableFrom(GenericClass rhsType) {
		return isAssignable(type, rhsType.type);
	}

	/**
	 * <p>
	 * isAssignableFrom
	 * </p>
	 *
	 * @param rhsType
	 *            a {@link Type} object.
	 * @return a boolean.
	 */
	public boolean isAssignableFrom(Type rhsType) {
		return isAssignable(type, rhsType);
	}

	/**
	 * <p>
	 * isAssignableTo
	 * </p>
	 *
	 * @param lhsType
	 *            a {@link GenericClass} object.
	 * @return a boolean.
	 */
	public boolean isAssignableTo(GenericClass lhsType) {
		return isAssignable(lhsType.type, type);
	}

	/**
	 * <p>
	 * isAssignableTo
	 * </p>
	 *
	 * @param lhsType
	 *            a {@link Type} object.
	 * @return a boolean.
	 */
	public boolean isAssignableTo(Type lhsType) {
		return isAssignable(lhsType, type);
	}

	/**
	 * True if this represents java.lang.Class
	 * 
	 * @return
	 */
	public boolean isClass() {
		return rawClass.equals(Class.class);
	}

	/**
	 * Return true if variable is an enumeration
	 * 
	 * @return a boolean.
	 */
	public boolean isEnum() {
		return rawClass.isEnum();
	}

	public boolean isGenericArray() {
		GenericClass componentClass = new GenericClass(rawClass.getComponentType());
		return componentClass.hasWildcardOrTypeVariables();
	}

	/**
	 * Determine if subType is a generic subclass
	 * 
	 * @param subType
	 * @return
	 */
	public boolean isGenericSuperTypeOf(GenericClass subType) {
		return true;
	}

	/**
	 * True is this represents java.lang.Object
	 * 
	 * @return
	 */
	public boolean isObject() {
		return rawClass.equals(Object.class);
	}

	/**
	 * True if this represents a parameterized generic type
	 * 
	 * @return
	 */
	public boolean isParameterizedType() {
		return type instanceof ParameterizedType;
	}

	/**
	 * Return true if variable is a primitive type
	 * 
	 * @return a boolean.
	 */
	public boolean isPrimitive() {
		return rawClass.isPrimitive();
	}

	/**
	 * True if this is a non-generic type
	 * 
	 * @return
	 */
	public boolean isRawClass() {
		return type instanceof Class<?>;
	}

	/**
	 * True if this is a type variable
	 * 
	 * @return
	 */
	public boolean isTypeVariable() {
		return type instanceof TypeVariable<?>;
	}

	/**
	 * True if this is a wildcard type
	 * 
	 * @return
	 */
	public boolean isWildcardType() {
		return type instanceof WildcardType;
	}

	/**
	 * True if this represents java.lang.String
	 * 
	 * @return a boolean.
	 */
	public boolean isString() {
		return rawClass.equals(String.class);
	}

	/**
	 * Return true if variable is void
	 * 
	 * @return a boolean.
	 */
	public boolean isVoid() {
		return rawClass.equals(Void.class) || rawClass.equals(void.class);
	}

	/**
	 * Return true if type of variable is a primitive wrapper
	 * 
	 * @return a boolean.
	 */
	public boolean isWrapperType() {
		return WRAPPER_TYPES.contains(rawClass);
	}

	public boolean satisfiesBoundaries(TypeVariable<?> typeVariable) {
		return satisfiesBoundaries(typeVariable, getTypeVariableMap());
	}

	/**
	 * Determine whether the boundaries of the type variable are satisfied by
	 * this class
	 * 
	 * @param typeVariable
	 * @return
	 */
	public boolean satisfiesBoundaries(TypeVariable<?> typeVariable,
	        Map<TypeVariable<?>, Type> typeMap) {
		boolean isAssignable = true;
		// logger.debug("Checking class: " + type + " against type variable " + typeVariable+" with map "+typeMap);
		Map<TypeVariable<?>, Type> ownerVariableMap = getTypeVariableMap();
		for(Type bound : typeVariable.getBounds()) {
//			if(bound instanceof ParameterizedType) {
//				Class<?> boundClass = GenericTypeReflector.erase(bound);
//				if(boundClass.isAssignableFrom(rawClass)) {
//					Map<TypeVariable<?>, Type> xmap = TypeUtils.determineTypeArguments(rawClass, (ParameterizedType) bound);
//					ownerVariableMap.putAll(xmap);
//				}
//			}
		}
		ownerVariableMap.putAll(typeMap);
		boolean changed = true;

		while(changed) {
			changed = false;
			for (TypeVariable<?> var : ownerVariableMap.keySet()) {

				// If the type variable points to a typevariable, let it point to what the other typevariable points to
				// A -> B
				// B -> C
				// ==> A -> C
				if(ownerVariableMap.get(var) instanceof TypeVariable<?>) {
					// Other type variable, i.e., the one this is currently pointing to
					TypeVariable<?> value = (TypeVariable<?>)ownerVariableMap.get(var);
					if(ownerVariableMap.containsKey(value)) {

						Type other = ownerVariableMap.get(value);
						if (other instanceof TypeVariable<?>) {
							// If the value (C) is also a typevariable, check we don't have a recursion here
							if (ownerVariableMap.containsKey(other)) {
								Type x = ownerVariableMap.get(other);
								if (x == var || x == value || x == other) {
									continue;
								}
							}
						}
						if (var != other && value != other) {
							ownerVariableMap.put(var, other);
							changed = true;
						}
					}
				}
			}
		}

		GenericClass concreteClass = null; //new GenericClass(GenericUtils.replaceTypeVariables(type,
		// ownerVariableMap));
		//logger.debug("Concrete class after variable replacement: " + concreteClass);

		for (Type theType : typeVariable.getBounds()) {
			//logger.debug("Current boundary of " + typeVariable + ": " + theType);
			// Special case: Enum is defined as Enum<T extends Enum>

			}
		//logger.debug("Result: is assignable " + isAssignable);
		return false;
	}

	public boolean satisfiesBoundaries(WildcardType wildcardType) {
		return satisfiesBoundaries(wildcardType, getTypeVariableMap());
	}

	/**
	 * Determine whether the upper and lower boundaries are satisfied by this
	 * class
	 * 
	 * @param wildcardType
	 * @return
	 */
	public boolean satisfiesBoundaries(WildcardType wildcardType,
	        Map<TypeVariable<?>, Type> typeMap) {
		boolean isAssignable = true;
		Map<TypeVariable<?>, Type> ownerVariableMap = getTypeVariableMap();
		ownerVariableMap.putAll(typeMap);

		// ? extends X


		return isAssignable;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (type == null) {
			LoggingUtils.getGeneratorLogger().info("Type is null for raw class " + rawClass);
			for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
				LoggingUtils.getGeneratorLogger().info(elem.toString());
			}
			assert (false);
		}
		return type.toString();
	}

	/**
	 * De-serialize. Need to use current classloader.
	 * 
	 * @param ois
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		String name = (String) ois.readObject();
		if (name == null) {
			this.rawClass = null;
			this.type = null;
			return;
		}
		this.rawClass = getClass(name);

		Boolean isParameterized = (Boolean) ois.readObject();
		if (isParameterized) {
			// GenericClass rawType = (GenericClass) ois.readObject();
			GenericClass ownerType = (GenericClass) ois.readObject();
			@SuppressWarnings("unchecked")
			List<GenericClass> parameterClasses = (List<GenericClass>) ois.readObject();
			Type[] parameterTypes = new Type[parameterClasses.size()];
			for (int i = 0; i < parameterClasses.size(); i++)
				parameterTypes[i] = parameterClasses.get(i).getType();
		} else {
			this.type = addTypeParameters(rawClass); //GenericTypeReflector.addWildcardParameters(raw_class);
		}
	}

	/**
	 * Serialize, but need to abstract classloader away
	 * 
	 * @param oos
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		if (rawClass == null) {
			oos.writeObject(null);
		} else {
			oos.writeObject(rawClass.getName());
			if (type instanceof ParameterizedType) {
				oos.writeObject(Boolean.TRUE);
				ParameterizedType pt = (ParameterizedType) type;
				// oos.writeObject(new GenericClass(pt.getRawType()));
				oos.writeObject(new GenericClass(pt.getOwnerType()));
				List<GenericClass> parameterClasses = new ArrayList<GenericClass>();
				for (Type parameterType : pt.getActualTypeArguments()) {
					parameterClasses.add(new GenericClass(parameterType));
				}
				oos.writeObject(parameterClasses);
			} else {
				oos.writeObject(Boolean.FALSE);
			}
		}
	}

}
