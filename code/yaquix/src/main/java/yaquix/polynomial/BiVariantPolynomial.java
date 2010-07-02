package yaquix.polynomial;

/**
 * Implements a polynomial in two variables.
 * @author hk
 *
 */
public class BiVariantPolynomial {
	/**
	 * stores the coefficients, such that it is
	 * coefficients[i][j] * x^i * y^j.
	 */
	int[][] coefficients;

	/**
	 * This constructs a polynomial in two variables.
	 *
	 * The coefficient in the array must be arranged such that the
	 * polynomial is represented by coefficients[i][j] * x^i * y^j.
	 *
	 * The array is copied in the constructor in order to avoid potential
	 * problems if the coefficients array is modified after passing it
	 * into a polynomial.
	 * @param coefficients
	 */
	public BiVariantPolynomial(int[][] coefficients) {
		this.coefficients = new int[coefficients.length][coefficients[0].length];
		for (int i = 0; i < coefficients.length; i++) {
			for (int ii = 0; ii < coefficients[i].length; ii++) {
				this.coefficients[i][ii] = coefficients[i][ii];
			}
		}
	}

	private int intPow(int base, int exponent) {
		int multiplicationsStillNecessary = exponent;
		int result = 1;
		while (multiplicationsStillNecessary > 0) {
			result = result*base;
            multiplicationsStillNecessary--;
		}
		return result;
	}

	/**
	 * evaluates the polynomial in the given point
	 * @param x the value of the first variable
	 * @param y the value of the second variable
	 * @return the value of the polynomial in the given coordinate
	 */
	public int evaluate(int x, int y) {
		int result = 0;
		for (int xCoeffIndex = 0; xCoeffIndex < coefficients.length; xCoeffIndex++) {
			for (int yCoeffIndex = 0; yCoeffIndex < coefficients[xCoeffIndex].length; yCoeffIndex++) {
				result += coefficients[xCoeffIndex][yCoeffIndex] * intPow(x, xCoeffIndex) * intPow(y, yCoeffIndex);
			}
		}
		return result;
	}
}
