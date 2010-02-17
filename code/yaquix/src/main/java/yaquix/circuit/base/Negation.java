package yaquix.circuit.base;

import java.util.LinkedList;

import yaquix.circuit.Circuit;

/**
 * This base circuits negates it single input.
 * @author hk
 *
 */
public class Negation extends Circuit {
	/**
	 * Constructs a new negation base circuit.
	 */
	@SuppressWarnings("unchecked")
	public Negation() {
		gateType = new GateType[3];
		gateType[0] = GateType.IN;
		gateType[1] = GateType.UNARYGATE;
		gateType[2] = GateType.OUT;
		
		adjacencyList = new LinkedList[3];
		adjacencyList[0].add(1);
		adjacencyList[1].add(2);
		
		tables = new boolean[3][4][4];
		tables[1][0][0] = true;
		tables[1][0][1] = false;
		
		inputs = new int[1];
		inputs[0] = 0;
		outputs = new int[1];
		outputs[0] = 2;
	}
}
