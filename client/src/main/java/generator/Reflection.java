package generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import generator.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The content of arrays in reflection methods may differ between classloaders, therefore
 * we sort the output alphabetically
 * 
 * @author gordon
 *
 */
public class Reflection {

	private static <T> T[] sortArrayInPlace(T[] original) {
		List<T> methods = Arrays.asList(original);
		Collections.sort(methods, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		methods.toArray(original);
		return original;
	}

	public static Class<?>[] getClasses(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getClasses(clazz));
	}

	// TODO: Should return mocked methods?
	public static ClassReader.Method[] getMethods(ClassReader clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getMethods(clazz));
	}

	public static Field[] getFields(ClassReader clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getFields(clazz));
	}

	public static Constructor<?>[] getConstructors(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getConstructors(clazz));
	}

	public static Class<?>[] getInterfaces(Class<?> clazz) throws SecurityException {
		return null;//sortArrayInPlace(Arrays.stream(ReflectionUtils.getInterfaces(clazz)).filter(c -> !c.equals(InstrumentedClass.class)).toArray(Class[]::new));
	}

	public static Class<?>[] getDeclaredClasses(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getDeclaredClasses(clazz));
	}

	public static ClassReader.Method[] getDeclaredMethods(ClassReader clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getDeclaredMethods(clazz));
	}
	
	public static Field[] getDeclaredFields(ClassReader clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getDeclaredFields(clazz));
	}

	public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(ReflectionUtils.getDeclaredConstructors(clazz));
	}
	
	public static int getModifiers(Class<?> clazz) {
		int modifier = clazz.getModifiers();
//		if(RemoveFinalClassAdapter.finalClasses.contains(clazz.getCanonicalName())) {
//			modifier = modifier | Modifier.FINAL;
//		}
		return modifier;
	}

	public static void setField(Field field, Object sourceObject, Object value) throws IllegalAccessException {
//		if (field.getType().equals(int.class))
//			field.setInt(sourceObject, getIntValue(value));
//		else if (field.getType().equals(boolean.class))
//			field.setBoolean(sourceObject, (Boolean) value);
//		else if (field.getType().equals(byte.class))
//			field.setByte(sourceObject, (byte) getIntValue(value));
//		else if (field.getType().equals(char.class))
//			field.setChar(sourceObject, getCharValue(value));
//		else if (field.getType().equals(double.class))
//			field.setDouble(sourceObject, getDoubleValue(value));
//		else if (field.getType().equals(float.class))
//			field.setFloat(sourceObject, getFloatValue(value));
//		else if (field.getType().equals(long.class))
//			field.setLong(sourceObject, getLongValue(value));
//		else if (field.getType().equals(short.class))
//			field.setShort(sourceObject, (short) getIntValue(value));
//		else {
//			field.set(sourceObject, value);
//		}
	}

	private static int getIntValue(Object object) {
		if (object instanceof Number) {
			return ((Number) object).intValue();
		} else if (object instanceof Character) {
			return ((Character) object).charValue();
		} else
			return 0;
	}

	private static long getLongValue(Object object) {
		if (object instanceof Number) {
			return ((Number) object).longValue();
		} else if (object instanceof Character) {
			return ((Character) object).charValue();
		} else
			return 0L;
	}

	private static float getFloatValue(Object object) {
		if (object instanceof Number) {
			return ((Number) object).floatValue();
		} else if (object instanceof Character) {
			return ((Character) object).charValue();
		} else
			return 0F;
	}

	private static double getDoubleValue(Object object) {
		if (object instanceof Number) {
			return ((Number) object).doubleValue();
		} else if (object instanceof Character) {
			return ((Character) object).charValue();
		} else
			return 0.0;
	}

	private static char getCharValue(Object object) {
		if (object instanceof Character) {
			return ((Character) object).charValue();
		} else if (object instanceof Number) {
			return (char) ((Number) object).intValue();
		} else
			return '0';
	}

}
