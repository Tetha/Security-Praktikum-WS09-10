package yaquix.circuit;

public class GarbledCircuit {

	/**
	 * adds an input to the circuit and returns the id of the input gate.
	 * @return the index of the new input gate
	 */
	public int addInput() {
		// TODO addInput
		return -1;
	}
	
	/**
	 * adds a constant gate to the circuit and returns the id of this gate.
	 * @param output the value to output
	 * @return the id of the new gate
	 */
	public int addConstantGate(int output) {
		// TODO addConstantGate
		return -1;
	}
	
	/**
	 * adds an unary gate with the garbled decision table and
	 * the gate with index input as the input of the gate
	 * @param table the garbled decision table for the gate
	 * @param input the id of the gate which is the input for this gate
	 * @return the index of the added gate
	 */
	public int addUnaryGate(int[][] table, int input) {
		// TODO addUnaryGate
		return -1;
	}
	
	/**
	 * adds a binary gate to the garbled decision table and
	 * the gates with index left, right as the left and right
	 * input.
	 * @param table the garbled decision table of this gate 
	 * @param left the index of the left input
	 * @param right the index of the right input
	 * @return the index of the newly added gate.
	 */
	public int addBinaryGate(int[][] table, int left, int right) {
		// TODO addBinaryGate
		return -1;
	}
	
	
	/**
	 * designates the output of the identified gate as an output of the
	 * circuit with the given outputMapping. outputMapping[0] is the
	 * garbled value for the output 0 and outputMapping[1] is the garbled
	 * value for the output 1.
	 * @param outputMapping the mapping back into 0 and 1 from garbled values
	 * @param gate the gate which provides the output value
	 */
	public void makeOutput(int[] outputMapping, int gate) {
		// TODO: makeOutput
	}	
	
	/**
	 * evaluates the circuit. Requires that all gates are outputs
	 * or connected to another gate and that all input are set.
	 * @return the result of the evaluation.
	 */
	public boolean[] evaluate() {
		// TODO evaluate
		return null;
	}
	
	/**
	 * sets the input number index to the garbled input value.
	 * @param index the index of the input
	 * @param value the input value
	 */
	public void setInput(int index, int value) {
		// TODO setInput
	}
}
