package yaquix.circuit;

import java.util.List;
import java.util.Map;

/**
 * Implements a boolean circuit, the bane of our existence.
 * @author hk
 *
 */
public class Circuit {
	/**
	 * determines the type of a gate
	 * @author hk
	 *
	 */
	private enum GateType {
		/**
		 * describes the gate as an input gate
		 */
		IN, 
		
		/**
		 * describes the gate as an output gate
		 */
		OUT,
		
		/**
		 * describes the gate as a constant value gate
		 */
		CONSTANT, 
		
		/**
		 * describes the gate as an unary gate
		 */
		UNARYGATE, 
		
		/**
		 * describes the gate as a binary gate
		 */
		BINARYGATE 
	};
	
	/**
	 * stores the type of all gates.
	 */
	GateType[] gateType;
	
	/**
	 * stores the structure of the circuit.
	 */
	List<Integer>[] adjacencyList;
	
	/**
	 * stores the annotations of the gates.
	 */
	int[][][] tables;
	
	/**
	 * stores the gate indices of the inputs.
	 */
	int[] inputs;
	
	/**
	 * stores the gate indices of the outputs;
	 */
	int[] outputs; 

	/**
	 * This extends a given circuit with another circuit.
	 * The parameter circuit is added on top of the existing circuit. That means, the
	 * outputs of the existing circuit are connected to the inputs of the 
	 * newly added circuit. These connections are controlled via the connection
	 * parameter: if connection.get(i) = j, then output number i of the
	 * existing circuit is connected to input number j of the parameter circuit.
	 * The new circuit is added to the left of the existing circuit. That means,
	 * the inputs of the parameter circuit which are not mentioned in the connection
	 * relation are added as inputs to the resulting circuit before copying
	 * all inputs of the current circuit to the resulting circuit and outputs
	 * of the current algorithm which are not mentioned by the mapping are added 
	 * after any outputs of the parameter circuit.
	 * 
	 * Note that extendBottomRight can be implemented by swapping addedCircuit and
	 * the current circuit.
	 * @param addedCircuit a circuit to add to the existing circuit.
	 * @param connection a specification how to connect the outputs of the current
	 * circuit to the inputs of the parameter circuit.
	 * @return
	 */
	public Circuit extendTopLeft(Circuit addedCircuit, 
								 Map<Integer, Integer> connection) {
		// TODO extendTopLeft
		return null;
	}

	/**
	 * This extends a given circuit with another circuit.
	 * The parameter circuit is added on top of the existing circuit. This means that
	 * some inputs of the parameter input are fed by outputs of the current algorithm.
	 * Which inputs are fed by what output exactly is determined by the connection mapping.
	 * If connection.get(i) = j, then the output number i of the current algorithm
	 * feeds input number j of the parameter circuit.
	 * The extension is on the right, that means, additional inputs of the parameter 
	 * circuit which are not mentioned by the connection mapping are added after
	 * the inputs of the existing algorithm, while any outputs of the existing algorithm
	 * which are not mentioned by the mapping are added to the overall outputs before
	 * any output of the parameter algorithms.
	 * 
	 * Note that extendBottomLeft can be implemented by swapping the current
	 * and the added circuit.
	 * @param addedCircuit a circuit to add to the existing circuit
	 * @param connection a specification to connect the outputs of the current
	 * circuit to the inputs of the parameter circuit.
	 * @return
	 */
	public Circuit extendTopRight(Circuit addedCircuit,
								  Map<Integer, Integer> connection) {
		// TODO extendTopRight
		return null;
	}
	
	/**
	 * This adds the parameter circuit to the left of the current circuit.
	 * The addition is done on the same level, that means, the two 
	 * circuits to not communicate with each other. 
	 * The addition is done to the left, that means, the inputs of the
	 * parameter circuit are added before any inputs of the current 
	 * circuit to the resulting circuits inputs and any outputs
	 * of the parameter circuit are added to the resulting circuit
	 * before any outputs of the current circuit.
	 * 
	 * Note that extendRight can be implemented by swapping the
	 * current circuit and the added circuit.
	 * @param addedCircuit
	 * @param connection
	 * @return
	 */
	public Circuit extendLeft(Circuit addedCircuit) {
		// TODO extendLeft
		return null;
	}
	/**
	 * garbles the boolean circuit into a garbled circuit, with the
	 * given input mapping.
	 * @param inputMapping For each input index, the entry[0] defines the
	 * garbled input value for an input 0, while the entry[1] defines the
	 * garbled input value for an input 1
	 * @return a garbled circuit which does the same as this circuit 
	 * but is garbled for yaos protocol
	 */
	public GarbledCircuit garble(Map<Integer, int[]> inputMapping) {
		// TODO garble
		return null;
	}
}
