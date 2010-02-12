package yaquix.classifier.error;

/**
 * This error is raised if the limits in an attribute are unsorted.
 * @author hk
 *
 */
public class LimitsUnsortedException extends ClassifierParseException {
	private static final long serialVersionUID = 1L;

	/**
	 * This constructs a new error
	 * @param line the line where the error occurred
	 * @param column the column where the error occurred
	 * @param limit1 the first limit that caused an error
	 * @param limti2 the second limit that caused an error
	 */
	public LimitsUnsortedException(int line, int column, double limit1, double limit2) {
		super(line, column, "error: limits are not sorted, when they should be " +
							"\n \t --> 1st limit: " + limit1 + ", 2nd limit" + limit2);
	}
}
