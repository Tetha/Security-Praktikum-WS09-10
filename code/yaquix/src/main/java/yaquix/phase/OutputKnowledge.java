package yaquix.phase;

/**
 * This models that a phase produces some knowledge.
 * @author hk
 *
 */
public interface OutputKnowledge<C> {
	/**
	 * This stores the knowledge for later usage
	 * @param knowledge the knowledge to store
	 */
	void put(C knowledge);
}
