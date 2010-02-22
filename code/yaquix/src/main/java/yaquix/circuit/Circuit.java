package yaquix.circuit;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	public enum GateType {
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

	private Random random;
	
	public void setRandom(Random r) {
		this.random = r;
	}
	
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
					int followerDestination = destinationOfAddedGate[follower];
					result.adjacencyList[newGateIndex].add(followerDestination);
				}
			}
		}
		
		for(int i = 0; i < inputs.length; i++) {
			int newGateIndex = destinationOfExistingInput.get(i);
			result.gateType[newGateIndex] = GateType.IN;
			result.inputs[newInputsAdded] = newGateIndex;
			newInputsAdded++;
		
			result.adjacencyList[newGateIndex] = new LinkedList<Integer>();
			int inputGateIndex = inputs[i];
			for(Integer follower : adjacencyList[inputGateIndex]) {
				int followerDestination = destinationOfExistingGate[follower];
				result.adjacencyList[newGateIndex].add(followerDestination);
			}
		
		}
		
		// inner gates
		for(int i= 0; i < addedCircuit.gateType.length; i++) {
			if (addedCircuit.gateType[i] == GateType.IN
					|| addedCircuit.gateType[i] == GateType.OUT) continue;
			
			int gateDestinationIndex = destinationOfAddedGate[i];
			result.gateType[gateDestinationIndex] = addedCircuit.gateType[i];
			result.tables[gateDestinationIndex] = addedCircuit.tables[i];
			result.adjacencyList[gateDestinationIndex] = new LinkedList<Integer>();
			for (Integer follower : addedCircuit.adjacencyList[i]) {
				int followerDestinationIndex = destinationOfAddedGate[follower];
				result.adjacencyList[gateDestinationIndex].add(followerDestinationIndex);
			}
		}
		
		for (int i = 0; i < gateType.length; i++) {
			if(gateType[i] == GateType.IN || gateType[i] == GateType.OUT) continue;
			int gateDestinationIndex = destinationOfExistingGate[i];
			result.gateType[gateDestinationIndex] = gateType[i];
			result.tables[gateDestinationIndex] = tables[i];
			result.adjacencyList[gateDestinationIndex] = new LinkedList<Integer>();
			for (Integer follower : adjacencyList[i]) {
				if (gateType[follower] == GateType.OUT) {
					int outputIndex = -1;
					for (int j = 0; j < outputs.length; j++) {
						if (outputs[j] == follower) {
							outputIndex = j;
							break;
						}
					}
					assert (outputIndex != -1);
					if (connection.containsKey(outputIndex)) {
						int associatedAddedInput = connection.get(outputIndex);
						int assoiciatedAddedGate = addedCircuit.inputs[associatedAddedInput];
						for (Integer inputFollower : addedCircuit.adjacencyList[assoiciatedAddedGate]) {
							int inputFollowerDestination = destinationOfAddedGate[inputFollower];
							adjacencyList[gateDestinationIndex].add(inputFollowerDestination);
						}
					} else {
						int outputDestination = destinationOfExistingOutput.get(follower);
						adjacencyList[gateDestinationIndex].add(outputDestination);
					}
				} else {
					int followerDestination = destinationOfExistingGate[follower];
					adjacencyList[gateDestinationIndex].add(followerDestination);
				}
			}
		}
		
		// outputs
		int outputsAdded = 0;
		for (int i = 0; i < addedCircuit.outputs.length; i++) {
			int gateDestinationIndex = destinationOfAddedOutput.get(i);
			result.gateType[gateDestinationIndex] = GateType.OUT;
			result.outputs[outputsAdded] = gateDestinationIndex;
			result.adjacencyList[gateDestinationIndex] = new LinkedList<Integer>();
			outputsAdded++;
		}
		
		for (int i = 0; i < outputs.length; i++) {
			int gateDestinationIndex = destinationOfExistingOutput.get(i);
			if (gateDestinationIndex == -1) {
				continue;
			} else {
				result.gateType[gateDestinationIndex] = GateType.OUT;
				result.outputs[outputsAdded] = gateDestinationIndex;
				result.adjacencyList[gateDestinationIndex] = new LinkedList<Integer>();
				outputsAdded++;
			}
		}
		return result;
	}
	
	/**
	 * counts the non-input-output-gates.
	 * @return the number of the non-input-output-gates
	 */
	private int countInnerGates() {
		int gateCount = 0;
		for(GateType g : gateType) {
			if (g != GateType.IN && g != GateType.OUT) {
				gateCount++;
			}
		}
		return gateCount;
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
		return existingGateTypes.length + addedGateTypes.length - connection.size()*2;
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
		return existingOutputs.length + addedOutputs.length - connection.size();
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
		return existingInputs.length + addedInputs.length - connection.size();
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
		return extendTopLeft(addedCircuit, new HashMap<Integer, Integer>());
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
		GarbledCircuit result = new GarbledCircuit(gateType.length, inputs.length, outputs.length);
		int[][] preds; // predecessors[gate][0..1]
		int[][] wireGarblings; // wireGarblings[source][0/1]
		
		int[] outputWireMapping;
		int[] inputWireMapping;
		int onlyPredIndex;
		
		preds = computePredecessors();
		wireGarblings = garbleAllWires(inputMapping);
		for (int i = 0; i < gateType.length; i++) {
			switch(gateType[i]) {
			case IN:
				outputWireMapping = wireGarblings[i];
				garbleInput(result, i, outputWireMapping);
			break;
			
			case OUT:
				onlyPredIndex = preds[i][0];
				inputWireMapping = wireGarblings[onlyPredIndex];
				garbleOutput(result, i, inputWireMapping);
			break;
			case CONSTANT:
				outputWireMapping = wireGarblings[i];
				garbleConstantGate(result, i, outputWireMapping);
			break;
			
			case UNARYGATE:
				outputWireMapping = wireGarblings[i];
				onlyPredIndex = preds[i][0];
				inputWireMapping = wireGarblings[onlyPredIndex];
				garbleUnaryGate(result, i, outputWireMapping, inputWireMapping);
			break;
			
			case BINARYGATE:
				outputWireMapping = wireGarblings[i];
				int leftPredIndex = preds[i][0];
				int rightPredIndex = preds[i][1];
				int[] leftInputWireMapping = wireGarblings[leftPredIndex];
				int[] rightInputWireMapping = wireGarblings[rightPredIndex];
				
				garbleBinaryGate(result, i, outputWireMapping, leftInputWireMapping, rightInputWireMapping);
			break;
			}
		}
		return null;
	}


	private void garbleOutput(GarbledCircuit result, int i,
			int[] inputWireMapping) {
		for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
			if (outputs[outputIndex] == i) {
				result.addOutput(i, outputIndex, inputWireMapping);
			}
		}
		
	}

	private void garbleInput(GarbledCircuit result, int i,
			int[] outputWireMapping) {
		for (int inputIndex = 0; inputIndex < inputs.length; inputIndex++) {
			if (inputs[inputIndex] == i) {
				result.addInput(i, inputIndex, adjacencyList[i], outputWireMapping);
			}
		}
	}

	private int[][] garbleAllWires(Map<Integer, int[]> inputMapping) {
		int trueValue;
		int falseValue;
		int[][] result = new int[gateType.length][2];
		for(int i = 0; i < gateType.length; i++) {
			if (gateType[i] == GateType.IN) {
				result[i] = inputMapping.get(i);
			} else {
				trueValue = random.nextInt();
				do {
					falseValue = random.nextInt();
				} while (trueValue == falseValue);

				int[] localGarbledMapping = result[i];
				localGarbledMapping[0] = falseValue;
				localGarbledMapping[1] = trueValue;
				result[i] = localGarbledMapping;
			}
		}
		return result;
	}

	private int[][] computePredecessors() {
		int[][] result = new int[gateType.length][2];
		int[] successorCount = new int[gateType.length];
		for (int gateIndex = 0; gateIndex < gateType.length; gateIndex++) {
			for(Integer follower : adjacencyList[gateIndex]) {
				int successorsOfFollower = successorCount[follower];
				result[follower][successorsOfFollower] = gateIndex;
				successorCount[follower] += 1;
			}
		}
		return result;
	}

	private void garbleBinaryGate(GarbledCircuit result, int i, 
			int[] outputWireMapping, int[] leftInputWireMapping, 
			int[] rightInputWireMapping) {
		// the input decision table maps 0 x 1 to 0, 1 (it is 
		// in some square-shaped format). In order to use the
		// function garbleRow, we turn this into a row-like
		// format which maps 0..3 to the output value. Then, we apply
		// garbleRow 4 times, permute the table and we are done.
		// note that we have to recompute the input mapping, because
		// each input mapping individually is too short. staring at
		// the specification for quite some time reveals, that each
		// entry of the new input mapping is the xor of the values of
		// the two shorter separate input mapping.
		
		int[] largeInputMapping = new int[4];		
		largeInputMapping[0*2+0] = leftInputWireMapping[0] ^ rightInputWireMapping[0];
		largeInputMapping[0*2+1] = leftInputWireMapping[0] ^ rightInputWireMapping[1];
		largeInputMapping[1*2+0] = leftInputWireMapping[1] ^ rightInputWireMapping[0];
		largeInputMapping[1*2+1] = leftInputWireMapping[1] ^ rightInputWireMapping[1];
		
		boolean[][] squareTruthTable = tables[i];
		boolean[] longTruthTable = new boolean[4];
		longTruthTable[0*2+0] = squareTruthTable[0][0];
		longTruthTable[0*2+1] = squareTruthTable[0][1];
		longTruthTable[1*2+0] = squareTruthTable[1][0];
		longTruthTable[1*2+1] = squareTruthTable[1][1];
		
		int[][] directlyGarbled = new int[2][4];
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 0, directlyGarbled);
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 1, directlyGarbled);
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 2, directlyGarbled);
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 3, directlyGarbled);
		
		List<Integer> destinationIndexes = new LinkedList<Integer>();
		for (int ii = 0; ii < 4; ii++) destinationIndexes.add(ii);
		Collections.shuffle(destinationIndexes);
		int[][] completelyGarbled = new int[2][4];
		for (int ii = 0; ii < 4; ii++) {
			int destinationIndex = destinationIndexes.get(i);
			completelyGarbled[0][destinationIndex] = directlyGarbled[0][ii];
			completelyGarbled[1][destinationIndex] = directlyGarbled[1][ii];
		}
		result.addBinaryGate(i, adjacencyList[i], completelyGarbled);
	}

	private void garbleUnaryGate(GarbledCircuit result, int i, 
			int[] outputWireMapping, int[] inputWireMapping) {		
		boolean[] truthTable = tables[i][0];

		
		int[][] directlyGarbled = new int[2][2];		
		garbleRow(outputWireMapping, inputWireMapping, truthTable,
				0, directlyGarbled);

		garbleRow(outputWireMapping, inputWireMapping, truthTable,
				1, directlyGarbled);
		

		int[][] completelyGarbled = new int[2][2];
		if (random.nextBoolean()) {
			// straight
			completelyGarbled[0][0] = directlyGarbled[0][0];
			completelyGarbled[1][0] = directlyGarbled[1][0];
			
			completelyGarbled[0][1] = directlyGarbled[0][1];			
			completelyGarbled[1][1] = directlyGarbled[1][1];
		} else {
			// cross
			completelyGarbled[0][0] = directlyGarbled[0][1];
			completelyGarbled[1][0] = directlyGarbled[1][1];
			
			completelyGarbled[0][1] = directlyGarbled[0][0];			
			completelyGarbled[1][1] = directlyGarbled[1][0];
		}
		
		result.addUnaryGate(i, adjacencyList[i], completelyGarbled);
	}

	/**
	 * @param outputWireMapping
	 * @param inputWireMapping
	 * @param truthTable
	 * @param rowIndex
	 * @param directlyGarbled
	 */
	private void garbleRow(int[] outputWireMapping, int[] inputWireMapping,
			boolean[] truthTable, int rowIndex, int[][] directlyGarbled) {
		int garbledInputForFalse = inputWireMapping[rowIndex];
		int outputForFalse = (truthTable[rowIndex] ? outputWireMapping[1] : outputWireMapping[0]);		
		int tagEntryForFalse = 0 ^ garbledInputForFalse;
		int valueEntryForFalse = outputForFalse ^ garbledInputForFalse;		
		directlyGarbled[0][rowIndex] = tagEntryForFalse;
		directlyGarbled[1][rowIndex] = valueEntryForFalse;
	}

	private void garbleConstantGate(GarbledCircuit result, int i, 
			int[] outputWireMapping) {
		int[][] garbledTable;
		boolean outputValue = tables[i][0][0];
		
		int garbledValue;
		if (outputValue) {
			garbledValue = outputWireMapping[1];
		} else {
			garbledValue = outputWireMapping[0];
		}
		
		garbledTable = new int[2][4];
		garbledTable[0][0] = 0;
		garbledTable[1][0] = garbledValue;
		
		result.addConstantGate(i, adjacencyList[i], garbledTable);
	}
}
