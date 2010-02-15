package yaquix.circuit;

import java.util.HashMap;
import java.util.Map;

import yaquix.circuit.base.And;
import yaquix.circuit.base.Constant;
import yaquix.circuit.base.Input;
import yaquix.circuit.base.Negation;
import yaquix.circuit.base.Or;
import yaquix.circuit.base.Shuffle;
import yaquix.circuit.base.Split;
import yaquix.circuit.base.Stop;
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
	 * @param aliceInputSize number of inputbits of alice
	 * @param bobOnputSize numer of inputbits of bob
	 * @param owner maps the output to owners.
	 * @return the modified circuit
	 */
	public static Circuit extendWithSeparateOutputs(Circuit input, 
													boolean[] owner,
													int aliceInputSize,
													int bobInputSize) {
		Map<Integer, Integer> connection = new HashMap<Integer, Integer>();
		Map<Integer, Integer> shuffleMap = new HashMap<Integer, Integer>();

		int outputSize = owner.length;
		int aliceKeySize = countAliceBits(owner, outputSize);
		int aliceXorSeen=0;
		int bobXorSeen=0;
		for (int i=0; i<outputSize; i++) {
			connection.clear();
			connection.put(i, 0);
			input=input.extendTopLeft(new Xor(), connection);
			if (owner[i]) {
				//alice-bit
				shuffleMap.put(outputSize-i-1, aliceXorSeen);
				aliceXorSeen++;
			} else {
				//bob-bit
				shuffleMap.put(outputSize-i-1, bobXorSeen+aliceInputSize+aliceKeySize);
				bobXorSeen++;
			}
		}
		for (int k=0; k<aliceInputSize; k++) {
			shuffleMap.put(k,k+aliceXorSeen);
		}
		for (int l=0; l<bobInputSize; l++) {
			shuffleMap.put(aliceInputSize+l, aliceXorSeen+bobXorSeen+aliceInputSize+l);
		}
		int monsterLength=aliceXorSeen+aliceInputSize+bobXorSeen+bobInputSize;
		Shuffle shuffle=new Shuffle(monsterLength, shuffleMap);
		input=shuffle.extendTopLeft(input, createIdentityMapping(monsterLength));
		
		shuffleMap.clear();
		for (int i=0; i<outputSize; i++) {
			shuffleMap.put(i, outputSize-i-1);
		}
		
		shuffle=new Shuffle(outputSize, shuffleMap);
		input=shuffle.extendTopLeft(input, createIdentityMapping(outputSize));
		return input;
	}

	/**
	 * @param owner
	 * @param outputSize
	 * @param aliceInputSize
	 * @return
	 */
	private static int countAliceBits(boolean[] owner, int outputSize) {
		int aliceInputSize=0;
		for (int j=0; j<outputSize; j++){
			if (owner[j]) {
				aliceInputSize++;
			}
		}
		return aliceInputSize;
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
	 * This is the state transition network for the comparer automaton
	 * computing a <=> b.
	 * The inputs are:
	 *  - the input with index 0 is the bit of a
	 *  - the input with index 1 is the bit of b
	 *  - the input with index 2 is the first bit of the
	 *    current state (s0 in the specification)
	 *  - the input with index 3 is the second bit of the
	 *    current state (s1 in the specification)
	 *    
	 * The outputs are:
	 *  - the output with index 0 is the first bit
	 *    of the encoding of the next state (s0' in the
	 *    specification)
	 *  - the output with index 1 is the second bit
	 *    of the encoding of the next state (s1' in the
	 *    specification)
	 *    
	 * @return a circuit to compute the state transition of the
	 *         comparision automaton.
	 */
	private static Circuit createComparerStateTransition() {
		Circuit result;
		Map<Integer, Integer> connection = new HashMap<Integer, Integer>();
		// Outputs: 
		result = new Split(2);
		
		// Inputs: s1
		// Outputs: s1 s1
		
		result = result.extendLeft(new Split(2));
	
		// Inputs s0 s1
		// Outputs: s0 s0 s1 s1
		
		result = result.extendLeft(new Split(2));
		
		// Inputs: b s0 s1
		// Outputs: b b s0 s0 s1 s1
		
		result = result.extendLeft(new Split(2));
		
		// Inputs: a b s0 s1
		// Outputs: a a b b s0 s0 s1 s1
		
		connection.clear();
		connection.put(6, 0);
		result = result.extendTopLeft(new Negation(), connection);
		
		// Outputs: -s1 a a b b s0 s0 s1
		
		connection.clear();
		connection.put(5, 0);
		result = result.extendTopLeft(new Negation(), connection);
		
		// Outputs: -s0 -s1 a a b b s0 s1
		
		connection.clear();
		connection.put(2, 0);
		result = result.extendTopLeft(new Negation(), connection);
		
		// Outputs: -a -s0 -s1 a b b s0 s1
		
		connection.clear();
		connection.put(1, 0);
		connection.put(2, 1);
		result = result.extendTopLeft(new And(), connection);
		
		// Outputs: (-s0)*(-s1) -a a b b s0 s1
		
		connection.clear();
		connection.put(1, 0);
		connection.put(3, 1);
		result = result.extendTopLeft(new And(), connection);
		
		// Outputs: (-a)*b (-s0)*(-s1) a b s0 s1
		
		connection.clear();
		connection.put(0, 0);
		connection.put(1, 1);
		result = result.extendTopLeft(new And(), connection);
		
		// Outputs: (((-a)*b)*((-s0)*(-s1))) a b s0 s1
		
		connection.clear();
		connection.put(0, 0);
		connection.put(4, 1);
		result = result.extendTopLeft(new Or(), connection);
		
		// Outputs: ((((-a)*b)*((-s0)*(-s1)))+s1) a b s0
		
		connection.clear();
		connection.put(1, 0);
		connection.put(2, 1);
		result = result.extendTopLeft(new Xor(), connection);
		
		// Outputs: axb ((((-a)*b)*((-s0)*(-s1)))+s1) s0
		
		connection.clear();
		connection.put(0, 0);
		connection.put(2, 1);
		result = result.extendTopLeft(new Or(), connection);
		
		// Outputs: (axb)+s0 ((((-a)*b)*((-s0)*(-s1)))+s1)
		
		return result;
	}
	
	
	/**
	 * creates a circuit that compares two number a, bof equal bitwidth
	 * and outputs if a < b, a = b, a > b. 
	 * 
	 * The first bitWidth inputs are the bits for the first number a with
	 * bit 0 being the most significant bit and bit bitWidth-1 being the
	 * least significant bit of a.
	 * 
	 * The second bitWidth inputs are the bits for the second number b
	 * with bit bitWidth being the most significant bit of b and 
	 * with with 2*bitWidth -1 being the least significant bit of b.
	 * 
	 * The first bit of the output must be 0 iff a = b. Thus, if
	 * a != b, the first bit of the output must be 1. If the first
	 * bit of the output is 1, then the second bit of the output
	 * must be 0 iff a > b and it must be 1 iff a < b. If the first 
	 * bit of the output is 0, the second bit of the output is not
	 * defined.
	 * 
	 * @param bitWidth the bitWidth of the numbers to compare
	 * @return a circuit to compute the comparision.
	 */
	private static Circuit createComparer(int bitWidth) {
		Circuit result = new Constant(false);
		result = result.extendLeft(new Constant(false));
		int inputWidth = 2*bitWidth;
		Map<Integer, Integer> connection = new HashMap<Integer, Integer>();
		connection.put(0, 2);
		connection.put(1, 3);
		
		for (int i = 0; i < bitWidth; i++) {
			result = result.extendTopLeft(createComparerStateTransition(), connection);			
		}
		
		// this reverses the pairs of bits with odd and even index
		// that is, a1 b1 a2 b2 is reversed into a2 b2 a1 b1
		Map<Integer, Integer> pairReversalShuffle = new HashMap<Integer, Integer>();
		for(int i = 0; i < bitWidth-1; i++) {
			pairReversalShuffle.put(2*i, inputWidth-2*i-1 -1);
			pairReversalShuffle.put(2*i+1, inputWidth-2*i -1);
		}
		Circuit pairReversal = new Shuffle(inputWidth, pairReversalShuffle);
		result = pairReversal.extendTopLeft(result, createIdentityMapping(inputWidth));		
		Circuit interleaver = createInterleavingCircuit(inputWidth);
		result = interleaver.extendTopLeft(result, createIdentityMapping(inputWidth));
		
		// output 0 is now the first state bit s0
		// output 1 is now the second state bit s1
		// from specification: s0s1 == 00 iff a=b
		// from specification: s0s1 == 10 iff a>b
		// from specification: s0s1 == 11 iff a<b
		
		return result;
	}

	/**
	 * creates a mapping with i -> i forall i in 0 .. bitwidth
	 * @param bitWidth the width of the identity mapping
	 * @return the described mapping
	 */
	private static Map<Integer, Integer> createIdentityMapping(int bitWidth) {
		Map<Integer, Integer> identity = new HashMap<Integer, Integer>();
		for (int i = 0; i < bitWidth; i++) {
			identity.put(i,i);
		}
		return identity;
	}
	
	/**
	 * This constructs the dominating output circuit. Compare to the
	 * specification for details.
	 *  We require the following input structure:
	 *   - at first, log2(aliceMails) bits in order to encode the number
	 *     of spam-mails alice has
	 *   - second, log2(aliceMails) bits in order to encode the number of
	 *     non-spam-mails alice has
	 *   - third, log2(bobmails) bits in order to encode the number of
	 *     spam-mails bob has
	 *   - fourth, log2(bobmails) bits in order to encode the number of nonspam
	 *     mails bob has.
	 * The logarithms must be rounded up if they are not natural numbers.
	 * The only output bit of the circuit must be 0 if the nonspam mails dominate
	 * and 1 if the spam mails dominate.
	 * @param aliceMails the number of mails alice provides at most
	 * @param bobMail the number of mails bob provides at most.
	 * @return A circuit which does this
	 */
	public static Circuit createDominatingOutputCircuit(int mailCountBound) {
		int inputSize = 4*mailCountBound;
		// swap bits from mailCountBound until mailCountBound*2-1 with
		// bits from mailCountBound*2 until mailCountBound*3-1
		Map<Integer, Integer> sortByType = new HashMap<Integer, Integer>();
		for (int i = 0; i < inputSize; i++) {
			if (0 <= i && i < mailCountBound) {
				sortByType.put(i,i);
			} else if (mailCountBound <= i && i < mailCountBound*2) {
				sortByType.put(i, i+mailCountBound);
			} else if (mailCountBound * 2 <= i &&  i < mailCountBound * 3) {
				sortByType.put(i, i-mailCountBound);
			} else if (3*mailCountBound <= i && i < 4*mailCountBound) {
				sortByType.put(i,i);
			}
		}
		
		Map<Integer, Integer> putNonSpamFirst = new HashMap<Integer, Integer>();
		for (int i = 0; i < inputSize; i++) {
			if (0 <= i && i < 2*mailCountBound) {
				putNonSpamFirst.put(i, i+2*mailCountBound);
			} else {
				putNonSpamFirst.put(i, i-2*mailCountBound);
			}
		}
		
		Circuit result = new Shuffle(inputSize, sortByType);
		result = result.extendTopLeft(new Shuffle(inputSize, putNonSpamFirst),
								createIdentityMapping(inputSize));
		
		Circuit parallelAdders = createAdder(mailCountBound);
		parallelAdders = parallelAdders.extendLeft(createAdder(mailCountBound));
		
		result = result.extendTopLeft(parallelAdders, createIdentityMapping(inputSize));
		result = result.extendTopLeft(createComparer(mailCountBound), createIdentityMapping(mailCountBound*2));
		result = result.extendTopLeft(new Stop(), createIdentityMapping(1));

		return result;
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
		

		//connection represents the identity
		connection.clear();
		for (int j = 0; j < bitwidth; j++) {
			connection.put(j, j);
			connection.put(bitwidth+j, bitwidth+j);		
		}

		Shuffle shuffle = createInterleavingCircuit(bitwidth);
		adder=shuffle.extendTopLeft(adder, connection);
		
		return adder;
	}

	/**
	 * Given a bit sequence A and a bit sequence B, this
	 * creates a circuit that turns the concatenation of A and B
	 * into the interleaving of A and B such that all bits
	 * on even positions come from A and all bits on odd positions
	 * come from B. In the result, the bits of A and the bits of B
	 * are in the same order as they are in A or B alone.
	 * 
	 * a1 a2 a3 b1 b2 b3 -> a1 b1 a2 b2 a3 b3.
	 * @param bitwidth
	 * @return
	 */
	private static Shuffle createInterleavingCircuit(int bitwidth) {
		Map<Integer, Integer> shuffleMap= new HashMap<Integer, Integer>();
		for (int j = 0; j < bitwidth; j++) {
			shuffleMap.put(j, 2*j);
			shuffleMap.put(bitwidth+j, 2*j+1);
		}
		
		Shuffle shuffle = new Shuffle(2*bitwidth, shuffleMap);
		return shuffle;
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
