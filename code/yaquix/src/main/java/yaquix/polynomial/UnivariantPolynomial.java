package yaquix.polynomial;

/**
 * Models a polynomial in one variable.
 * @author kraemer
 *
 */
public interface UnivariantPolynomial {

	/**
	 * computes the degree of the polynomial.
	 * @return the degree
	 */
	int degree();

	/**
	 * evaluates the polynomial in the given point.
	 * @param x the point to evaluate the polynomial in
	 * @return the value of the polynomial
	 */
	int evaluate(int x);

}