package yaquix.phase.classifier.entropy;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;
import yaquix.polynomial.UnivariantPolynomial;

/**
 * This phase privately evaluates a given polynomial on the server
 * at a point only the client knows such that the server does not 
 * learn the point and the client does not learn the polynomial.
 * 
 * Compare to the detailed algorithm in the specification. This class
 * uses the OneOutOfMObliviousTransfer and the Polynomials.
 * @author hk
 *
 */
class PolynomialEvaluation extends Phase {
	/**
	 * If the server constructor was called and serverExecute is called,
	 * this contains the polynomial to evaluate.
	 */
	private InputKnowledge<UnivariantPolynomial> serverPolynomial;
	
	/**
	 * If the client constructor was called and clientExecute is called,
	 * this contains the point to evaluate the polynomial in.
	 */
	private InputKnowledge<Integer> clientParameter;
	
	/**
	 * If the client construct was called and the clientExecute is called,
	 * this requires the result of evaluating the polynomial in the
	 * client point to be stored.
	 */
	private OutputKnowledge<Integer> clientResult;

	
	/**
	 * This is the server side constructor of the polynomial evaluation
	 * phase. Note that if you call this constructor, you MUST call 
	 * serverExecute or undefined behavior happens.
	 * @param serverPolynomial this has to contain the polynomial to
	 * evaluate.
	 */
	public PolynomialEvaluation(
			InputKnowledge<UnivariantPolynomial> serverPolynomial) {
		this.serverPolynomial = serverPolynomial;
	}

	/**
	 * This is the clientside constructor of the polynomial evaluation
	 * phase. Note that if you call this constructor, you MUST call
	 * clientExecute or undefined behavior happens.
	 * @param clientParameter This contains the point to evaluate the
	 * polynomial in
	 * @param clientResult ther esult of the evaluation of the polynomial
	 */
	public PolynomialEvaluation(InputKnowledge<Integer> clientParameter,
			OutputKnowledge<Integer> clientResult) {
		this.clientParameter = clientParameter;
		this.clientResult = clientResult;
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
