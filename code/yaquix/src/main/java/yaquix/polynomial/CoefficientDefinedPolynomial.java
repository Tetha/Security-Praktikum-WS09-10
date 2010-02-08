package yaquix.polynomial;

import java.util.Map;

/**
 * This models a polynomial in one variable, defined by a
 * vector of coefficients.
 * @author hk
 *
 */
public class CoefficientDefinedPolynomial implements UnivariantPolynomial {
	/**
	 * stores the coefficients, such that the polynomial
	 * is stored as coefficients[i] * x^i.
	 */
	private int[] coefficients;
	
	/**
	 * This initializes the polynomial. The parameter array is
	 * copied so no modification side effects will happen.
	 * @param coefficients
	 */
	public CoefficientDefinedPolynomial(int[] coefficients) {
		this.coefficients = new int[coefficients.length];
		for (int i = 0; i < coefficients.length; i++) {
			this.coefficients[i] = coefficients[i];
		}
	}
	
	@Override
	public int degree() {
		return coefficients.length;
	}
	
	private int intPow(int base, int exponent) {
		int multiplicationsStillNecessary = exponent;
		int result = 1;
		while (multiplicationsStillNecessary > 0) {
			result = result*base;
		}
		return result;
	}
	
	@Override
	public int evaluate(int x) {
		int result = 0;
		
		for (int i = 0; i < coefficients.length; i++) {
			result = result + coefficients[i] * intPow(x, i);
		}
		return result;
	}
}
