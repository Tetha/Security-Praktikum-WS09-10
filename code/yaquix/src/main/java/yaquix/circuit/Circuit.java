package yaquix.circuit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private static final Logger LOG = LoggerFactory.getLogger(Circuit.class);
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

	private SecureRandom random;

	public void setRandom(SecureRandom r) {
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
		checkCircuit(addedCircuit);
		checkCircuit(this);
		LOG.trace(String.format("extendTopLeft(%s, %s)", addedCircuit, connection));
		Set<Integer> inputGates = new HashSet<Integer>();
		for (int i = 0; i < inputs.length; i++) {
			inputGates.add(inputs[i]);
		}
		assert inputGates.size() == inputs.length : "Inputs not unique";

		if (connection == null) {
			throw new NullPointerException("connection");
		}
		if (addedCircuit == null) {
			throw new NullPointerException("added circuit");
		}

		assert connection.size() <= addedCircuit.inputs.length : "Connection too large";
		assert connection.size() <= outputs.length : String.format("Connection too large (conn-size = %d > %d = # outputs)", connection.size(), outputs.length);
		Circuit result = new Circuit();
		result.inputs = new int[countSurvingInputs(connection, inputs, addedCircuit.inputs)];
		assert inputs.length <= result.inputs.length && result.inputs.length <= inputs.length + addedCircuit.inputs.length;

		int survivingOutputCount = countSurvivingOutputs(connection, outputs, addedCircuit.outputs);
		result.outputs = new int[survivingOutputCount];
		assert addedCircuit.outputs.length <= result.outputs.length && result.outputs.length <= addedCircuit.outputs.length + outputs.length;
		int survivingGatesCount = countSurvivingGates(connection, gateType, addedCircuit.gateType);
		LOG.trace(String.format("surviving gates: %d, existing gates: %d, added gates: %d, connection: %s",
					survivingGatesCount, gateType.length, addedCircuit.gateType.length, connection));

		result.adjacencyList = new LinkedList[survivingGatesCount];
		for (int i = 0; i < survivingGatesCount; i++) {
			result.adjacencyList[i] = new LinkedList<Integer>();
		}
		result.gateType = new GateType[survivingGatesCount];
		result.tables = new boolean[survivingGatesCount][4][4];

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
		int survivingAddedInputs = 0;
		List<Integer> destinationOfAddedInput = new LinkedList<Integer>();
		int[] destinationOfAddedGate = new int[addedCircuit.gateType.length];
		int[] destinationOfExistingGate = new int[gateType.length];
		for(int i = 0; i < addedCircuit.inputs.length; i++) {
			if (connection.containsValue(i)) {
				destinationOfAddedInput.add(-1);
				destinationOfAddedGate[addedCircuit.inputs[i]] = -1;
			} else {
				destinationOfAddedInput.add(survivingAddedInputs);
				assert 0 <= survivingAddedInputs && survivingAddedInputs < result.gateType.length;

				destinationOfAddedGate[addedCircuit.inputs[i]] = survivingAddedInputs;
				survivingAddedInputs++;
			}
		}

		List<Integer> destinationOfExistingInput = new LinkedList<Integer>();
		for(int i = 0; i < inputs.length; i++) {
			int destinationIndex = i+survivingAddedInputs;
			destinationOfExistingInput.add(destinationIndex);
			destinationOfExistingGate[inputs[i]] = destinationIndex;
			LOG.trace(String.format("mapping existing input %d with gateIndex %d to new gate index %d",
					i, inputs[i], destinationIndex));
		}

		int copiedAddedGates = 0;
		for(int i = 0; i < addedCircuit.gateType.length; i++) {
			if (addedCircuit.gateType[i] == GateType.IN
					|| addedCircuit.gateType[i] == GateType.OUT) continue;

			int destinationIndex = copiedAddedGates+existingInputs+survivingAddedInputs;
			LOG.trace(String.format("result.gateType.lenght = %d, copiedAddedGates=%d, existingInputs=%d,survivingAddedInputs=%d",
						result.gateType.length, copiedAddedGates, existingInputs, survivingAddedInputs));
			assert 0 <= destinationIndex && destinationIndex < result.gateType.length : destinationIndex;
			destinationOfAddedGate[i] = destinationIndex;
			LOG.trace(String.format("mapping added gate with gateIndex %d to new gate index %d",
					i,  destinationIndex));
			copiedAddedGates++;
		}

		int copiedExistingGates = 0;
		for(int i = 0; i < gateType.length; i++) {
			if (gateType[i] == GateType.IN
					|| gateType[i] == GateType.OUT) continue;
			int destinationIndex = survivingAddedInputs+existingInputs+addedGates+copiedExistingGates;
			assert 0 <= destinationIndex && destinationIndex < result.gateType.length;
			destinationOfExistingGate[i] = destinationIndex;
			LOG.trace(String.format("mapping existing gate with gateIndex %d to new gate index %d",
					i,  destinationIndex));
			copiedExistingGates++;
		}

		List<Integer> destinationOfAddedOutput =  new LinkedList<Integer>();
		for(int i = 0 ; i < addedCircuit.outputs.length; i++) {
			int destinationIndex = survivingAddedInputs + existingInputs + addedGates + existingGates + i;
			destinationOfAddedOutput.add(destinationIndex);
			LOG.trace(String.format("i=%d,output=%d", i, addedCircuit.outputs[i]));
			LOG.trace(String.format("destAddGate.length=%d", destinationOfAddedGate.length));
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
				assert 0 <= destinationIndex && destinationIndex < result.gateType.length :
							String.format("%d %d", destinationIndex, result.gateType.length);
				destinationOfExistingOutput.add(destinationIndex);
				destinationOfExistingGate[outputs[i]] = destinationIndex;
				survivingAddedOutputs++;

			}
		}

		// Index mapping is correct.

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

				int inputGateIndex = addedCircuit.inputs[i];
				for(Integer follower : addedCircuit.adjacencyList[inputGateIndex]) {
					int followerDestination = destinationOfAddedGate[follower];
					assert 0 <= followerDestination && followerDestination < result.gateType.length;
					addNewFollower(result, newGateIndex, followerDestination);
				}
			}
		}


		for(int i = 0; i < inputs.length; i++) {
			int newGateIndex = destinationOfExistingInput.get(i);
			result.gateType[newGateIndex] = GateType.IN;
			result.inputs[newInputsAdded] = newGateIndex;
			newInputsAdded++;

			int inputGateIndex = inputs[i];
			for(Integer follower : adjacencyList[inputGateIndex]) {
				if (gateType[follower] == GateType.OUT) {
					skipConnectedOutput(addedCircuit, connection, result,
							destinationOfAddedGate, destinationOfExistingOutput,
							newGateIndex, follower);
				} else {
					int followerDestination = destinationOfExistingGate[follower];
					assert newGateIndex != followerDestination : "cycle";
					assert 0 <= followerDestination && followerDestination < result.gateType.length;
					addNewFollower(result, newGateIndex, followerDestination);
				}
			}

		}


		// inner gates
		for(int i= 0; i < addedCircuit.gateType.length; i++) {
			if (addedCircuit.gateType[i] == GateType.IN
					|| addedCircuit.gateType[i] == GateType.OUT) continue;

			int gateDestinationIndex = destinationOfAddedGate[i];
			result.gateType[gateDestinationIndex] = addedCircuit.gateType[i];
			result.tables[gateDestinationIndex] = addedCircuit.tables[i];
			for (Integer follower : addedCircuit.adjacencyList[i]) {
				int followerDestinationIndex = destinationOfAddedGate[follower];
				assert 0 <= gateDestinationIndex && gateDestinationIndex < result.gateType.length;
				addNewFollower(result, gateDestinationIndex,
						followerDestinationIndex);
			}
		}

		for (int i = 0; i < gateType.length; i++) {
			if(gateType[i] == GateType.IN || gateType[i] == GateType.OUT) continue;
			int gateDestinationIndex = destinationOfExistingGate[i];
			LOG.trace(String.format("gateDestination=%d, i=%d, gatetype-length=%d",
									gateDestinationIndex, i,result.gateType.length));
			result.gateType[gateDestinationIndex] = gateType[i];
			result.tables[gateDestinationIndex] = tables[i];
			for (Integer follower : adjacencyList[i]) {
				if (gateType[follower] == GateType.OUT) {
					skipConnectedOutput(addedCircuit, connection, result,
							destinationOfAddedGate,
							destinationOfExistingOutput, gateDestinationIndex,
							follower);
				} else {
					int followerDestination = destinationOfExistingGate[follower];
					assert 0 <= followerDestination && followerDestination < result.gateType.length;
					addNewFollower(result, gateDestinationIndex,
							followerDestination);
				}
			}
		}

		checkCircuit(result);

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

		checkCircuit(result);
		return result;
	}

	/**
	 * @param result
	 * @param gateDestinationIndex
	 * @param followerDestination
	 */
	protected void addNewFollower(Circuit result, int gateDestinationIndex,
			int followerDestination) {
		assert 0 <= followerDestination && followerDestination < result.gateType.length;
		result.adjacencyList[gateDestinationIndex].add(followerDestination);
	}

	/**
	 * @param checkedCircuit
	 */
	private void checkCircuit(Circuit checkedCircuit) {
        assert checkedCircuit != null;
        assert checkedCircuit.adjacencyList != null;
        assert checkedCircuit.gateType != null;

		assert checkedCircuit.gateType.length == checkedCircuit.adjacencyList.length;
		assert checkedCircuit.gateType.length == checkedCircuit.tables.length;
		for (int i = 0; i < checkedCircuit.adjacencyList.length; i++) {
			for (Integer follower : checkedCircuit.adjacencyList[i]) {
				assert 0 <= follower && follower < checkedCircuit.gateType.length
					: String.format("Follower %d of %d out of bounds (min: 0, max: %d)",
								follower, i, checkedCircuit.gateType.length);
			}
			assert checkedCircuit.adjacencyList[i] != null : "Uninitialized adjacency list " + i;
			assert !checkedCircuit.adjacencyList[i].contains(i) : "cycle";
		}
	}

	private void skipConnectedOutput(Circuit addedCircuit,
			Map<Integer, Integer> connection, Circuit result,
			int[] destinationOfAddedGate,
			List<Integer> destinationOfExistingOutput,
			int gateDestinationIndex, Integer follower) {
		int outputIndex = findInArray(follower, outputs);

		if (connection.containsKey(outputIndex)) {
			int associatedAddedInput = connection.get(outputIndex);
			int assoiciatedAddedGate = addedCircuit.inputs[associatedAddedInput];
			for (Integer inputFollower : addedCircuit.adjacencyList[assoiciatedAddedGate]) {
				int inputFollowerDestination = destinationOfAddedGate[inputFollower];
				assert 0 <= inputFollowerDestination && inputFollowerDestination < result.gateType.length;
				assert inputFollowerDestination != gateDestinationIndex : "No Cycles";
				addNewFollower(result, gateDestinationIndex,
						inputFollowerDestination);
			}
		} else {
			int outputDestination = destinationOfExistingOutput.get(outputIndex);
			assert 0 <= outputDestination && outputDestination < result.gateType.length;
			assert outputDestination != gateDestinationIndex : "No cycles";
			addNewFollower(result, gateDestinationIndex, outputDestination);
		}
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
		LOG.trace(String.format("ex in: %d, added in: %d, connection.size(): %d, connectio %s",
								existingInputs.length, addedInputs.length, connection.size(), connection));
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
	 * @return
	 */
	public Circuit extendLeft(Circuit addedCircuit) {
		return extendTopLeft(addedCircuit, new HashMap<Integer, Integer>());
	}


	/**
	 * returns the number of inputs
	 * @return how many inputs the circuit has
	 */
	public int getInputCount() {
		return inputs.length;
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
	public GarbledCircuit garble(Map<Integer, int[]> inputMapping, SecureRandom random) {
		setRandom(random);
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

            default: throw new IllegalArgumentException(gateType[i].toString());
			}
		}

		return result;
	}


	private void garbleOutput(GarbledCircuit result,
                              int gateIndex,
			                  int[] inputWireMapping) {
        int falseOutput = inputWireMapping[0];
        int trueOutput = inputWireMapping[1];
		for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
			if (outputs[outputIndex] == gateIndex) {
				result.addOutput(gateIndex, outputIndex,
                                 GarbledCircuit.outputGateTable(falseOutput, trueOutput));
			}
		}

	}

	private void garbleInput(GarbledCircuit result, int i,
			int[] outputWireMapping) {
        int gateIndexForInput = findInArray(i,  inputs);
        result.addInput(i,
                        gateIndexForInput,
                        adjacencyList[i],
                        outputWireMapping);
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

	private void garbleBinaryGate(GarbledCircuit result,
                                  int gateIndex,
			                      int[] outputWireMapping,
                                  int[] leftInputWireMapping,
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

        boolean[][] gateTable = tables[gateIndex];
        int falseFalseOutput = outputWireMapping[boolToInt(gateTable[0][0])];
        int trueFalseOutput =  outputWireMapping[boolToInt(gateTable[1][0])];
        int falseTrueOutput = outputWireMapping[boolToInt(gateTable[0][1])];
        int trueTrueOutput =  outputWireMapping[boolToInt(gateTable[1][1])];

        int leftFalse = leftInputWireMapping[0];
        int leftTrue = leftInputWireMapping[1];
        int rightFalse = rightInputWireMapping[0];
        int rightTrue = rightInputWireMapping[1];
        int[][] garbledTable =
            GarbledCircuit.composeTable(
                GarbledCircuit.binaryTableLine(leftFalse, rightFalse, falseFalseOutput),
                GarbledCircuit.binaryTableLine(leftFalse, rightTrue, falseTrueOutput),
                GarbledCircuit.binaryTableLine(leftTrue, rightFalse, trueFalseOutput),
                GarbledCircuit.binaryTableLine(leftTrue, rightTrue, trueTrueOutput)
            );

        /*
		int[] largeInputMapping = new int[4];
		largeInputMapping[0*2+0] = leftInputWireMapping[0] ^ rightInputWireMapping[0];
		largeInputMapping[0*2+1] = leftInputWireMapping[0] ^ rightInputWireMapping[1];
		largeInputMapping[1*2+0] = leftInputWireMapping[1] ^ rightInputWireMapping[0];
		largeInputMapping[1*2+1] = leftInputWireMapping[1] ^ rightInputWireMapping[1];

		boolean[][] squareTruthTable = tables[gateIndex];
		boolean[] longTruthTable = new boolean[4];
		longTruthTable[0*2+0] = squareTruthTable[0][0];
		longTruthTable[0*2+1] = squareTruthTable[0][1];
		longTruthTable[1*2+0] = squareTruthTable[1][0];
		longTruthTable[1*2+1] = squareTruthTable[1][1];


        System.out.println("leftIn, rightIn, largeIn, longTruth");
        System.out.println(String.format("%s %s %s %s", leftInputWireMapping[0], rightInputWireMapping[0], largeInputMapping[0], longTruthTable[0]));
        System.out.println(String.format("%s %s %s %s", leftInputWireMapping[0], rightInputWireMapping[1], largeInputMapping[1], longTruthTable[1]));
        System.out.println(String.format("%s %s %s %s", leftInputWireMapping[1], rightInputWireMapping[0], largeInputMapping[2], longTruthTable[2]));
        System.out.println(String.format("%s %s %s %s", leftInputWireMapping[1], rightInputWireMapping[1], largeInputMapping[3], longTruthTable[3]));


        System.out.println("output wire mapping");
        System.out.println(String.format("%d %d", outputWireMapping[0], outputWireMapping[1]));
		int[][] directlyGarbled = new int[2][4];
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 0, directlyGarbled);
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 1, directlyGarbled);
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 2, directlyGarbled);
		garbleRow(outputWireMapping, largeInputMapping, longTruthTable, 3, directlyGarbled);

        System.out.println(String.format("%d %d %d %d", directlyGarbled[0][0],directlyGarbled[0][1],directlyGarbled[0][2],directlyGarbled[0][3]));
        System.out.println(String.format("%d %d %d %d", directlyGarbled[1][0],directlyGarbled[1][1],directlyGarbled[1][2],directlyGarbled[1][3]));
		List<Integer> destinationIndexes = new LinkedList<Integer>();
		for (int ii = 0; ii < 4; ii++) destinationIndexes.add(ii);
		Collections.shuffle(destinationIndexes);
		int[][] completelyGarbled = new int[2][4];
		for (int ii = 0; ii < 4; ii++) {
			int destinationIndex = destinationIndexes.get(ii);
			completelyGarbled[0][destinationIndex] = directlyGarbled[0][ii];
			completelyGarbled[1][destinationIndex] = directlyGarbled[1][ii];
		}*/
		result.addBinaryGate(gateIndex, adjacencyList[gateIndex], garbledTable);
	}

	private void garbleUnaryGate(GarbledCircuit result, int i,
			int[] outputWireMapping, int[] inputWireMapping) {
		boolean[] truthTable = tables[i][0];
        int falseOutput = outputWireMapping[boolToInt(truthTable[0])];
        int trueOutput = outputWireMapping[boolToInt(truthTable[1])];

        int falseInput = inputWireMapping[0];
        int trueInput = inputWireMapping[1];
        int[][] garbledTable =
                GarbledCircuit.composeTable(
                        GarbledCircuit.unaryGateLine(falseInput, falseOutput),
                        GarbledCircuit.unaryGateLine(trueInput, trueOutput)
                );
        /*
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
		}*/

		result.addUnaryGate(i, adjacencyList[i], garbledTable);
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

		int outputValue = outputWireMapping[boolToInt(tables[i][0][0])];
        garbledTable = GarbledCircuit.constantGateTable(outputValue);

        /*
		int garbledValue;
		if (outputValue) {
			garbledValue = outputWireMapping[1];
		} else {
			garbledValue = outputWireMapping[0];
		}

		garbledTable = new int[2][4];
		garbledTable[0][0] = 0;
		garbledTable[1][0] = garbledValue;
        */

		result.addConstantGate(i, adjacencyList[i], garbledTable);
	}

	/**
	 * Turns the circuit into a dot-graph.
	 */
	public String dumpAsDot() {
		StringBuilder result = new StringBuilder();
		result.append("digraph {");
		result = addGates(result);
		result = addEdges(result);
		result.append("}");
		return result.toString();
	}

	/**
	 * Adds all gates to the dot representation.
	 * @param result where to append the gates to.
	 */
	private StringBuilder addEdges(StringBuilder result) {
		for (int startIndex = 0; startIndex < adjacencyList.length; startIndex++) {
			result = addEdgesFrom(result, startIndex);
		}
		return result;
	}

	/**
	 * adds all edges coming from startindex to the graphviz code in result.
	 * @param startIndex the index of the gate where the nodes start
	 * @param result a place to append to
	 * @return result to document the state change
	 */
	private StringBuilder addEdgesFrom(StringBuilder result, int startIndex) {
		String startName = buildNodeName(startIndex);
		for (Integer follower : adjacencyList[startIndex]) {
			String followerName = buildNodeName(follower);
			String edgeStatement = String.format("%s -> %s;", startName, followerName);
			result.append(edgeStatement);
		}
		return result;
	}

	/**
	 * adds all edges to the graph.
	 * @param result where to append the edges to.
	 */
	private StringBuilder addGates(StringBuilder result) {
		for (int gateIndex = 0; gateIndex < gateType.length; gateIndex++) {
			result = addGate(result, gateIndex);
		}
		return result;
	}

	/**
	 * adds a single gate to the dot representation.
	 * @param result where to add the gate to.
	 * @param gateIndex the  index of the gate to add
	 */
	private StringBuilder addGate(StringBuilder result, int gateIndex) {
		switch (gateType[gateIndex]) {
		case IN:
			int inputIndex = findInArray(gateIndex, inputs);
			result = addBoundaryGate(result, inputIndex, gateIndex, "Input");
			break;

		case OUT:
			int outputIndex = findInArray(gateIndex, outputs);
			result = addBoundaryGate(result, outputIndex, gateIndex, "Output");
			break;

		case CONSTANT:
			result = addConstantGate(result, gateIndex);
			break;

		case UNARYGATE:
			result = addUnaryGate(result, gateIndex);
			break;

		case BINARYGATE:
			result = addBinaryGate(result, gateIndex);
			break;

		default:
			assert false : "gateType not exhausted";
		}
		return result;
	}

	/**
	 * returns the index of NEEDLE in HAYSTACK
	 * @param needle the value to find
	 * @param hayStack the array to search
	 * @return the index
	 */
	private int findInArray(int needle, int[] hayStack) {
		int result = 0;
		while (hayStack[result] != needle) {
			result++;
		}
		assert hayStack[result] == needle;
		return result;
	}

	/**
	 * This adds a boundary (input/output)-gate to the graph representation.
	 * @param result where to append to
	 * @param boundaryIndex the index in the boundary array
	 * @param gateIndex the index of the inner gate
	 * @param label the label to add to the boundary index
	 * @return result to document state change
	 */
	private StringBuilder addBoundaryGate(StringBuilder result,
			int boundaryIndex, int gateIndex, String label) {
		String nodeDescription = String.format("%s %d", label, boundaryIndex);
		return addNode(result, gateIndex, nodeDescription, "invhouse");
	}




	/**
	 * adds a constant gate to the graph representation
	 * @param result the stringbuilder to append to.
	 * @param gateIndex the index of the gate to add
	 */
	private StringBuilder addConstantGate(StringBuilder result, int gateIndex) {
		int gateValue = tables[gateIndex][0][0] ? 1 : 0;
		String label = String.format("%d", gateValue);
		return addNode(result, gateIndex, label, "record");
	}

	/**
	 * Adds a unary gate to the graph representation
	 * @param result the stringbuilder to append to
	 * @param gateIndex the index of the gate to add
	 */
	private StringBuilder addUnaryGate(StringBuilder result, int gateIndex) {
		int falseValue = boolToInt(tables[gateIndex][0][0]);
		int trueValue = boolToInt(tables[gateIndex][0][1]);
		String label = String.format("{I|0|1} | {I|%d|%d}",
							falseValue, trueValue);
		return addNode(result, gateIndex, label, "record");
	}



	/**
	 * Adds a binary gate to the graph representation.
	 * @param result the stringbuilder to append to
	 * @param gateIndex the index of the gate to add
	 */
	private StringBuilder addBinaryGate(StringBuilder result, int gateIndex) {
		int falseFalseValue = boolToInt(tables[gateIndex][0][0]);
		int falseTrueValue = boolToInt(tables[gateIndex][0][1]);
		int trueFalseValue = boolToInt(tables[gateIndex][1][0]);
		int trueTrueValue = boolToInt(tables[gateIndex][1][1]);
		String label = String.format("{L|0|0|1|1}|{R|0|1|0|1}|{O|%d|%d|%d|%d}",
				falseFalseValue, falseTrueValue, trueFalseValue, trueTrueValue);
		return addNode(result, gateIndex, label, "record");
	}

	/**
	 * Constructs the node name for a gate with index GATE_INDEX
	 * @param gateIndex the index of the gate
	 * @return the name in the graphviz output for the gate
	 */
	private String buildNodeName(int gateIndex) {
		return String.format("v%d", gateIndex);
	}

	/**
	 * This adds a shaped, labeled node to the graph representation.
	 * @param result the stringbuilder to append to
	 * @param gateIndex the index of the gate added
	 * @param nodeDescription the description of the node
	 * @param shape the shape of the node
	 * @return result to document the state change
	 */
	private StringBuilder addNode(StringBuilder result, int gateIndex,
			String nodeDescription, String shape) {
		String nodeName = buildNodeName(gateIndex);
		String nodeStatement = String.format("%s [shape=%s, label=\"%s\"];",
											 nodeName, shape, nodeDescription);
		result.append(nodeStatement);
		return result;
	}

	/**
	 * reuturns 0 for false and 1 for true
	 * @param encodedBool the bool to turn into an int
	 * @return 0 or 1 for false or true
	 */
	private int boolToInt(boolean encodedBool) {
		return encodedBool ? 1 : 0;
	}

	public int getOutputCount() {
		return outputs.length;
	}
}
