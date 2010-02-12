package yaquix.classifier.error;

/**
 * This exception is raised if there was a general syntax error during
 * parsing the classifier.
 * @author hk
 *
 */
public class SyntaxException extends ClassifierParseException {
	/**
	 * This constructs a new error
	 * @param line the line where the error occurred
	 * @param column the column where the error occured
	 * @param foundPrefix the prefix of the now following string
	 * @param expectedPrefixes the number of prefixes we'd expect to be here
	 */
	public SyntaxException(int line, int column, String foundPrefix, String[] expectedPrefixes) {
		super(line, column, "error: syntax-error, meh...this was just not supposed to happen, not now" +
							"\n \t --> found prefix: " + foundPrefix +
				", there were " + expectedPrefixes.length + " different prefixes that were good, none of them matched though");
	}
}
