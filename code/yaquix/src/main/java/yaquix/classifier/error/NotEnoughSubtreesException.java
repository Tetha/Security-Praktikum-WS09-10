package yaquix.classifier.error;

import yaquix.classifier.Classifier;

/**
 * This error is raised if a tree defines too little subtrees.
 * @author hk
 *
 */
public class NotEnoughSubtreesException extends ClassifierParseException {
	/**
	 * Constructs a new exception
	 * @param line the line where the error occured
	 * @param column the column where the error occured
	 * @param requiredNumberOfSubtrees the number of subtrees we require at least
	 * @param subTrees the subtrees we actually found
	 */
	public NotEnoughSubtreesException(int line, int column, int requiredNumberOfSubtrees, Classifier[] subTrees) {
		super(line, column, "error: there are not enough subtrees" +
							"\n \t --> min required: " + requiredNumberOfSubtrees + ", found only: " + subTrees.length +
							"\n \t --> found: " + subTrees.toString());
	}
}
