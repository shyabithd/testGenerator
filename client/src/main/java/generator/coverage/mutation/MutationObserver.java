package generator.coverage.mutation;

import generator.mutation.Mutation;

/**
 * <p>MutationObserver class.</p>
 *
 * @author Gordon Fraser
 */
public class MutationObserver {

	/** Constant <code>activeMutation=-1</code> */
	public static int activeMutation = -1;

	/**
	 * <p>mutationTouched</p>
	 *
	 * @param mutationID a int.
	 */
	public static void mutationTouched(int mutationID) {

	}

	public static void activateMutation(Mutation mutation) {
		if (mutation != null)
			activeMutation = mutation.getId();
	}

	/**
	 * <p>activateMutation</p>
	 *
	 * @param id a int.
	 */
	public static void activateMutation(int id) {
		activeMutation = id;
	}

	/**
	 * <p>deactivateMutation</p>
	 */
	public static void deactivateMutation() {
		activeMutation = -1;
	}

	public static void deactivateMutation(Mutation mutation) {
		activeMutation = -1;
	}

}
