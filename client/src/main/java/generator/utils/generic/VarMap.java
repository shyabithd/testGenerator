package generator.utils.generic;

import generator.ClassReader;
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

	private final Map<TypeVariable<?>, ClassReader.DataType> map = new LinkedHashMap<>();

	/**
	 * Creates an empty VarMap
	 */
	public VarMap() {
	}

	public void add(TypeVariable<?> variable, ClassReader.DataType value) {
		map.put(variable, value);
	}

	public void addAll(TypeVariable<?>[] variables, ClassReader.DataType[] values) throws IllegalArgumentException{
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


	public ClassReader.DataType map(ClassReader.DataType type) throws IllegalArgumentException{
		Inputs.checkNull(type);

		return type;
	}

	public ClassReader.DataType[] map(ClassReader.DataType[] types) throws IllegalArgumentException{
		Inputs.checkNull(types);
		ClassReader.DataType[] result = new ClassReader.DataType[types.length];
		for (int i = 0; i < types.length; i++) {
			result[i] = map(types[i]);
		}
		return result;
	}
}
