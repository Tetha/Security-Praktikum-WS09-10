package yaquix.circuit.base;

import java.util.LinkedList;

import yaquix.circuit.Circuit;

/**
 * This is a circuit that consists of two input gates and a binary
 * gate combining them into a single output gate.
 * @author kraemer
 *
 */
public abstract class BinaryGateBase extends Circuit {

	/**
	 * constructs a new circuit.
	 */
	@SuppressWarnings("unchecked")
	public BinaryGateBase() {
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
		tables[2][0][0] = getTableValue(0,0);
		tables[2][0][1] = getTableValue(0,1);
		tables[2][1][0] = getTableValue(1,0);
		tables[2][1][1] = getTableValue(1,1);
		
		inputs = new int[2];
		inputs[0] = 0;
		inputs[1] = 1;
		
		outputs = new int[1];
		outputs[0] = 3;
	}

	/**
	 * specifies the translation table.
	 * @param firstInput the value of the first input
	 * @param secondInput the value of the second input
	 * @return
	 */
	protected abstract boolean getTableValue(int firstInput, int secondInput);

}