package yaquix.circuit;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import yaquix.circuit.Circuit.GateType;

public class GarbledCircuit implements Serializable {
	private static final long serialVersionUID = -4330491556897226784L;

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
	 * tables[g][0][i] is the marking entry for i of gate g,
	 * tables[g][1][i] is the value entry for i of gate g
	 * for constant gates, only i=0 is defined,
	 * for unary gates, i=0,1 is defined,
	 * for binary gates, i=0,1,2,3 is defined
	 * for an output gate, tables[g][0][0] is the garbled value for
	 * output 0 and tables[g][0][1] is the garbled value for 
	 * output 1
	 */
	protected int[][][] tables;
	
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
		
	protected int[] inputValues;
	
	@SuppressWarnings("unchecked")
	public GarbledCircuit(int gateCount, int inputCount, int outputCount) {
		gateType = new GateType[gateCount];
		adjacencyList = new LinkedList[gateCount];
		tables = new int[gateCount][2][4];
		inputs = new int[inputCount];
		inputValues = new int[gateCount];
		outputs = new int[outputCount];
	}

	/**
	 * adds an input to the circuit and returns the id of the input gate.
	 * @param gateIndex where to put the gate
	 * @param inputIndex the index of the input in the input array
	 * @param outputWireMapping 
	 * @return the index of the new input gate
	 */
	public void addInput(int gateIndex, int inputIndex, List<Integer> follower, int[] outputWireMapping) {
		inputs[inputIndex] = gateIndex;
		gateType[gateIndex] = GateType.IN;
		adjacencyList[gateIndex] = follower;
	}
	
	/**
	 * adds a constant gate to the circuit and returns the id of this gate.
	 * @param gateIndex where to put the gate
	 * @param follower the list of followers
	 * @param table the garbled decision table
	 */
	public void addConstantGate(int gateIndex, List<Integer> follower, int[][] table) {
		gateType[gateIndex] = GateType.CONSTANT;
		adjacencyList[gateIndex] = follower;
		tables[gateIndex] = table;
	}
	
	/**
	 * adds an unary gate with the garbled decision table and
	 * the gate with index input as the input of the gate
	 * @param gateIndex where to put the gate
	 * @param follower the list of follower indexes
	 * @param table the garbled decision table
	 */
	public void addUnaryGate(int gateIndex, List<Integer> follower, int[][] table) {
		gateType[gateIndex] = GateType.UNARYGATE;
		adjacencyList[gateIndex] = follower;
		tables[gateIndex] = table;
	}
	
	/**
	 * adds a binary gate to the garbled decision table and
	 * the gates with index left, right as the left and right
	 * input.
	 * @param gateIndex where to put the gate
	 * @param follower the list of follower indexes
	 * @param table the garbled decision table
	 */
	public void addBinaryGate(int gateIndex, List<Integer> follower, int[][] table) {
		gateType[gateIndex ] = GateType.BINARYGATE;
		adjacencyList[gateIndex ] = follower;
		tables[gateIndex] = table;
	}
	
	
	/**
	 * designates the output of the identified gate as an output of the
	 * circuit with the given outputMapping. outputMapping[0] is the
	 * garbled value for the output 0 and outputMapping[1] is the garbled
	 * value for the output 1.
	 * @param gateIndex where to put the gate
	 * @param follower the list of follower indexes
	 * @param inputWireMapping the output mapping 
	 */
	public void addOutput(int gateIndex, int outputIndex, int[] inputWireMapping) {
		gateType[gateIndex] = GateType.OUT;
		outputs[outputIndex] = gateIndex;
		tables[gateIndex][0] = inputWireMapping;
	}	
	
	/**
	 * evaluates the circuit. Requires that all gates are outputs
	 * or connected to another gate and that all input are set.
	 * @return the result of the evaluation.
	 */
	public boolean[] evaluate() {
		int[] outputValues = new int[gateType.length];
		boolean[] hasOutput = new boolean[gateType.length];
		
		while(someOutputHasNoOutput(hasOutput)) {
			for (int gateIndex = 0; gateIndex <  gateType.length; gateIndex++) {
				if (!hasOutput[gateIndex] && allInputsDefined(hasOutput, gateIndex)) {
					evaluateGate(gateIndex, outputValues, hasOutput);
				}
			}
		}
		
		return decodeOutput(outputValues);
	}

	
	private boolean[] decodeOutput(int[] outputValues) {
		boolean[] result = new boolean[outputs.length];
		for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
			int gateIndex = outputs[outputIndex];
			int outputValue = outputValues[gateIndex];
			if (tables[outputIndex][0][0] == outputValue) {
				result[outputIndex] = false;
			} else {
				result[outputIndex] = true;
			}
		}
		return result;
	}

	private void evaluateGate(int gateIndex, int[] outputValues, boolean[] hasOutput) {
		int[] preds;
		int predOutput;
		hasOutput[gateIndex] = true;
		switch (gateType[gateIndex]) {
		case CONSTANT:
			outputValues[gateIndex] = tables[gateIndex][0][0];
			break;
			
		case UNARYGATE:
			preds = computePredecessors(gateIndex);
			int predIndex = preds[0];
			predOutput = outputValues[predIndex];
			if (tables[gateIndex][0][0] == predOutput) {
				outputValues[gateIndex] = tables[gateIndex][1][0] ^ predOutput;
			} else {
				outputValues[gateIndex] = tables[gateIndex][1][1] ^ predOutput;
			}
		break;
		
		case BINARYGATE:
			preds = computePredecessors(gateIndex);
			int leftPredIndex = preds[0];
			int rightPredIndex = preds[1];
			int leftInput = outputValues[leftPredIndex];
			int rightInput = outputValues[rightPredIndex];
			int inputTag = leftInput ^ rightInput;
			
			for (int i = 0; i < 4; i++) {
				if (tables[gateIndex][0][i] == inputTag) {
					outputValues[gateIndex] = tables[gateIndex][1][i] ^ inputTag;
					break;
				}
			}
		break;
		
		case IN:
			outputValues[gateIndex] = inputValues[gateIndex];
		break;
		
		case OUT:
			preds = computePredecessors(gateIndex);
			int onlyPredIndex = preds[0];
			predOutput = outputValues[onlyPredIndex];
			outputValues[gateIndex] = predOutput;
		break;
		}
	}

	private boolean allInputsDefined(boolean[] hasOutput, int gateIndex) {
		int[] predecessors = computePredecessors(gateIndex);
		for (int predIndex = 0; predIndex < predecessors.length; predIndex++) {
			int pred = predecessors[predIndex];
			if (!hasOutput[pred]) return false;
		}
		return true;
	}

	private int[] computePredecessors(int gateIndex) {
		int[] preds = new int[2];
		int predCount = 0;
		
		for (int predIndex = 0; predIndex < gateType.length; predIndex++) {
			for (Integer follower : adjacencyList[predIndex]) {
				if (follower == gateIndex) {
					preds[predCount] = predIndex;
					predCount++;
				}
			}
		}
		return preds;
	}

	private boolean someOutputHasNoOutput(boolean[] hasOutput) {
		for (int outputIndex = 0; outputIndex < hasOutput.length; outputIndex++) {
			int gateIndex = outputs[outputIndex];
			if (!hasOutput[gateIndex]) return false;
		}
		return true;
	}

	/**
	 * sets the input number index to the garbled input value.
	 * @param index the index of the input
	 * @param value the input value
	 */
	public void setInput(int index, int value) {
		inputValues[inputs[index]] = value;
	}
}
