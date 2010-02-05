package yaquix.phase.classifier.entropy;

import yaquix.Connection;
import yaquix.phase.Phase;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.polynomial.UnivariantPolynomial;

/**
 * This class improves a first approximation of ln(x) by
 * evaluating a taylor approximation privately. 
 * 
 * We construct a polynomial defined in the paper, which
 * is deduced from the taylor approximation of ln(x) and
 * evaluate this privately. The polynomial evaluation is
 * implemented in a separate phase.
 * 
 * @author hk
 *
 */
class ApproximationImprovement extends Phase {
	/**
	 * This contains the local share of the first pair of shares
	 * computed by the first approximation phase, called alpha
	 * in the paper.
	 */
	private InputKnowledge<Integer> localFirstShare;
	
	/**
	 * This requires the computed term share of x*ln(x) to be stored.
	 */
	private OutputKnowledge<Integer> localXLnXShareTerm;
	
	@Override
	public void clientExecute(Connection connection) {
		// TODO clientExecute

	}

	@Override
	public void serverExecute(Connection connection) {
		// TODO serverExecute

	}

	private UnivariantPolynomial constructPolynomial(int localFirstShare, 
													 int randomShare) {
		// TODO constructPolynomial
		return null;
	}
}
