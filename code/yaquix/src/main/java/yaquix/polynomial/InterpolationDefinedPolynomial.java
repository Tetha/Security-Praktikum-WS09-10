package yaquix.polynomial;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is a polynomial in one variable which is defined by 
 * a number of coordinates in the plane. The polynomial
 * interpolates them and offers the ability to compute
 * new values.
 * 
 * This is implemented using Nevilles algorithm.
 * @author kraemer
 *
 */
public class InterpolationDefinedPolynomial implements UnivariantPolynomial {
	/**
	 * contains the interpolated points
	 */
	private Map<Integer, Integer> points;
	
	/**
	 * enumerates the points in an arbitrary way
	 */
	private List<Integer> pointOrder;
	
	/**
	 * Constructs a new polynomial defined by interpolation
	 * @param points the points known
	 */
	public InterpolationDefinedPolynomial(Map<Integer,Integer> points) {
		this.points = points;
		pointOrder = new LinkedList<Integer>();
		for (Integer point : points.keySet()) {
			pointOrder.add(point);
		}
	}
	
	@Override
	public int degree() {
		return points.size()-1;
	}

	@Override
	public int evaluate(int x) {
		return p(0, degree(), x);
	}

	private int p(int i, int j, int x) {
		if (i == j) {
			return points.get(pointOrder.get(i));
		} else {
			int xi = points.get(pointOrder.get(i));
			int xj = points.get(pointOrder.get(j));
			return ((x - xj)*p(i,j-1,x) + (xi - x) * p(i+1,j,x))/(xi-xj);
		}
	}
}
