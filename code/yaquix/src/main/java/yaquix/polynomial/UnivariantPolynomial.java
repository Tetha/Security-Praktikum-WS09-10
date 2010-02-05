package yaquix.polynomial;

import java.util.Map;

/**
 * Models a polynomial in one variable.
 * @author hk
 *
 */
public class UnivariantPolynomial {
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
	public UnivariantPolynomial(int[] coefficients) {
		// TODO Constructor
	}
	
	/**
	 * computes the degree of the polynomial.
	 * @return the degree
	 */
	public int degree() {
		// TODO degree
		return -1;
	}
	
	/**
	 * evaluates the polynomial in the given point.
	 * @param x the point to evaluate the polynomial in
	 * @return the value of the polynomial
	 */
	public int evaluate(int x) {
		// TODO evaluate
		return -1;
	}
	
	/**
	 * Given a mapping from x to values, this interpolates an univariant
	 * polynomial between these points. Compare, for example, Nevilles
	 * Algorithm.
	 * @return
	 */
	public static UnivariantPolynomial interpolate(Map<Integer, Integer> points) {
		// TODO interpolate
		return null;
	}
	
	/**
	 * Uses Gauss algorithm to solve a system of linear equations.
	 * @param coefficients the coefficient matrix of the linear equation system
	 * @param rightHandSide the right handside of the linear equation system
	 * @return a variable assignment that solves the linear equation system
	 */
	private static int[] solveLinearEquations(int[][] coefficients, 
											  int[] rightHandSide) {
		// TODO solve linear equation
		return null;
	}
}
