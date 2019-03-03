package generator.instrumentations;

import java.util.*;

public class LinePool {

	/** Map class names to methods to sets of line numbers */
	private static Map<String, Map<String, Set<Integer>>> lineMap = new LinkedHashMap<String, Map<String, Set<Integer>>>();

	/**
	 * Insert line into map for class
	 *
	 * @param className a {@link String} object.
	 * @param methodName a {@link String} object.
	 * @param lineNo a int.
	 */
	public static void addLine(String className, String methodName, int lineNo) {
		if (!lineMap.containsKey(className))
			lineMap.put(className, new LinkedHashMap<String, Set<Integer>>());

		if (!lineMap.get(className).containsKey(methodName))
			lineMap.get(className).put(methodName, new LinkedHashSet<Integer>());

		lineMap.get(className).get(methodName).add(lineNo);
	}

	/**
	 * Retrieve set of lines
	 *
	 * @param className a {@link String} object.
	 * @param methodName a {@link String} object.
	 * @return a {@link Set} object.
	 */
	public static Set<Integer> getLines(String className, String methodName) {
		if (lineMap.containsKey(className))
			if (lineMap.get(className).containsKey(methodName))
				return lineMap.get(className).get(methodName);

		return new HashSet<Integer>();
	}

	/**
	 * Retrieve all lines in a class
	 *
	 * @param className a {@link String} object.
	 * @return a {@link Set} object.
	 */
	public static Set<Integer> getLines(String className) {
		Set<Integer> lines = new LinkedHashSet<Integer>();
		if (lineMap.containsKey(className))
			for (Set<Integer> methodLines : lineMap.get(className).values())
				lines.addAll(methodLines);
		return lines;
	}

	/**
	 * Retrieve all lines in the pool
	 *
	 * @return a {@link Set} object.
	 */
	public static Set<Integer> getAllLines() {
		Set<Integer> lines = new LinkedHashSet<Integer>();
		for (String className : lineMap.keySet())
			for (Set<Integer> methodLines : lineMap.get(className).values())
				lines.addAll(methodLines);
		return lines;
	}

	/**
	 * Retrieve all lines in the pool
	 *
	 * @return a {@link Set} object.
	 */
	public static int getNumLines() {
		int num = 0;
		for (String className : lineMap.keySet())
			num += lineMap.get(className).size();

		return num;
	}

	/**
	 * Returns a Set containing all classes for which this pool knows lines
	 * for as Strings
	 *
	 * @return a {@link Set} object.
	 */
	public static Set<String> getKnownClasses() {
		return new HashSet<String>(lineMap.keySet());
	}

	public static Set<String> getKnownMethodsFor(String className) {
		if(!lineMap.containsKey(className))
			return new HashSet<String>();
		else
			return lineMap.get(className).keySet();
	}

	public static void removeClass(String className) {
		lineMap.remove(className);
	}

	public static void reset() {
		lineMap.clear();
	}
}
