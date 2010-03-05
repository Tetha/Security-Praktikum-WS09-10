package yaquix.phase.classifier.entropy;

import java.security.SecureRandom;
import java.util.Random;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;
import yaquix.polynomial.CoefficientDefinedPolynomial;
import yaquix.polynomial.UnivariantPolynomial;

/**
 * This class takes one factor from each of the two users and
 * computes one random term for each user such that the sum of the
 * terms is identical to the product of the factors.
 *
 * This is implemented by selecting a simple univariant linear
 * polynomial which uses the term of the server user as the
 * factor of the variable and adds a random value to it. Then
 * we privately evaluate the polynomial and output the result
 * of the evaluation for the client user and the random value
 * we added to the polynomial (negated) as the term for the
 * server user.
 *
 * @author hk
 *
 */
class PrivateMultiplication extends Phase {
	private SecureRandom randomSource;
	private InputKnowledge<Integer> localFactor;
	private OutputKnowledge<Integer> localTerm;


	/**
	 * Constructs a new private multiplication phase
	 * @param randomSource the random number generator in progress at the moment
	 * @param localFactor the factor of the current user
	 * @param localTerm a place to store the computed term.
	 */
	public PrivateMultiplication(SecureRandom randomSource,
			InputKnowledge<Integer> localFactor,
			OutputKnowledge<Integer> localTerm) {
		this.randomSource = randomSource;
		this.localFactor = localFactor;
		this.localTerm = localTerm;
	}

	@Override
	public void clientExecute(Connection connection) {
		PolynomialEvaluation poleval = new PolynomialEvaluation(localFactor, localTerm);
		poleval.clientExecute(connection);
	}

	@Override
	public void serverExecute(Connection connection) {
		// XXX 
		InputKnowledge<CoefficientDefinedPolynomial> serverPol =
			new InputKnowledge<CoefficientDefinedPolynomial>() {
				@Override
				public CoefficientDefinedPolynomial get() {
				return (CoefficientDefinedPolynomial)
					createPolynomial(localFactor.get(),randomSource.nextInt());
				}
			};
		PolynomialEvaluation poleval = new PolynomialEvaluation(serverPol);
		poleval.serverExecute(connection);
	}

	/**
	 * constructs the linear polynomial factor * x + randomValue
	 * @param factor the local factor
	 * @param randomValue the random value to become the local share
	 * @return  the linear polynomial to evaluate
	 */
	private UnivariantPolynomial createPolynomial(int factor, int randomValue) {
		return new CoefficientDefinedPolynomial(new int[] {randomValue, factor});
	}
}
