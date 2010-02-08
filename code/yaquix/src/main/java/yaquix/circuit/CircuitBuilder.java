package yaquix.circuit;

import java.util.HashMap;
import java.util.Map;

import yaquix.circuit.base.And;
import yaquix.circuit.base.Constant;
import yaquix.circuit.base.Input;
import yaquix.circuit.base.Or;
import yaquix.circuit.base.Shuffle;
import yaquix.circuit.base.Split;
import yaquix.circuit.base.Xor;

/**
 * This class contains methods to construct the various
 * circuits used in the application.
 * 
 * This was changed from using objects for the various circuits
 * as the current means of constructing a circuit are easier to use
 * this way.
 * 
 * @author hk
 *
 */
public class CircuitBuilder {
	/**
	 * This applies the extension to separate the outputs for each user
	 * to an existing circuit. The extension adds a layer of xor-gates
	 * to the existing circuit and adds new input gates for each user
	 * in order to provide some sort of key. Thus, we achieve something
	 * resembling a onetime pad for the output part of each user and
	 * thus the output is separated into a part each user can use
	 * and decode and a part the user cannot decode and thus, not read.
	 * 
	 * The parameter assigns each output to either the sender of the
	 * circuit (alice, value true) or the receiver (bob, if the value
	 * is false)/ The key bits must be the first bits of the input
	 * of each user.
	 * @param input the circuit to extend
	 * @param owner maps the output to owners.
	 * @return the modified circuit
	 */
	public static Circuit extendWithSeparateOutputs(Circuit input, 
													boolean[] owner) {
		// TODO extendWithSeparateOutputs
		return null;
	}
	
	/**
	 * This applies the extension for turning the output into shares
	 * to a given input circuit. This extension adds a layer of
	 * the distribution net (compare the specification for details)
	 * onto each output gate according to the xor of a bit in an 
	 * additional input of each user. 
	 * The new inputs of the user are required to prefix the already
	 * existing inputs of the user.
	 * @param input the circuit to extend
	 * @return the modified circuit
	 */
	public static Circuit extendWithShareSeparation(Circuit input) {
		// TODO extendWithShareSeparation
		return null;
	}
	
	/**
	 * This constructs the dominating output circuit. Compare to the
	 * specification for details. We require the inputs for alice to be
	 * the first emails with the first bit of the label encoding to be
	 * on even positions (counting from 0) and the second bits of the
	 * label encoding to be on odd positions (counting from 0). Bobs mail
	 * encodings follow Alice mail encodings in the same fashion.
	 * @param mailCount the number of mails to handle
	 * @return A circuit which does this
	 */
	public static Circuit createDominatingOutputCircuit(int mailCount) {
		// TODO createDominatingOutputcircuit
		return null;
	}
	
	/**
	 * This creates a full adder of one bit. The first input of the
	 * full adder is the carry bit, while the second bit is the
	 * first bit to add and the third bit is the second bit to add.
	 * The first bit output is the new carry bit while the second
	 * bit is the sum of the two bits.
	 * @return a full adder
	 */
	private static Circuit createFullAdder() {
		Map<Integer, Integer> connection = new HashMap<Integer, Integer>();
		Circuit B = new Input();
		Circuit A = new Input();
		Circuit fullAdder=B.extendLeft(A);
		//output: A B
		fullAdder=fullAdder.extendLeft(new Input());
		//output C A B
		//input C A B
		
		//splitten der inputs
		connection.put(0, 0);
		fullAdder=fullAdder.extendTopLeft(new Split(2), connection);
		//output C C A B
		connection.clear();
		connection.put(2, 0);
		fullAdder.extendTopLeft(new Split(3), connection);
		//output A A A C C B
		connection.clear();
		connection.put(5, 0);
		fullAdder.extendTopLeft(new Split(3), connection);
		//outputs till now: B B B A A A C C
		
		connection.clear();
		connection.put(3, 0);
		connection.put(2, 1);
		fullAdder.extendTopLeft(new Xor(), connection);
		//output: BxA B B A A C C
		
		connection.clear();
		connection.put(1, 0);
		connection.put(3, 1);
		fullAdder.extendTopLeft(new And(), connection);
		//output: B*A BxA B A C C
		
		connection.clear();
		connection.put(2, 0);
		connection.put(3, 1);
		fullAdder.extendTopLeft(new Or(), connection);
		//output: B+A B*A BxA C C
		
		connection.clear();
		connection.put(2, 0);
		connection.put(3, 1);
		fullAdder.extendTopLeft(new Xor(), connection);
		//output: (BxA)xC B+A B*A C
		
		connection.clear();
		connection.put(1, 0);
		connection.put(3, 1);
		fullAdder.extendTopLeft(new And(), connection);
		//output (B+A)*C (BxA)xC B*A
		
		connection.clear();
		connection.put(0, 0);
		connection.put(2, 1);
		fullAdder.extendTopLeft(new Or(), connection);
		//output ((B+A)*C)+(B*A) (BxA)xC
		
		return fullAdder;
	}
	
	/**
	 * This creates a ripple carry adder for two bit numbers.
	 * The first number is required to be the first half of the input
	 * while the second number is required to be the second half of the input.
	 * We don't care for overflows, as we use bitwidths large enough, so the
	 * output is as wide as the inputs.
	 * @return a ripple carry adder
	 */
	private static Circuit createAdder(int bitwidth) {
		Map<Integer, Integer> connection= new HashMap<Integer, Integer>();
		connection.put(0, 0);
		if (bitwidth < 1) {
			throw new IllegalArgumentException();
		}
		Circuit adder=new Constant(false);
		for (int i=0; i<bitwidth; i++) {
			adder=adder.extendTopLeft(createFullAdder(), connection);
		}
		
		// shuffleMap establishes a relationship between the alternating 
		// bits of the input number bits the adder reads and the
		// continuous sequences of bits of each number the user wants
		// to input
		//connection represents the identity
		Map<Integer, Integer> shuffleMap= new HashMap<Integer, Integer>();
		shuffleMap.clear();
		connection.clear();
		for (int j=0; j<bitwidth; j++) {
			shuffleMap.put(j, 2*j);
			shuffleMap.put(bitwidth+j, 2*j+1);
			connection.put(j, j);
			connection.put(bitwidth+j, bitwidth+j);
		}
		
		Shuffle shuffle = new Shuffle(2*bitwidth, shuffleMap);
		adder=shuffle.extendTopLeft(adder, connection);
		
		return adder;
	}
	
	/**
	 * This constructs a circuit which computes the first shares
	 * of the first approximation (called alpha in the paper).
	 * 
	 * This implements the first part of the described circuit which
	 * shifts the sum of the input values by some magic values. This
	 * does not handle the separation into shares yet, as we can
	 * simply apply the extensions in the phase. 
	 */
	public static Circuit createFirstHalfOfFirstApproximation(int N, int bitwidth) {
		// TODO createFirstHalfOfFirstApproximation
		return null;
	}
	
	/**
	 * This constructs a table which uses a lookup table to compute
	 * the second shares of the first approximation output circuit
	 * (called beta in the paper). 
	 * 
	 * TODO: design lookup table (with automata, in doubt).
	 * This creation just handles the creation of the circuit, the separation
	 * into shares and the separation of the output is done in
	 * the phase by applying the extensions.
	 */
	public static Circuit createSecondHalfOfFirstApproximation(int N, int bitwidth) {
		// TODO createSecondHalfOfFirstApproximation
		return null;
	}
}
