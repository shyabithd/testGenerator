package generator.ga;

/**
 * Replacement function that only looks at the fitness and secondary objectives,
 * without checking any further constraint on the parents
 *
 * @author Gordon Fraser
 */
public class FitnessReplacementFunction extends ReplacementFunction {

	private static final long serialVersionUID = 8492857847521917540L;

	
	/**
	 * <p>Constructor for FitnessReplacementFunction.</p>
	 *
	 * @param maximize a boolean.
	 */
	public FitnessReplacementFunction(boolean maximize) {
		super(maximize);
	}
	
	/**
	 * <p>Constructor for FitnessReplacementFunction.</p>
	 */
	public FitnessReplacementFunction(){
		this(false);
	}
}
