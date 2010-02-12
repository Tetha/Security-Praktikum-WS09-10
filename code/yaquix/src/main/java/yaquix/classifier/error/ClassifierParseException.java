package yaquix.classifier.error;

/**
 * This class is raised whenever an error occurs during parsing
 * a classifier.
 * @author hk
 *
 */
public class ClassifierParseException extends IllegalArgumentException {
	private static final long serialVersionUID = 4470584941747234698L;

	/**
	 * Contains the line the error occured.
	 */
	private int line;
	/**
	 * Contains the column the error occured.
	 */
	private int column;

	/**
	 * Constructs a new Parse exception with the given input position.
	 * @param line the line where the error occurred
	 * @param column the column where the error occurred
	 * @param reason what went wrong.
	 */
	public ClassifierParseException(int line, int column, String reason) {
		super(reason);
		this.line = line;
		this.column = column;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
