package yaquix.circuit.base;

import java.util.LinkedList;
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
	@SuppressWarnings("unchecked")
	public Shuffle(int inputCount, Map<Integer, Integer> connections) {
		int nodeCount = 2*inputCount;
		gateType = new Circuit.GateType[nodeCount];
		inputs = new int[nodeCount];
		outputs = new int[nodeCount];
		tables = new boolean[nodeCount][4][4];
		adjacencyList = new LinkedList[nodeCount];
		for (int i = 0; i < nodeCount; i++) {
			if (0 <= i && i < inputCount) {
				// input
				inputs[i] = i;
				gateType[i] = GateType.IN;
				Integer follower = connections.get(i);
				if (follower != null) {
					adjacencyList[i].add(follower);
				}
			} else if (inputCount <= i && i < 2*inputCount) {
				// output
				outputs[i-inputCount] = i;
				gateType[i] = GateType.OUT;
			}
		}
	}
}