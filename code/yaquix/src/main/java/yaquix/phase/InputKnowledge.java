package yaquix.phase;

/**
 * This models that a phase requires this knowledge as input.
 * @author hk
 *
 */
public interface InputKnowledge<C> {
	/**
	 * This fetches the knowledge stored earlier.
	 * @return the stored knowledge
	 */
	C get();
}
