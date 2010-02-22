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

		for(int i = 0; i < coefficients.length; i++){
			this.coefficients[i] = coefficients[i].clone();
		}
	}

	/**
	 * evaluates the polynomial in the given point
	 * @param x the value of the first variable
	 * @param y the value of the second variable
	 * @return the value of the polynomial in the given coordinate
	 */
	public int evaluate(int x, int y) {

		int result = 0;

		for(int i = 0; i<2; i++){
			for(int j =0; j<3; j++){
				result += coefficients[i][j] * Math.pow(x, i) * Math.pow(y, j);
			}
		}

		return result;
	}
}
