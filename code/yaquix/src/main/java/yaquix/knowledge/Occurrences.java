package yaquix.knowledge;

/**
 * Contains the possible values for the attributes we use.
 * @author hk
 *
 */
public enum Occurrences {
	/**
	 * This describes that a word occurs less often than both
	 * limits of the classifying attribute.
	 */
	RARE,
	/**
	 * This describes that a word occurs more than one limit
	 * of the classifying attribute, but less often than the
	 * other limit of the same attribute.
	 */
	MEDIUM,
	/**
	 * This describes that a word occurs more often than
	 * both limits of the classifying attribute.
	 */
	OFTEN
}
