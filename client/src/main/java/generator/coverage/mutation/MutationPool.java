package generator.coverage.mutation;

import generator.mutation.Mutation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>MutationPool class.</p>
 *
 * @author fraser
 */
public class MutationPool {

	// maps className -> method inside that class -> list of branches inside that method 
	private static Map<String, Map<String, List<Mutation>>> mutationMap = new HashMap<String, Map<String, List<Mutation>>>();

	// maps the mutationIDs assigned by this pool to their respective Mutations
	private static Map<Integer, Mutation> mutationIdMap = new HashMap<Integer, Mutation>();

	private static int numMutations = 0;


	public static Mutation addMutation(String className, String methodName,
	        String mutationName) {

		if (!mutationMap.containsKey(className))
			mutationMap.put(className, new HashMap<String, List<Mutation>>());

		if (!mutationMap.get(className).containsKey(methodName))
			mutationMap.get(className).put(methodName, new ArrayList<Mutation>());

		Mutation mutationObject = new Mutation(className, methodName, mutationName,
		        numMutations++);
		mutationMap.get(className).get(methodName).add(mutationObject);
		mutationIdMap.put(mutationObject.getId(), mutationObject);

		return mutationObject;
	}

	/**
	 * Returns a List containing all mutants in the given class and method
	 *
	 * Should no such mutant exist an empty List is returned
	 *
	 * @param className a {@link String} object.
	 * @param methodName a {@link String} object.
	 * @return a {@link List} object.
	 */
	public static List<Mutation> retrieveMutationsInMethod(String className,
	        String methodName) {
		List<Mutation> r = new ArrayList<Mutation>();
		if (mutationMap.get(className) == null)
			return r;
		List<Mutation> mutants = mutationMap.get(className).get(methodName);
		if (mutants != null)
			r.addAll(mutants);
		return r;
	}

	/**
	 * <p>getMutants</p>
	 *
	 * @return a {@link List} object.
	 */
	public static List<Mutation> getMutants() {
		return new ArrayList<Mutation>(mutationIdMap.values());
	}
	
	public static Mutation getMutant(int id) {
		return mutationIdMap.get(id);
	}

	/**
	 * Remove all known mutants
	 */
	public static void clear() {
		mutationMap.clear();
		mutationIdMap.clear();
		numMutations = 0;
	}

	/**
	 * Returns the number of currently known mutants
	 *
	 * @return The number of currently known mutants
	 */
	public static int getMutantCounter() {
		return numMutations;
	}
}
