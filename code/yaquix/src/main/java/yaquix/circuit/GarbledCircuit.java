package yaquix.circuit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yaquix.circuit.Circuit.GateType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
     * NEW
     * stores the annotation of the gates
     * tables[g] contains the entries for gate g.
     * tables[g][0...3] are 0 to three lines in each table.
     * input gates have 0 lines, so tables[g] is empty.
     * constant gates have 1 line, so tables[g][0] is defined.
     * unary gates have 2 lines, so tables[g][0..1] are defined
     * binary gates have 4 lines, so tables[g][0..3] are defined
     *
     * Inside a single line:
     * line[0] contains the required left input
     * line[1] contains the required right input
     * line[2] contains the actual output
     *
     * for input gates, all of these are undefined.
     * for constant gates, line[2] is well-defined.
     * for unary gates, line[0] and line[2] are well-defined.
     * for binary gates, line[0..2] are well-defined.
     * for output gates, table[g][0][0] contains the value outputting false and
     * table[g][0][1] contains the value outputting true
     * OLD
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

    private int[][] predecessors;

	private static final Logger LOG = LoggerFactory
			.getLogger(GarbledCircuit.class);

	@SuppressWarnings("unchecked")
	public GarbledCircuit(int gateCount, int inputCount, int outputCount) {
		gateType = new GateType[gateCount];
		adjacencyList = new LinkedList[gateCount];
		tables = new int[gateCount][4][4];
		inputs = new int[inputCount];
		inputValues = new int[inputCount];
        Arrays.fill(inputValues, -1);
		outputs = new int[outputCount];
	}

	/**
	 * adds an input to the circuit and returns the id of the input gate.
	 * @param gateIndex where to put the gate
	 * @param inputIndex the index of the input in the input array
	 * @param outputWireMapping
	 * @return the index of the new input gate
	 */
	public void addInput(int gateIndex,
                         int inputIndex,
                         List<Integer> follower,
                         int[] outputWireMapping) {
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
	public void addConstantGate(int gateIndex, List<Integer> follower,
                                int[][] table) {
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
	public void addUnaryGate(int gateIndex,
                             List<Integer> follower,
                             int[][] table) {
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
	public void addBinaryGate(int gateIndex,
                              List<Integer> follower,
                              int[][] table) {
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
	 * @param inputWireMapping the output mapping
	 */
	public void addOutput(int gateIndex,
                          int outputIndex,
                          int[][] table) {
		gateType[gateIndex] = GateType.OUT;
		outputs[outputIndex] = gateIndex;
		tables[gateIndex] = table;
		adjacencyList[gateIndex] = new LinkedList<Integer>();
	}

	/**
	 * evaluates the circuit. Requires that all gates are outputs
	 * or connected to another gate and that all input are set.
	 * @return the result of the evaluation.
	 */
	public boolean[] evaluate() {
		int[] outputValues = new int[gateType.length];  // XXX: Too big?
		boolean[] hasOutput = new boolean[gateType.length]; // XXX: too big?
        assert allInputsSet() : "Unset input in: " + Arrays.toString(inputValues);

		//setInputOutputs(hasOutput, outputValues);

        LOG.trace(String.valueOf(gateType.length));
		while(someOutputHasNoOutput(hasOutput)) {
			for (int gateIndex = 0; gateIndex <  gateType.length; gateIndex++) {
				if (gateType[gateIndex] == GateType.IN  ||
						!hasOutput[gateIndex] && allInputsDefined(hasOutput, gateIndex)) {
					evaluateGate(gateIndex, outputValues, hasOutput);
				}
			}
		}

		return decodeOutput(outputValues);
	}

    private boolean allInputsSet() {
        boolean allInputsSetUntilNow = true;
        for (Integer i : inputValues)
            allInputsSetUntilNow = allInputsSetUntilNow && inputSet(i);
        return allInputsSetUntilNow;
    }

    private boolean inputSet(Integer i) {
        return i != -1;
    }
	private boolean[] decodeOutput(int[] outputValues) {
		boolean[] result = new boolean[outputs.length];
		for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
			int gateIndex = outputs[outputIndex];
			int outputValue = outputValues[gateIndex];
            if (outputGateFalseInput(tables[gateIndex]) == outputValue) {
                result[outputIndex] = false;
            } else {
				result[outputIndex] = true;
			}
		}
		return result;
	}

	private void evaluateGate(int gateIndex, int[] outputValues, boolean[] hasOutput) {
		//LOG.trace(String.format("Evaluate gate %d", gateIndex));
		int[] preds;
		int predOutput;
		hasOutput[gateIndex] = true;
        int[][] gateTable;
		switch (gateType[gateIndex]) {
		case CONSTANT:
            //System.err.println(String.format("Evaluating constant gate %d -> %d",
            //                                  gateIndex, constantGateProducedOutput(tables[gateIndex])));
            outputValues[gateIndex] = constantGateProducedOutput(tables[gateIndex]);
			break;

		case UNARYGATE:
			preds = computePredecessors(gateIndex);
			int predIndex = preds[0];
			predOutput = outputValues[predIndex];
            gateTable = tables[gateIndex];

            //System.err.println(String.format("evaluating unary gate %d", gateIndex));
            //System.err.println(String.format("\tInput: %d", predOutput));
            //System.err.println(String.format("\ttable: %s", Arrays.deepToString(gateTable)));

			assert unaryGateRequiredInput(gateTable[0]) == predOutput
                    || unaryGateRequiredInput(gateTable[1]) == predOutput
                    : String.format("Output %d of gate of type %s not in table %s",
                                    predOutput, gateType[predIndex],
                                    Arrays.deepToString(gateTable));

            int outputValue;
            if (unaryGateRequiredInput(gateTable[0]) == predOutput)
                outputValue = unaryGateProducedOutput(gateTable[0]);
            else
                outputValue = unaryGateProducedOutput(gateTable[1]);
            outputValues[gateIndex] = outputValue;
		break;

		case BINARYGATE:
			preds = computePredecessors(gateIndex);
			int leftPredIndex = preds[0];
			int rightPredIndex = preds[1];
			int leftInput = outputValues[leftPredIndex];
			int rightInput = outputValues[rightPredIndex];

            gateTable = tables[gateIndex];
			boolean outputWritten = false;
            //System.err.println(String.format("evaluating binary gate %d", gateIndex));
            //System.err.println(String.format("\tInputs: left -> %d | %d <- right", leftInput, rightInput));
            //System.err.println(String.format("\t%s", Arrays.deepToString(gateTable)));
			for (int i = 0; i < 4; i++) {
                if (binaryGateLineMatches(gateTable[i], leftInput, rightInput)) {
                    outputWritten = true;
                    outputValues[gateIndex] = binaryGateProducedOutput(gateTable[i]);
                }
			}
			assert outputWritten;
		break;

		case IN:
            //System.err.println(String.format("evaluating input gate %d to %d",
            //                   gateIndex, inputValues[gateIndex]));
			outputValues[gateIndex] = inputValues[gateIndex];
		break;

		case OUT:
			preds = computePredecessors(gateIndex);
			int onlyPredIndex = preds[0];
			predOutput = outputValues[onlyPredIndex];
			outputValues[gateIndex] = predOutput;
		break;

        default: throw new IllegalArgumentException(gateType[gateIndex].toString());
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

    private void computePredecessors() {
        predecessors = new int[gateType.length][2];
        int[] predecessorsFoundCount = new int[gateType.length];
        for (int predIndex = 0; predIndex < gateType.length; predIndex++) {
            for (Integer follower : adjacencyList[predIndex]) {
                int firstFreeIndex = predecessorsFoundCount[follower];
                assert 0 <= firstFreeIndex && firstFreeIndex < 2;
                predecessors[follower][firstFreeIndex] = predIndex;
                predecessorsFoundCount[follower]++;
            }
        }
    }
	private int[] computePredecessors(int gateIndex) {
        if (predecessors == null) computePredecessors();
        return predecessors[gateIndex];
	}

	private boolean someOutputHasNoOutput(boolean[] hasOutput) {
		boolean result = false;
		for (int outputIndex = 0; outputIndex < outputs.length; outputIndex++) {
			int gateIndex = outputs[outputIndex];
            if (!hasOutput[gateIndex]) {
                result = true;
            }
        }
		return result;
	}

	/**
	 * sets the input number index to the garbled input value.
	 * @param index the index of the input
	 * @param value the input value
	 */
	public void setInput(int index, int value) {
		inputValues[inputs[index]] = value;
	}

    private int constantGateProducedOutput(int[][] table) { return table[0][2]; }

    private int unaryGateRequiredInput(int[] line) { return line[0]; }
    private int unaryGateProducedOutput(int[] line) { return line[2]; }

    private int binaryGateRequiredLeftInput(int[] line) { return line[0]; }
    private int binaryGateRequiredRightInput(int[] line) { return line[1]; }
    private int binaryGateProducedOutput(int[] line) { return line[2]; }

    private int outputGateFalseInput(int[][] table) { return table[0][0]; }
    private int outputGateTrueInput(int[][] table) { return table[0][1]; }

    private boolean binaryGateLineMatches(int[] line, int leftInput, int rightInput) {
        return binaryGateRequiredLeftInput(line) == leftInput
                && binaryGateRequiredRightInput(line) == rightInput;
    }


    /* NEW
     * stores the annotation of the gates
     * tables[g] contains the entries for gate g.
     * tables[g][0...3] are 0 to three lines in each table.
     * input gates have 0 lines, so tables[g] is empty.
     * constant gates have 1 line, so tables[g][0] is defined.
     * unary gates have 2 lines, so tables[g][0..1] are defined
     * binary gates have 4 lines, so tables[g][0..3] are defined
     *
     * Inside a single line:
     * line[0] contains the required left input
     * line[1] contains the required right input
     * line[2] contains the actual output
     *
     * for input gates, all of these are undefined.
     * for constant gates, line[2] is well-defined.
     * for unary gates, line[0] and line[2] are well-defined.
     * for binary gates, line[0..2] are well-defined.
     * for output gates, table[g][0][0] contains the value outputting false and
     * table[g][0][1] contains the value outputting true
     */
    public static int[] constantGateLine(int output) {
        return new int[] { 0xDEADBEEF, 0xCAFEBABE, output };
    }

    public static int[][] constantGateTable(int output) {
        return new int[][] { constantGateLine(output) };
    }

    public static int[] unaryGateLine(int input, int output) {
        return new int[] { input, 0xCAFEBABE, output };
    }

    public static int[] binaryTableLine(int leftInput, int rightInput, int output) {
        return new int[] { leftInput, rightInput, output };
    }

    public static int[][] composeTable(int[]... lines) {
        return lines;
    }

    public static int[][] outputGateTable(int falseOutput, int trueOutput) {
        return new int[][] { {falseOutput, trueOutput } };
    }
}
