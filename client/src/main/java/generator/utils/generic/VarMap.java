package generator.utils.generic;

import generator.utils.GenericClass;
import utils.Inputs;

import java.lang.reflect.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mapping between type variables and actual parameters.
 * 
 */
public class VarMap {

	private final Map<TypeVariable<?>, Type> map = new LinkedHashMap<>();

	/**
	 * Creates an empty VarMap
	 */
	public VarMap() {
	}

	public void add(TypeVariable<?> variable, Type value) {
		map.put(variable, value);
	}

	public void addAll(TypeVariable<?>[] variables, Type[] values) throws IllegalArgumentException{
		Inputs.checkNull(variables,values);
		if(variables.length != values.length) {
			throw new IllegalArgumentException("Array length mismatch");
		}

		for (int i = 0; i < variables.length; i++) {
			add(variables[i], values[i]);
		}
	}

	public void addAll(Map<TypeVariable<?>, GenericClass> variables) throws IllegalArgumentException{
		Inputs.checkNull(variables);
		for (Entry<TypeVariable<?>, GenericClass> entry : variables.entrySet()) {
			add(entry.getKey(), entry.getValue().getType());
		}
	}


	public Type map(Type type) throws IllegalArgumentException{
		Inputs.checkNull(type);

		if (type instanceof Class) {
			return type;
		} else if (type instanceof TypeVariable) {
			// TypeVariables may also come from generic methods!
			// assert map.containsKey(type);
			if (map.containsKey(type))
				return map.get(type);
			else {
				//FIXME: (wrong) tmp workaround, as WildcardTypeImpl does crash EvoSuite
				//return Object.class;
				// TODO: Bounds should be mapped, but might be recursive so we just use unbounded for now
				return null;
			}
		} else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return null;
		} else if (type instanceof WildcardType) {
			WildcardType wType = (WildcardType) type;
			return null;
		} else if (type instanceof GenericArrayType) {
			return GenericArrayTypeImpl.createArrayType(map(((GenericArrayType) type).getGenericComponentType()));
		} else {
			throw new IllegalArgumentException("not implemented: mapping " + type.getClass()
			        + " (" + type + ")");
		}
	}

	public Type[] map(Type[] types) throws IllegalArgumentException{
		Inputs.checkNull(types);
		Type[] result = new Type[types.length];
		for (int i = 0; i < types.length; i++) {
			result[i] = map(types[i]);
		}
		return result;
	}
}
