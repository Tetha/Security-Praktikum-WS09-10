package yaquix.circuit.base;

import java.util.Map;

import yaquix.circuit.Circuit;

/**
 * This shuffles inputs and outputs around according
 * to a certain wire mapping. Basically, you need 
 * to think of a bipartite graph of the same number
 * of inputs and outputs. 
 * @author hk
 *
 */
public class Shuffle extends Circuit {
	/**
	 * constructs the shuffle circuit. If connections.get(i) = j,
	 * then the input i will be mapped to the output j.
	 * @param inputCount the number of inputs and outputs
	 * @param connections connections from the input to the output
	 */
	public Shuffle(int inputCount, Map<Integer, Integer> connections) {
	}
}
