package yaquix.phase;

/**
 * This class moves data from one phase to the next phase.
 * It implements the mediator design pattern.
 * @author hk
 *
 * @param <C> The type of the knowledge to move
 */
public class Knowledge<C> implements InputKnowledge<C>, OutputKnowledge<C> {
	private C value;
	
	@Override
	public C get() {
		// TODO get
		return null;
	}

	@Override
	public void put(C knowledge) {
		// TODO put
	}

}
