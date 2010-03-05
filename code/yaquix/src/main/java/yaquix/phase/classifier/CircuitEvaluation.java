package yaquix.phase.classifier;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import yaquix.Connection;
import yaquix.circuit.Circuit;
import yaquix.circuit.GarbledCircuit;
import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

/**
 * This implements Yaos protocol for private circuit evaluation such
 * that each party only learns the output (and whatever they can
 * deduce from the output), but not the input of the other party.
 * 
 * In order to do this, the phase constructs selects an input mapping
 * for the circuit, constructs a garbled circuit using this, 
 * sends the circut to the client, encodes the own input, encodes
 * the remote input using 1-2-OT, evaluates the circuit on the clietn
 * and sends the output back to the server.
 * @author hk
 */
public class CircuitEvaluation extends Phase {
	/**
	 * If the server constructor was used and serverExecute
	 * is called, this contains the circuit to evaluate.
	 */
	private InputKnowledge<Circuit> serverCircuit;
	
	/**
	 * this contains the local input for the circuit.
	 */
	private InputKnowledge<boolean[]> localInput;
	
	/**
	 * This contains the overall output of the circuit.
	 */
	private OutputKnowledge<boolean[]> concertedOutput;
	
	
	/**
	 * contains the source of random bits.
	 */
	private SecureRandom random;
	
	/**
	 * This is the server side constructor for the circuit evaluation
	 * phase. Note that if you call this constructor, you must call
	 * serverExecute, or undefined behaviour will happen (well, in
	 * this case, nonsensical behaviour will happen, but the
	 * point stands. Don't use this constructor and clientExecute).
	 * @param serverCircuit the circuit to evaluate
	 * @param localInput the local input for the circuit
	 * @param concertedOutput the overall output of the circuit evaluation
	 */
	public CircuitEvaluation(InputKnowledge<Circuit> serverCircuit,
			InputKnowledge<boolean[]> localInput,
			OutputKnowledge<boolean[]> concertedOutput,
			SecureRandom randomSource) {
		this.serverCircuit = serverCircuit;
		this.localInput = localInput;
		this.concertedOutput = concertedOutput;
		random = randomSource;
	}

	
	/**
	 * This is the client side constructor for the circuit evaluation
	 * phase. Note that if you call this constructor, you must
	 * call clientExecute or undefined behavior will happen.
	 * @param localInput the local input for the circuit evaluation
	 * @param concertedOutput the overall output of the circuit evaluation.
	 */
	public CircuitEvaluation(InputKnowledge<boolean[]> localInput,
			OutputKnowledge<boolean[]> concertedOutput,
			SecureRandom randomSource) {
		this.localInput = localInput;
		this.concertedOutput = concertedOutput;
		this.random = randomSource;
	}


	@Override
	public void serverExecute(Connection connection) throws IOException, ClassNotFoundException {
		Circuit inputCircuit = serverCircuit.get();
		Map<Integer, int[]> inputMapping = buildInputMapping(inputCircuit.getInputCount());
		
		GarbledCircuit transmittedCircuit = inputCircuit.garble(inputMapping);
		connection.sendGarbledCircuit(transmittedCircuit); // (1)
		

		
		boolean[] aliceInput = localInput.get();
		int[] encodedAliceInput = encodeInput(inputMapping, aliceInput);
		connection.sendIntegers(encodedAliceInput); // (2)
		
		int bobInputLength = connection.receiveInteger(); // (3)
		
		Knowledge<int[]> aliceMessages = new Knowledge<int[]>();
		Phase ot = new OneOfTwoObliviousTransfer(aliceMessages, random);
		for (int bobInIndex = 0; bobInIndex < bobInputLength; bobInIndex++) {
			aliceMessages.put(inputMapping.get(bobInIndex));
			ot.serverExecute(connection); // (4)
		}
		
		boolean[] output = connection.receiveBitstring(); // (5)
		concertedOutput.put(output);
	}

	@Override
	public void clientExecute(Connection connection) throws IOException, ClassNotFoundException {
		GarbledCircuit transmittedCircuit = connection.receiveGarbledCircuit(); // (1)
		int[] encodedAliceInput = connection.receiveIntegers(); // (2)
		
		boolean[] bobInput = localInput.get();
		int bobInputLength = bobInput.length;
		connection.sendInteger(bobInputLength); // (3)
		
		Knowledge<Boolean> input = new Knowledge<Boolean>();
		Knowledge<Integer> receivedMessage = new  Knowledge<Integer>();
		Phase ot = new OneOfTwoObliviousTransfer(input, receivedMessage, random);
		
		int[] encodedBobInput = new int[bobInputLength];
		for (int bobInIndex = 0; bobInIndex < bobInput.length; bobInIndex++) {
			input.put(bobInput[bobInIndex]);
			ot.clientExecute(connection); // (4)
			encodedBobInput[bobInIndex] = receivedMessage.get();
		}
		
		setInputs(transmittedCircuit, encodedAliceInput, encodedBobInput);
		boolean[] output = transmittedCircuit.evaluate();
		
		connection.sendBitstring(output); // (5)
		
		concertedOutput.put(output);
	}

	/**
	 * for a single input, constructs a value garbling
	 * @return a single input garbling
	 */
	private int[] buildSingleInputMapping() {
		int trueValue;
		int falseValue;
		do {
			trueValue = random.nextInt();
			falseValue = random.nextInt();
		} while (trueValue == falseValue);
		
		int[] result = new int[2];
		result[0] = falseValue;
		result[1] = trueValue;
		return result;
	}
	
	/**
	 * Constructs a random mapping for INPUT_COUNT inputs.
	 * @param inputCount the number of inputs to garble
	 * @return an input mapping for each of these inputs
	 */
	private Map<Integer, int[]> buildInputMapping(int inputCount) {
		Map<Integer, int[]> result = new HashMap<Integer, int[]>();
		for (int inputIndex = 0; inputIndex  < inputCount; inputIndex++) {
			result.put(inputIndex, buildSingleInputMapping());
		}
		return result;
	}
	
	/**
	 * Encodes INPUT according to INPUTMAPPING so the result can be used
	 * for yaos circuit.
	 * @param inputMapping the mapping of the input bits, each value must
	 * have the garbled false value at index 0 and the garbled 1 value at
	 * index 1
	 * @param input the boolean input vector to encode
	 * @return the encoded input vector
	 */
	private int[] encodeInput(Map<Integer, int[]> inputMapping, boolean[] input) {
		int[] result = new int[input.length];
		
		for (int inputIndex = 0; inputIndex < input.length; inputIndex++) {
			int[] currentInputMapping = inputMapping.get(inputIndex);
			if (input[inputIndex]) {
				result[inputIndex] = currentInputMapping[1];
			} else {
				result[inputIndex] = currentInputMapping[0];
			}
		}
		return result;
	}
	
	/**
	 * Set inputs of TRANSMITTED_CIRCUIT such that the first
	 * inputs are set to the contents of ENCODED_ALICE_INPUT
	 * and the remaining inputs are set to the contents
	 * of ENCODED_BOB_INPUT
	 * @param transmittedCircuit the circuit to set the inputs
	 * on
	 * @param encodedAliceInput alice input to set
	 * @param encodedBobInput bobs input to set
	 */
	private void setInputs(GarbledCircuit transmittedCircuit,
			int[] encodedAliceInput, int[] encodedBobInput) {
		int aliceInputLength = encodedAliceInput.length;
		for (int aliceIndex = 0; aliceIndex < aliceInputLength; aliceIndex++) {
			transmittedCircuit.setInput(aliceIndex, encodedAliceInput[aliceIndex]);
		}
		for (int bobIndex = 0; bobIndex < encodedBobInput.length; bobIndex++) {
			int inputIndex = bobIndex + aliceInputLength;
			transmittedCircuit.setInput(inputIndex, encodedBobInput[bobIndex]);
		}		
	}
}
