package yaquix.circuit.base;

import java.util.LinkedList;

import yaquix.circuit.Circuit;

/**
 * This base circuit is the and of two inputs.
 * @author hk
 *
 */
public class And extends Circuit {
	/**
	 * Constructs a new base and circuit.
	 */
	@SuppressWarnings("unchecked")
	public And() {
		gateType = new Circuit.GateType[4];
		gateType[0] = GateType.IN;
		gateType[1] = GateType.IN;
		gateType[2] = GateType.BINARYGATE;
		gateType[3] = GateType.OUT;
		
		adjacencyList = new LinkedList[4];
		adjacencyList[0] = new LinkedList<Integer>();
		adjacencyList[1] = new LinkedList<Integer>();
		adjacencyList[2] = new LinkedList<Integer>();
		adjacencyList[3] = new LinkedList<Integer>();
		
		adjacencyList[0].add(2);
		adjacencyList[1].add(2);
		adjacencyList[2].add(3);
		
		tables = new boolean[4][4][4];
		tables[2][0][0] = false;
		tables[2][0][1] = false;
		tables[2][1][0] = false;
		tables[2][1][1] = true;
		
		inputs = new int[2];
		inputs[0] = 0;
		inputs[1] = 1;
		
		outputs = new int[1];
		outputs[0] = 3;
	}
}
