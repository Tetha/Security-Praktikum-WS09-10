package yaquix.classifier.error;

/**
 * This error is raised if one of the limits in the input was larger
 * than 1.
 * @author hk
 *
 */
public class LimitTooLargeException extends ClassifierParseException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new error.
	 * @param line the line where the error occured
	 * @param column the column where the error occured
	 * @param limit the limit that was too large
	 */
	public LimitTooLargeException(int line, int column, double limit) {
		super(line, column, "error: limit is to large \n \t --> limit: " + limit);
	}
}
