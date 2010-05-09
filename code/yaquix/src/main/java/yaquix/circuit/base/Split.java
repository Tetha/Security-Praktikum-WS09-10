package yaquix.circuit.base;

import java.util.LinkedList;

import yaquix.circuit.Circuit;

/**
 * This base circuit implements the split of a single output
 * into many outputs.
 * @author hk
 *
 */
public class Split extends Circuit {
	/**
	 * Constructs a new splitting circuit
	 * @param numberOfOutputs the number of outputs to split the input into.
	 */
	@SuppressWarnings("unchecked")
	public Split(int numberOfOutputs) {
		if (numberOfOutputs < 0) {
			throw new IllegalArgumentException("number of outputs < 0");
		}
		
		int nodeCount = numberOfOutputs + 1;
		
		gateType = new GateType[nodeCount];
		gateType[0] = GateType.IN;
		
		adjacencyList = new LinkedList[nodeCount];
		adjacencyList[0] = new LinkedList<Integer>();
		
		tables = new boolean[nodeCount][4][4];
		
		inputs = new int[1];
		inputs[0] = 0;
		
		outputs = new int[numberOfOutputs];
		for (int i = 1; i < nodeCount; i++) {
			gateType[i] = GateType.OUT;
			
			adjacencyList[0].add(i);
			adjacencyList[i] = new LinkedList<Integer>();
			
			outputs[i-1] = i;
		}
	}
}