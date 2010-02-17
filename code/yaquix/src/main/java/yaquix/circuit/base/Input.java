package yaquix.circuit.base;

import java.util.LinkedList;

import yaquix.circuit.Circuit;

/**
 * trivial base circuit implementing an input
 * 
 * @author kraemer
 * @autor amedick
 *
 */
public class Input extends Circuit {
	@SuppressWarnings("unchecked")
	public Input() {
		gateType = new GateType[2];
		gateType[0] = GateType.IN;
		gateType[1] = GateType.OUT;
		
		adjacencyList = new LinkedList[2];
		adjacencyList[0].add(1);
		
		tables = new boolean[2][4][4];
		
		inputs = new int[1];
		inputs[0] = 0;
		
		outputs = new int[1];
		outputs[0] = 1;
	}
}
