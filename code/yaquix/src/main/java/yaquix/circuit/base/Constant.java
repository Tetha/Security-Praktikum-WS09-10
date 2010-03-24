package yaquix.circuit.base;

import java.util.LinkedList;

import yaquix.circuit.Circuit;

/**
 * This base circuit implements constant gates.
 * @author hk
 *
 */
public class Constant extends Circuit {
	/**
	 * This creates a new constant base circuit.
	 * @param isTrue if this is true, the base circuit has output 1, else 0
	 */
	@SuppressWarnings("unchecked")
	public Constant(boolean isTrue) {
		gateType = new GateType[2];
		gateType[0] = GateType.CONSTANT;
		gateType[1] = GateType.OUT;
		
		adjacencyList = new LinkedList[2];
		adjacencyList[0] = new LinkedList<Integer>();
		adjacencyList[0].add(1);
		adjacencyList[1] = new LinkedList<Integer>();
		
		tables = new boolean[2][4][4];
		tables[0][0][0] = isTrue;
		
		inputs = new int[0];
		outputs = new int[1];
		outputs[0] = 1;
	}
}
