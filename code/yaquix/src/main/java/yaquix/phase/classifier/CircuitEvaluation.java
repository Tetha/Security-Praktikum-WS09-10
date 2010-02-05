package yaquix.phase.classifier;

import yaquix.Connection;
import yaquix.circuit.Circuit;
import yaquix.phase.InputKnowledge;
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
			OutputKnowledge<boolean[]> concertedOutput) {
		this.serverCircuit = serverCircuit;
		this.localInput = localInput;
		this.concertedOutput = concertedOutput;
	}

	
	/**
	 * This is the client side constructor for the circuit evaluation
	 * phase. Note that if you call this constructor, you must
	 * call clientExecute or undefined behavior will happen.
	 * @param localInput the local input for the circuit evaluation
	 * @param concertedOutput the overall output of the circuit evaluation.
	 */
	public CircuitEvaluation(InputKnowledge<boolean[]> localInput,
			OutputKnowledge<boolean[]> concertedOutput) {
		this.localInput = localInput;
		this.concertedOutput = concertedOutput;
	}


	@Override
	public void clientExecute(Connection connection) {
		// TODO clientExecute

	}

	@Override
	public void serverExecute(Connection connection) {
		// TODO serverExecute

	}

}
