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
		gateType = new GateType[1];
		gateType[0] = GateType.CONSTANT;
		
		adjacencyList = new LinkedList[1];
		
		tables = new boolean[1][4][4];
		tables[0][0][0] = isTrue;
		
		inputs = new int[0];
		outputs = new int[1];
		outputs[0] = 1;
	}
}
