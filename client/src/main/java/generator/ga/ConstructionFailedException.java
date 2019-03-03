package generator.ga;

public class ConstructionFailedException extends Exception {

	private static final long serialVersionUID = -1326799751206971428L;

	/**
	 * <p>Constructor for ConstructionFailedException.</p>
	 *
	 * @param reason a {@link String} object.
	 */
	public ConstructionFailedException(String reason) {
		super(reason);
	}

}
