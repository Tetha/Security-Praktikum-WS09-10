package yaquix.circuit.base;

import java.util.LinkedList;

import yaquix.circuit.Circuit;

/**
 * This removes the input.
 * @author kraemer
 *
 */
public class Stop extends Circuit {
	@SuppressWarnings("unchecked")
	public Stop() {
		gateType = new GateType[1];
		gateType[0] = GateType.IN;
		
		adjacencyList = new LinkedList[1];
		adjacencyList[0] = new LinkedList<Integer>();
		
		tables = new boolean[1][4][4];
		
		inputs = new int[1];
		inputs[0] = 0;
		
		outputs = new int[0];
	}
}
