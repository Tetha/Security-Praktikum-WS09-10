package yaquix.phase.classifier.entropy;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

/**
 * This phase evaluates the first approximation circuit in order
 * to obtain a first rough approximation of ln(x), with x being
 * the servers local term + the clients local term. This is its
 * own phase in order to encapsulate the encoding of the input
 * for the circuit and the decoding of the output.
 *
 * @author hk
 *
 */
class FirstApproximation extends Phase {
	/**
	 * This is the local term.
	 */
	private InputKnowledge<Integer> localTerm;

	/**
	 * This is the first share computed, alpha in the
	 * id3-paper.
	 */
	private OutputKnowledge<Integer> localFirstShare;

	/**
	 * This is the second share computed, beta
	 * in the id3 paper.
	 */
	private OutputKnowledge<Integer> localSecondShare;


	/**
	 * This constructs a new first approximation phase.
	 * @param localTerm the local term to sum with the remote term
	 * @param localFirstShare a first private share, alpha
	 * @param localSecondShare a second private share, beta
	 */
	public FirstApproximation(InputKnowledge<Integer> localTerm,
			OutputKnowledge<Integer> localFirstShare,
			OutputKnowledge<Integer> localSecondShare) {
		this.localTerm = localTerm;
		this.localFirstShare = localFirstShare;
		this.localSecondShare = localSecondShare;
	}

	@Override
	public void clientExecute(Connection connection) {
		// TODO clientExecute
	}

	@Override
	public void serverExecute(Connection connection) {
		// TODO serverExecute
	}
}
