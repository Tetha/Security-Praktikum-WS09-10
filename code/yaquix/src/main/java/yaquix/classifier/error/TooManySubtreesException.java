package yaquix.classifier.error;

import yaquix.classifier.Classifier;

/**
 * This exception is raised if a tree contains too many subtrees.
 * @author hk
 *
 */
public class TooManySubtreesException extends ClassifierParseException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception
	 * @param line the line the error occurred in
	 * @param column the column the error occurred in
	 * @param requiredSubTrees how many subtrees we are allowed to have at most
	 * @param subTrees the sub trees we found
	 */
	public TooManySubtreesException(int line, int column, int requiredSubTrees, Classifier[] subTrees) {
		super(line, column, "");
		// TODO: constructor
	}
}
