package yaquix.circuit;

import java.util.LinkedList;
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
	protected enum GateType {
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
	protected GateType[] gateType;
	
	/**
	 * stores the structure of the circuit.
	 * adjacencyList[i] contains the followers of gate with index i.
	 */
	protected List<Integer>[] adjacencyList;
	
	/**
	 * stores the annotations of the gates.
	 * if a gate with index i is constant, then tables[i][0][0] contains
	 * the value of the gate. 
	 * if a gate with index i is unary, then tables[i][0][[0] contains 
	 * the output of the gate for input 0 and tables[i][0][1] contains
	 * the output of the gate for input 1.
	 * if a gate with index i is binary, then tables[i] contains the 
	 * outputs (with [left][right]).
	 */
	protected boolean[][][] tables;
	
	/**
	 * stores the gate indices of the inputs.
	 * inputs[i] = j means: gate #j has type IN and the input i
	 * goes into gate j
	 */
	protected int[] inputs;
	
	/**
	 * stores the gate indices of the outputs;
	 * outputs[i] = j means: the value of output i comes from
	 * the output of gate j
	 */
	protected int[] outputs; 

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
	@SuppressWarnings("unchecked")
	public Circuit extendTopLeft(Circuit addedCircuit, 
								 Map<Integer, Integer> connection) {
		Circuit result = new Circuit();
		result.inputs = new int[countSurvingInputs(connection, inputs, addedCircuit.inputs)];
		result.outputs = new int[countSurvivingOutputs(connection, outputs, addedCircuit.outputs)];
		int survivingGatesCount = countSurvivingGates(connection, gateType, addedCircuit.gateType);
		result.adjacencyList = new LinkedList[survivingGatesCount];
		result.gateType = new GateType[survivingGatesCount];
		result.tables = new boolean[survivingGatesCount][4][4];

		int survivingAddedInputs = 0;
		int existingInputs = inputs.length;
		int addedGates = addedCircuit.countInnerGates();
		int existingGates = countInnerGates();
		int addedOutputs = addedCircuit.outputs.length;
		int survivingAddedOutputs = 0;
		
		// the destination of X input lists associate the input index with 
		// gate indexes in the result circuit
		// the destination of X gate associates gate indexes in the existing
		// or added circuit with gate indexes in the result circuit
		// the destination of output associates output indexes with 
		// gate indexes in the result circuit.
		List<Integer> destinationOfAddedInput = new LinkedList<Integer>();
		int[] destinationOfAddedGate = new int[addedCircuit.gateType.length];
		int[] destinationOfExistingGate = new int[gateType.length];
		for(int i = 0; i < addedCircuit.inputs.length; i++) {
			if (connection.containsValue(i)) {
				destinationOfAddedInput.add(-1);
				destinationOfAddedGate[addedCircuit.inputs[i]] = -1;
			} else {
				destinationOfAddedInput.add(survivingAddedInputs);
				destinationOfAddedGate[addedCircuit.inputs[i]] = survivingAddedInputs;
				survivingAddedInputs++;
			}
		}
		
		List<Integer> destinationOfExistingInput = new LinkedList<Integer>(); 
		for(int i = 0; i < inputs.length; i++) {
			int destinationIndex = i+survivingAddedInputs;
			destinationOfExistingInput.add(destinationIndex);
			destinationOfExistingGate[inputs[i]] = destinationIndex;
		}
		
		for(int i = 0; i < addedCircuit.gateType.length; i++) {
			if (addedCircuit.gateType[i] == GateType.IN
					|| addedCircuit.gateType[i] == GateType.OUT) continue;
			
			destinationOfAddedGate[i] = i+existingInputs+survivingAddedInputs;
		}
		
		
		for(int i = 0; i < gateType.length; i++) {
			if (gateType[i] == GateType.IN
					|| gateType[i] == GateType.OUT) continue;
			destinationOfExistingGate[i] = i+existingInputs+survivingAddedInputs+addedGates;
		}
		
		List<Integer> destinationOfAddedOutput =  new LinkedList<Integer>();
		for(int i = 0 ; i < addedCircuit.outputs.length; i++) {
			int destinationIndex = survivingAddedInputs + existingInputs + addedGates + existingGates + i;
			destinationOfAddedOutput.add(destinationIndex);
			destinationOfAddedGate[addedCircuit.outputs[i]] = destinationIndex; 
		}
		
		List<Integer> destinationOfExistingOutput = new LinkedList<Integer>();
		for(int i = 0; i < outputs.length; i++) {
			if(connection.containsKey(i)) {
				destinationOfExistingOutput.add(-1);
				destinationOfExistingGate[outputs[i]] = -1;
			} else {
				
				int destinationIndex = survivingAddedInputs + existingInputs 
												+ addedGates + existingGates 
												+ addedOutputs + survivingAddedOutputs;
				destinationOfExistingOutput.add(destinationIndex);
				destinationOfExistingGate[outputs[i]] = destinationIndex;
				survivingAddedOutputs++;
				
			}
		}
		
		// copy the gates in a meaningful fashion.
		int newInputsAdded = 0;
		
		for(int i = 0; i < addedCircuit.inputs.length; i++) {
			int newGateIndex = destinationOfAddedInput.get(i);
			if (newGateIndex == -1) {
				// the successor of the delete output corresponding to
				// this index will do all the work, we just die
			} else {
				result.gateType[newGateIndex] = GateType.IN;
				result.inputs[newInputsAdded] = newGateIndex;
				newInputsAdded++;
				
				result.adjacencyList[newGateIndex] = new LinkedList<Integer>();
				int inputGateIndex = addedCircuit.inputs[i];
				for(Integer follower : addedCircuit.adjacencyList[inputGateIndex]) {
					GateType followerType = addedCircuit.gateType[follower];
				}
			}
		}
		
		for(int i = 0; i < inputs.length; i++) {
			int newGateIndex = destinationOfExistingInput.get(i);
			result.gateType[newGateIndex] = GateType.IN;
			result.inputs[newInputsAdded] = newGateIndex;
			newInputsAdded++;
		}
		
		
		return result;
	}
	
	/**
	 * counts the non-input-output-gates.
	 * @return the number of the non-input-output-gates
	 */
	private int countInnerGates() {
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * Counts the number of gates the resulting circuit will have.
	 * @param connection the connection relation
	 * @param existingGateTypes the gate types of the existing circuit
	 * @param addedGateTypes the gate types of the added circuit.
	 * @return
	 */
	private int countSurvivingGates(Map<Integer, Integer> connection,
			GateType[] existingGateTypes, GateType[] addedGateTypes) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * This counts the number of outputs surviving the connection process. These
	 * are all outputs from the added circuit and those outputs from the existing
	 * circuit which area not mentioned in the connection
	 * @param connection the connection relation
	 * @param existingOutputs the existing outputs
	 * @param addedOutputs the added outputs
	 * @return the number of outputs surviving
	 */
	private int countSurvivingOutputs(Map<Integer, Integer> connection,
			int[] existingOutputs, int[] addedOutputs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * counts how many inputs will be in the resulting circuit. Those are
	 * all inputs from either of the two inputs - those inputs from the
	 * added inputs which are mentioned in the connection
	 * @param connection the connection mapping
	 * @param existingInputs the inputs of the existing circuit
	 * @param addedInputs the inputs of the extension curcuit
	 * @return the number of still external inputs
	 */
	private int countSurvingInputs(Map<Integer, Integer> connection,
			int[] existingInputs, int[] addedInputs) {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO extendTopRight -- check if we ever ever use this
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
