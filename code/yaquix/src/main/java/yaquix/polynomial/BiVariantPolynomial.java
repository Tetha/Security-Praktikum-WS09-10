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
		// TODO: constructor
	}
	
	/**
	 * evaluates the polynomial in the given point
	 * @param x the value of the first variable
	 * @param y the value of the second variable
	 * @return the value of the polynomial in the given coordinate
	 */
	public int evaluate(int x, int y) {
		// TODO evaluate
		return -1;
	}
}
