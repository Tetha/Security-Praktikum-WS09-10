package yaquix.circuit;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yaquix.circuit.base.And;
import yaquix.circuit.base.Constant;
import yaquix.circuit.base.Input;
import yaquix.circuit.base.Negation;
import yaquix.circuit.base.Or;
import yaquix.circuit.base.Shuffle;
import yaquix.circuit.base.Split;
import yaquix.circuit.base.Stop;
import yaquix.circuit.base.Times;
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
	private static final Logger LOG = LoggerFactory.getLogger(CircuitBuilder.class);
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
	 * This applies the separation into shares to a single bit.
	 * The inputs are:
	 *  - input 0 is the distribution bit
	 *  - input 1 is the distributed bit, ie the extended circuits output
	 * The outputs are:
	 *  - output 0 is the bit for Alices share
	 *  - output 1 is the bit for bobs share
	 *  @return a circuit for the separation of a single bit of the result
	 */
	private static Circuit createSingleBitSeparation() {
		Circuit result;
		HashMap<Integer, Integer> connection = new HashMap<Integer, Integer>();
		result = new Split(2);

		// Inputs: i
		// Outputs: i i
		result.extendLeft(new Split(2));

		// Inputs:  v i
		// Outputs: v v i i

		connection.clear();
		connection.put(0, 0);
		result.extendTopLeft(new Negation(), connection);

		// Outputs: -v v i i

		HashMap<Integer, Integer> shuffle = new HashMap<Integer, Integer>();
		shuffle.put(0,2);
		shuffle.put(1,0);
		shuffle.put(2,1);
		shuffle.put(3,3);
		result.extendTopLeft(new Shuffle(4, shuffle), createIdentityMapping(4));

		// Outputs: v i -v i

		Circuit ands = new And();
		ands = ands.extendLeft(new And());
		// Input ands: a b c d
		// Output ands: a*b c*d

		result.extendTopLeft(ands, createIdentityMapping(4));

		// Outputs: v*i (-v)*i

		return result;
	}
	/**
	 * This applies the extension for turning the output into shares
	 * to a given input circuit. This extension adds a layer of
	 * the distribution net (compare the specification for details)
	 * onto each output gate according to the xor of a bit in an
	 * additional input of each user.
	 * The new inputs of the user are required to prefix the already
	 * existing inputs of the user.
	 * @param outputWidth the outputWidth to extend
	 * @param input the circuit to extend
	 * @return the modified circuit
	 */
	public static Circuit extendWithShareSeparation(Circuit input, int outputWidth,
													int aliceInputWidth, int bobInputWidth) {
		Circuit distVectComp = times(outputWidth, new Xor());
		Circuit inputWithDistComp = input.extendLeft(distVectComp);
		Circuit result = inputWithDistComp.extendTopLeft(
											times(outputWidth, createSingleBitSeparation()),
											createIdentityMapping(outputWidth));
		HashMap<Integer, Integer> shuffleMap = new HashMap<Integer, Integer>();
		for(int i = 0; i < 2*outputWidth + aliceInputWidth + bobInputWidth; i++) {
			if (0 <= i && i < outputWidth) {
				// KA
				shuffleMap.put(i, i);
			} else if (outputWidth <= i && i < outputWidth + aliceInputWidth) {
				// IA
				shuffleMap.put(i, i+outputWidth);
			} else if (outputWidth + aliceInputWidth <= i && i < 2*outputWidth + aliceInputWidth) {
				// KB
				shuffleMap.put(i, i-aliceInputWidth);
			} else if (2*outputWidth + aliceInputWidth <= i && i < 2*outputWidth + aliceInputWidth + bobInputWidth) {
				// IB
				shuffleMap.put(i, i);
			}
		}
		Circuit shuffle = new Shuffle(2*outputWidth + aliceInputWidth + bobInputWidth, shuffleMap);
		result = shuffle.extendTopLeft(result, createIdentityMapping(2*outputWidth + aliceInputWidth + bobInputWidth));

		return result;
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
     * This method appears to work.
	 * @return a circuit to compute the state transition of the
	 *         comparision automaton.
	 */
	public static Circuit createComparerStateTransition() {
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
	 * creates a circuit that compares two number a, b of equal bitwidth
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
	 * // this method appears to work
	 * @param bitWidth the bitWidth of the numbers to compare
	 * @return a circuit to compute the comparision.
	 */
	public static Circuit createComparer(int bitWidth) {
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
		Circuit interleaver = createInterleavingCircuit(bitWidth);
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
     *
     * This method appears to work properly.
	 * @return a full adder
	 */
	public static Circuit createFullAdder() {
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
		fullAdder=fullAdder.extendTopLeft(new Split(3), connection);
		//output A A A C C B
		connection.clear();
		connection.put(5, 0);
		fullAdder=fullAdder.extendTopLeft(new Split(3), connection);
		//outputs till now: B B B A A A C C

		connection.clear();
		connection.put(3, 0);
		connection.put(2, 1);
		fullAdder=fullAdder.extendTopLeft(new Xor(), connection);
		//output: BxA B B A A C C

		connection.clear();
		connection.put(1, 0);
		connection.put(3, 1);
		fullAdder=fullAdder.extendTopLeft(new And(), connection);
		//output: B*A BxA B A C C

		connection.clear();
		connection.put(2, 0);
		connection.put(3, 1);
		fullAdder=fullAdder.extendTopLeft(new Or(), connection);
		//output: B+A B*A BxA C C

		connection.clear();
		connection.put(2, 0);
		connection.put(3, 1);
		fullAdder=fullAdder.extendTopLeft(new Xor(), connection);
		//output: (BxA)xC B+A B*A C

		connection.clear();
		connection.put(1, 0);
		connection.put(3, 1);
		fullAdder=fullAdder.extendTopLeft(new And(), connection);
		//output (B+A)*C (BxA)xC B*A

		connection.clear();
		connection.put(0, 0);
		connection.put(2, 1);
		fullAdder=fullAdder.extendTopLeft(new Or(), connection);
		//output ((B+A)*C)+(B*A) (BxA)xC

		return fullAdder;
	}

	/**
	 * This creates a ripple carry adder for two bit numbers.
	 * The first number is required to be the first half of the input
	 * while the second number is required to be the second half of the input.
	 * We don't care for overflows, as we use bitwidths large enough, so the
	 * output is as wide as the inputs.
     *
     * this method appears to work properly.
     *
	 * @return a ripple carry adder
	 */
	public static Circuit createAdder(int bitwidth) {
		Map<Integer, Integer> connection= new HashMap<Integer, Integer>();
		connection.put(0, 0);
		if (bitwidth < 1) {
			throw new IllegalArgumentException();
		}
		Circuit adder=new Constant(false);
		for (int i=0; i<bitwidth; i++) {
			adder=adder.extendTopLeft(createFullAdder(), connection);
		}

		// kick last carry
		Circuit stop = new Stop();
		connection.clear();
		connection.put(0,0);
		adder = adder.extendTopLeft(stop, connection);

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
	 * This performs a single step in the table lookups.
	 *
	 * The first keyWidth bits of the input must be the key of the currently
	 * examine key-value-pair.
	 * The second valueWidth bits of the input must be the value of the current
	 * key-value-pair.
	 * The third keyWidth bits of the input must be the key we are searching.
	 * The fourth valueWidth bits of the input are the value we have found
	 * so far (or it is undefined).
	 * @param keyWidth the bitwidth of the keys
	 * @param valueWidth the width of the values
	 */
	private static Circuit createLookupTableStateTransition(int keyWidth, int valueWidth) {
		HashMap<Integer, Integer> connection = new HashMap<Integer, Integer>();
		Circuit result = times(keyWidth, new Split(2));
		// Inputs: [keyWidth S]
		// Outputs: [keyWidth S] [keyWidth S]

		result = result.extendTopLeft(createComparer(keyWidth), createIdentityMapping(keyWidth));

		// Inputs: [keyWidth K] [keyWidth S]
		// Outputs: C= C> [keyWidth S]

		connection.clear();
		connection.put(1, 0);

		result = result.extendTopLeft(new Stop(), connection);

		// Outputs: C= [keyWidth S]

		result = result.extendTopLeft(new Split(2*valueWidth), createIdentityMapping(1));

		// Outputs: [valueWidth C=] [valueWidth C=] [keyWidth S]

		//--------------------------------------------------------------
		Circuit middleLeft;
		middleLeft = times(valueWidth, new And());

		// Inputs middleLeft: [valueWidth X] [valueWidth Y]
		// Outputs middleLeft: [valueWidth X*Y]

		Circuit middleRight = times(valueWidth, new Negation());
		// Inputs middleRight: [valueWidth A]
		// Outputs middleRight: [valueWidth -A]

		middleRight = middleRight.extendTopLeft(times(valueWidth, new And()), createIdentityMapping(valueWidth));

		// Inputs middleRight: [valueWidth B] [valueWidth A]
		// Outputs middleRight [valueWidth B*-A]

		Circuit middle = middleRight.extendLeft(middleLeft);
		// Inputs middle: [valueWidth X] [valueWidth Y] [valueWidth B] [valueWidth A]
		// Outputs middle: [valueWidth X*Y] [valueWidth B*-A]

		Circuit fComputation = middle.extendTopLeft(times(valueWidth, new Or()), createIdentityMapping(2*valueWidth));

		// Inputs fComputation: [valueWidth X] [valueWidth Y] [valueWidth B] [valueWidth A]
		// Outputs fComputation: [valueWidth (X*Y)+(B*-A)]
		//--------------------------------------------------------------

		connection.clear();
		// Identity: X <- V; Y <- C=; B <- F; A <- C=
		for(int i = 0; i < 2*valueWidth+keyWidth; i++) {
			if (0 <= i && i < valueWidth) {
				// first block C=
				connection.put(i, i+valueWidth);
			} else if(valueWidth <= i && i < 2*valueWidth) {
				// second block C=
				connection.put(i, i+2*valueWidth);
			} else if(2*valueWidth <= i && i < 2*valueWidth + keyWidth) {
				// S
			}
		}
		result = result.extendTopLeft(fComputation, connection);

		// Inputs: [valueWidth V] [valueWidth F] [keyWidth K] [keyWidth S]
		// Outputs: [valueWidth (V*C=)+(F*-C=)] [keyWidth S]
		// Identify F' = (V*C=)+(F*-C=)
		// Outputs: [valueWidth F'] [keyWidth S]

		HashMap<Integer, Integer> shuffleInput = new HashMap<Integer, Integer>();
		for(int i = 0; i < 2*valueWidth + 2*keyWidth; i++) {
			if(0 <= i && i < keyWidth) {
				shuffleInput.put(i, i+2*valueWidth);
			} else if (keyWidth <= i && i < keyWidth+valueWidth) {
				shuffleInput.put(i, i-keyWidth);
			} else if (keyWidth + valueWidth <= i && i < 2*keyWidth + valueWidth) {
				shuffleInput.put(i, i+valueWidth);
			} else if (2*keyWidth + valueWidth <= i && i < 2*keyWidth + 2*valueWidth) {
				shuffleInput.put(i, i-2*keyWidth);
			}
		}

		result = new Shuffle(2*valueWidth + 2*keyWidth, shuffleInput).extendTopLeft(result, createIdentityMapping(2*valueWidth+2*keyWidth));

		// Input: [keyWidth K] [valueWidth V] [keyWidth S] [valueWidth F]
		// Outputs: [valueWidth F'] [keyWidth S]

		shuffleInput.clear();
		for(int i = 0; i < valueWidth+keyWidth; i++) {
			if (0 <= i && i < valueWidth) {
				// in F'
				shuffleInput.put(i, i+keyWidth);
			} else if (valueWidth <= i && i < valueWidth+keyWidth) {
				// in S
				shuffleInput.put(i, i-valueWidth);
			}
		}
		result = result.extendTopLeft(new Shuffle(valueWidth+keyWidth, shuffleInput), createIdentityMapping(valueWidth+keyWidth));

		// Input: [keyWidth K] [valueWidth V] [keyWidth S] [valueWidth F]
		// Outputs: [keyWidth S] [valueWidth F']
		return result;
	}

	/**
	 * Generates a lookup table.
	 *
	 * The input has to be a sequence of key, value pairs followed by the
	 * key to look up. That is, if ki are keys and vi are values and s is the
	 * searched key, then the input  must be structured like k1 v1 k2 v2 ... s
	 *
	 * The output consists of valueWidth bits with the entry of the lookup table.
	 * If the element was not in the table, the result is not defined.
	 * @param pairCount the number of entries in the lookup table
	 * @param keyWidth the bit width of the keys
	 * @param valueWidth the bit width of the values.
	 * @return
	 */
	private static Circuit createLookUpTable(int pairCount, int keyWidth, int valueWidth) {

		HashMap<Integer, Integer> connection = new HashMap<Integer, Integer>();

		Circuit sInput = times(keyWidth, new Input());
		Circuit initialF = times(valueWidth, new Constant(false));
		Circuit result = initialF.extendLeft(sInput);

		for(int i = 0; i < valueWidth + keyWidth; i++) {
			if (0 <= i && i < keyWidth) {
				// S
				connection.put(i, keyWidth+valueWidth+i);
			} else if (keyWidth  <= i && i < keyWidth + valueWidth) {
				connection.put(i, keyWidth+valueWidth+i);
			}
		}
		for (int i = 0; i < pairCount; i++) {
			result = result.extendTopLeft(createLookupTableStateTransition(keyWidth, valueWidth), connection);
		}
		return result;
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

	/**
	 * This constructs a circuit which checks if all mail labels are the same.
	 * The input has to be structured as follows:
	 *  - every even and odd bit encode a mail label. A spam label is encoded with
     * 01, a nonspam label is encoded with 00 and an absent label is encoded with 11.
     * The encoding is defined in the specification
	 * The output has to be structured as follows:
	 *  - if the labels agree "Spam", then the first bit of the output must be 0 and the second
	 *    bit of the output must be 1
	 *  - if the labels agree "Non Spam", then the first bit of the output must be 1 and the second
	 *    bit of the output must be 0
	 *  - if the labels disagree, the first bit must be 1 and the second bit must be 1.
	 * @param mailCount the overall amount of e-mails used
	 * @return a circuit computing this.
	 */
	public static Circuit createAgreeingLabelComputation(int mailCount) {
		Circuit transition = createAgreeingLabelTransition();
		String LOGName = LOG.getName();
		String myName = LOGName + ".AgreeingLabelComputationTransition";
		Logger myLogger = LoggerFactory.getLogger(myName);
		myLogger.trace(transition.dumpAsDot());
		Circuit result = new Constant(false);
		result = result.extendLeft(new Constant(false));

		for (int i = 0; i < mailCount; i++) {
			result = result.extendTopLeft(transition, createIdentityMapping(2));
		}

		myName = LOGName + ".AgreeingLabelComputation";
		myLogger = LoggerFactory.getLogger(myName);
		myLogger.trace(result.dumpAsDot());

		return result;
	}

	/**
	 * @return
	 */
	private static Circuit createAgreeingLabelTransition() {
		// Inputs
		// Outputs

		Circuit result = new Split(2);

		// Inputs: e1
		// Outputs e1 e1

		result = result.extendLeft(new Split(2));

		// Inputs: e0 e1
		// Outputs: e0 e0 e1 e1

		result = result.extendLeft(new Split(3));

		// Inputs: s1 e0 e1
		// Outputs s1 s1 s1 e0 e0 e1 e1

		result = result.extendLeft(new Split(2));

		// Inputs: s0 s1 e0 e1
		// Outputs: s0 s0 s1 s1 s1 e0 e0 e1 e1

		Circuit layer1 = new Negation();
		layer1 = layer1.extendLeft(new Negation());
		layer1 = layer1.extendLeft(new And());
		layer1 = layer1.extendLeft(new Or());

		// layer 1: or and neg neg

		Map<Integer, Integer> inputToLayer1 = new HashMap<Integer, Integer>();
		inputToLayer1.put(0, 0); // s0 -> left of first or
		inputToLayer1.put(2, 1); // s1 -> right of first or
		inputToLayer1.put(1, 2); // s0 -> left of first and
		inputToLayer1.put(3, 3); // s1 -> right of first and
		inputToLayer1.put(5, 4); // e0 -> negation
		inputToLayer1.put(7, 5); // e1 -> negation

		result = result.extendTopLeft(layer1, inputToLayer1);

		// Outputs: s0+s1 s0*s1 -e0 -e1 s1 e0 e1

		Circuit layer2 = new And();
		layer2 = layer2.extendLeft(new Or());
		layer2 = layer2.extendLeft(new And());

		// layer 2: And Or And

		Map<Integer, Integer> layer1ToLayer2 = new HashMap<Integer, Integer>();
		layer1ToLayer2.put(5, 0); // e0 -> left of and
		layer1ToLayer2.put(0, 1); // s0+s1 -> right of and
		layer1ToLayer2.put(1, 2); // s0*s1 -> left of or
		layer1ToLayer2.put(3, 3); // -e1 -> right of or
		layer1ToLayer2.put(2, 4); // -e0 -> left of and
		layer1ToLayer2.put(6, 5); // e1 -> right of and

		result = result.extendTopLeft(layer2, layer1ToLayer2);

		// e0*(s0+s1) (s0*s1)+(-e1) (-e0)*e1 s1

		Circuit layer3 = new Or();
		layer3 = layer3.extendLeft(new Or());

		Map<Integer, Integer> layer2ToLayer3 = new HashMap<Integer, Integer>();
		layer2ToLayer3.put(0, 0); // e0*(s0+s1) -> left or
		layer2ToLayer3.put(1, 1); // (s0*s1)+(-e1) -> right or
		layer2ToLayer3.put(2, 2); // (-e0)*e1 -> left or
		layer2ToLayer3.put(3, 3); // s1 -> right or

		result = result.extendTopLeft(layer3, layer2ToLayer3);
		return result;
	}

	/**
	 * This constructs the state transition net for the
	 * max gain circuit.
	 * The first sumWidth inputs must be the current
	 * maximum sum.
	 * The second indexWidth inputs must be the current
	 * maximum index.
	 * The third sumWidth inputs must be the current
	 * input sum.
	 * The fourth indexWidth inputs must be the current
	 * sum index.
	 *
	 *
	 * Then,
	 * the first sumWidth outputs will be the
	 * new maximum sum,
	 * the second indexWidth outputs will be the
	 * new maximum index.
	 * @param sumWidth the width of the sums
	 * @param indexWidth the index of the maximum
	 * @return a circuit to do a single compare
	 * and copy.
	 */
	public static Circuit createMaxGainStatetransition(int sumWidth, int indexWidth) {
		Map<Integer, Integer> connection = new HashMap<Integer, Integer>();
		Circuit result = null;

        Circuit condAssign = makeCondAssign();

		// Inputs: B D C
		// Outputs condAssign: (C*B)+(-C*D)

		// RENAME: C -> B, B -> T, D -> F
		// Inputs condAssign: T F B
		// Outputs condAssign: (B*T)+(-B*F)

		Circuit condIndexAssign = times(indexWidth, condAssign);

		// Inputs: [indexWidth x T] [indexWidth x F] [indexWidth x B]
		// Outputs [indexWidth x (B*T)+(-B*F)]

		connection.clear();
		for (int i  = 0; i < indexWidth; i++) {
			connection.put(i, i+2*indexWidth);
		}
		condIndexAssign = new Split(indexWidth).extendTopLeft(condIndexAssign, connection);

		// Inputs condIndexAssign: [indexWidth x T] [indexWidth x F] B
		// Outputs condIndexAssign: [indexWidth x (B*T)+(-B*F)]

		Circuit condSumAssign = times(sumWidth, condAssign);
		// Inputs condSumAssign: [sumWidth x T] [sumWidth x F] [sumWidth x B]
		// Outputs condSumAssign: [sumWidth * (B*T)+(-B*F)]

		connection.clear();
		for (int i = 0; i < sumWidth; i++) {
			connection.put(i, i+2*sumWidth);
		}
		condSumAssign = new Split(sumWidth).extendTopLeft(condSumAssign,  connection);

		// Inputs condsumAssign: [sumWidth x T] [sumWidth x F] B
		// Outputs condSumAssign: [sumWidth x (B*T)+(-B*F)]

		Circuit selectLayer = condIndexAssign.extendLeft(condSumAssign);

		// Inputs selectLayer: [sumWidth x T] [sumWidth x F] B [indexWidth x T] [indexWidth x F] A
		// Ouputs selectLayer: [sumWidth x (B*T)+(-B*F)] [indexWidth x (A*T)+(-A*F)]

		connection.clear();
		connection.put(0, 2*sumWidth);
		connection.put(1, 2*sumWidth+1+2*indexWidth);
		selectLayer = new Split(2).extendTopLeft(selectLayer, connection);

		// Inputs selectLayer: [sumWidth x T] [sumwidth x F] [indexWidth x T] [indexWidth x F] B
		// Outputs selectLayer [sumWidth x (B*T)+(-B*F)] [indexWidth x (B*T)+(-A*F)]
		// Abstraction: Outputs selectLayer: [sumWidth x if B then T else F] [indexWidth x if B then T else F]

		Circuit rawComparer = createComparer(sumWidth);
		// Inputs rawComparer: [sumWidth : A] [sumWidth : B]
		// Outputs rawComparer: (A=B: 0, A>B||A<B 1) (A=B: ?, A>B:0, A<B: 1)

		Circuit strictLessThan = rawComparer.extendTopLeft(new And(), createIdentityMapping(2));

		// Inputs strictLessThan: [sumWidth: A] [sumWidth: B]
		// Outputs strictLessThan: (A=B||A>B: 0, A<B: 1)

		connection.clear();
		connection.put(0, 2*sumWidth + 2*indexWidth);
		result = strictLessThan.extendTopLeft(selectLayer, connection);

		// Inputs result: [sumWidth x T] [sumWidth x F] [indexWidth x T] [indexWidth x F] [sumWidth: A] [sumWidth: B]
		// Outputs: [sumWidth x if (A < B) then T else F] [indexWidth x if (A < B) then T else F]
		// RENAME: 1.T -> M, 1.F -> C 2.T -> I 2.F -> J, A -> X, B -> Y
		// Inputs result: [sumWidth x M] [sumWidth x C] [indexWidth x I] [indexWidth x J] [sumWidth: X] [sumWidth: Y]
		// Oututs: [sumWidth x if (X < Y) then M else C] [indexWidth x if (X < Y) then I else J]

		// make x and c the same
		connection.clear();
		for (int i = 0; i < 2*sumWidth; i++) {
			if (0 <= i && i < sumWidth) {
				connection.put(i, i+sumWidth);
			} else if (sumWidth <= i && i < 2*sumWidth) {
				connection.put(i, i+sumWidth+2*indexWidth);
			}
		}
		result = times(sumWidth, new Split(2)).extendTopLeft(result, connection);

		// Inputs result: [sumWidth x M] [indexWidth x I] [indexWidth x J] [sumWidth: Y] [sumWidth: C]
		// Outputs result: [sumWidth x if (C < Y) then M else C] [indexWIdth x if (C < Y) then I else J]

		// make m and y the same
		connection.clear();
		for (int i = 0; i < 2*sumWidth; i++) {
			if (0 <=  i && i < sumWidth) {
				connection.put(i, i);
			} else if (sumWidth <= i && i < 2*sumWidth) {
				connection.put(i, i+2*indexWidth);
			}
		}
		result = times(sumWidth, new Split(2)).extendTopLeft(result, connection);

		// Inputs result: [indexWidth x I] [indexWidth x J] [sumWidth: C] [sumWidth: M]
		// Outputs result: [sumWidth x if (C < M) then M else C] [indexWidth x if (C < M) then I else J]
		Map<Integer, Integer> shuffleMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < 2*indexWidth + 2*sumWidth; i++) {
			if (0 <= i && i < sumWidth) {
				shuffleMap.put(i, i+2*indexWidth+sumWidth);
			} else if (sumWidth <= i && i < sumWidth + indexWidth) {
				shuffleMap.put(i, i - sumWidth);
			} else if (sumWidth + indexWidth <= i && i < 2*sumWidth + indexWidth) {
				shuffleMap.put(i, i-sumWidth+indexWidth);
			} else if (2*sumWidth + indexWidth <= i && i < 2*sumWidth + 2*indexWidth) {
				shuffleMap.put(i, i - 2*sumWidth);
			}
		}
	    result = new Shuffle(2*sumWidth+2*indexWidth, shuffleMap).extendTopLeft(result,
							 createIdentityMapping(2*sumWidth + 2*indexWidth));
		return result;

	}

    /**
     * The cond-assign circuit has three input bits:
     *  - the first bit is the truth value, that is, the value which is
     *    selected if the condition is true
     *  - the second bit is the false value, that is, the value which
     *    is selected if the condition is false
     *  - the third bit is the condition code, that is, this bit is 1
     *    if the truth-value is supposed to be selected and it is 0
     *    if the false-value is to be selected.
     *
     * the output of the cond-assign is a single bit, which is either
     * truthvalue or false-value depending on the condition.
     * @return
     */
    public static Circuit makeCondAssign() {
        Map<Integer, Integer> connection = new HashMap<Integer, Integer>();
        Circuit condAssign = new Or();
        // Inputs condAssign: X Y
        // Outputs condAssign: X+Y
        Circuit filterAnds = new And();
        filterAnds = filterAnds.extendLeft(new And());
        // Inputs filterAnds: A B C D
        // Outputs filterAnds: A*B C*D

        condAssign = filterAnds.extendTopLeft(condAssign, createIdentityMapping(2));

        // Inputs condAssign: A B C D
        // Outputs condAssign: (A*B)+(C*D)

        connection.clear();
        connection.put(0, 3);
        condAssign = new Negation().extendTopLeft(condAssign, connection);

        // Inputs: A B D C
        // Outputs condAssign: (A*B)+(-C*D)

        connection.clear();
        connection.put(0, 0);
        connection.put(1, 3);
        condAssign = new Split(2).extendTopLeft(condAssign, connection);
        // Inputs: B D C
        // Outputs condAssign: (C*B)+(-C*D)

        // RENAME: C -> B, B -> T, D -> F
        // Inputs condAssign: T F B
        // Outputs condAssign: (B*T)+(-B*F)

        return condAssign;
    }

    private static int intLog2(int x) {
		int currentX = 1;
		int n = 0;
		while (currentX < x) {
			currentX *= 2;
			n++;
		}
		return n;
	}

	/**
	 * Constructs the circuit which finds the maximum gain. In order to do this, the
	 * circuit gets two vectors of entropy-shares and outputs the index in the
	 * vector with maximum sum. This index is then identical to the index of the
	 * attribute with the maximum information gain.
	 *
	 * The input of the circuit consists of VECTORLENGTH SHAREWIDTH bit numbers
	 * which are the shares of alice, followed by VECTORLENGTH SHAREWIDTH bit numbers
	 * which are the shares of bob. The output of the circuit is a log_2(VECTORLENGTH)
	 * long binary encoded integer which is the index of the attribute with maximum
	 * information gain.
	 *
	 * @param vectorLength the number of shares to search
	 * @param shareWidth the bitwidth of each share
	 * @return a circuit to perform this computation.
	 */
	 public static Circuit createMaximumGainCircuit(int vectorLength, int shareWidth) {
		 /*
		  * We construct this circuit in several layers from output to input.
		  *
		  * (1) we construct a properly sized linear search network using the
		  * automaton implemented in createMaxGainStateTransition. Note that
		  * every but the first state transition adds a single new sum that
		  * can be handled, while the first state transition adds 2. This means,
		  * we need vectorlength -1 transition networks, which add 2 + 1 + ..
		  * (vectorlength-2 many 1s) = vectorlength sums we can handle.
		  * Let indexWidth be the smallest integer larger than log_2(VECTORLENGTH)
		  * This presents us with the following required inputs:
		  * [ shareWidth+1: A0+B0 ] [ indexWidth: idx0 ] [ shareWidth+1: A1+B1 ] [ indexWidth: idx1 ]
		  *
		  * (2) we add the indexes with constant gates such that idx 0 becomes 0,
		  * idx1 becomes 1, and so on.
		  * Index #k is preceded by k+1 sums and k indexes. Thus, the bits of index
		  * #k must go from (k+1)*sumWidth  + k*indexWidth.
		  * This leaves us with the inputs:
		  * [ shareWidth+1: A0+B0 ] [ shareWidth+1: A1 + B1 ] ..
		  *
		  * (3) we add the summation of the shares. Since the adder circuit assumes
		  * that the bitwidth is large enough to handle overflows, this requires the inputs:
		  * [ shareWidth+1: A0 ] [ shareWidth+1 B0 ] [ shareWidth+1 A1 ] [ shareWidth+1 B1 ] ...
		  *
		  * (4) Since the input is shareWidth bits only, however, we need to precede
		  * each chunk with a constant 0 gate. In other words, we need 2*vectorlength many
		  * constant 0-gates, and need to connect 0-gate #i with input (shareWidth+1) * i of the
		  * created circuit
		  * Thus the inputs after (4) are:
		  * [ shareWidth: A0 ] [ shareWidth B0 ] [ shareWidth A1 ] [ shareWidth B1 ]
		  *
		  * (5) in order to comply with the input specification, we need to reorder this.
		  * [ shareWidth: A0 ] [ shareWidth A1 ] ... [ shareWidth B0 ] [ shareWidth B1 ]
		  *
		  * Thus, the Ai needs to be moved i * shareWidth to the right (as there will be
		  * i many Bx before it), and the Bi needs to be moved to index
		  * shareWidth + 2*i*shareWidth (as there is always one Ai in front of it, and
		  * 2*i pairs of A0 and B0).
		  *
		  * (6) Finally, we need to prevent the value for the maximum sum to be output.
		  * This is done by putting the first sumWidth outputs into stop-circuits.
		  */

		 LOG.trace("Creating max gain circuit.");
		 Map<Integer, Integer> connection = new HashMap<Integer, Integer>();
		 int sumWidth = shareWidth +1;
		 int indexWidth = intLog2(vectorLength);
		 LOG.trace("Step 1");
		 // (1)
		 Circuit stateTransition = createMaxGainStatetransition(sumWidth, indexWidth);
		 Circuit result = stateTransition;
		 for (int i = 0; i < vectorLength-2; i++) {
			 result = result.extendTopLeft(stateTransition, createIdentityMapping(sumWidth+indexWidth));
		 }
		 LOG.trace("Step 2");
		 // (2)
		 for (int indexIndex = 0; indexIndex < vectorLength; indexIndex++) {
			 connection.clear();
			 Circuit indexConstantGates = encodeInteger(indexIndex, indexWidth);
			 for (int indexBitIndex = 0; indexBitIndex < indexWidth; indexBitIndex++) {
				 int indexOffset = (indexIndex+1)*sumWidth;
				 connection.put(indexBitIndex, indexOffset+indexBitIndex);
			 }
			 result = indexConstantGates.extendTopLeft(result, connection);
		 }

		 LOG.trace("Step 3");
		 // (3)
		 connection.clear();
		 Circuit adder = createAdder(sumWidth);
		 Circuit adders = null;
		 for (int i = 0; i < vectorLength; i++) {
			 if (adders == null) {
				 adders = adder;
			 } else {
				 adders = adders.extendLeft(adder);
			 }
		 }
		 result = adders.extendTopLeft(result, createIdentityMapping(vectorLength*sumWidth));

		 LOG.trace("Step 4");
		 // (4)
		 connection.clear();
		 Circuit zero = new Constant(false);
		 Circuit zeros = null;
		 for(int i = 0; i < 2*vectorLength; i++) {
			 if (zeros == null) {
				 zeros = zero;
			 } else {
				 zeros = zeros.extendLeft(zero);
			 }
			 connection.put(i, sumWidth*i);
		 }
		 result = zeros.extendTopLeft(result, connection);

		 LOG.trace("Step 5");
		 // (5)
		 Map<Integer, Integer> shuffleMap = new HashMap<Integer, Integer>();
		 for (int aIndex = 0; aIndex < vectorLength; aIndex++) { // Ai s
			 int inputOffset = aIndex * shareWidth;
			 for (int bitIndex = inputOffset; bitIndex < inputOffset + shareWidth; bitIndex++) {
				 int bitDestination = bitIndex + aIndex*shareWidth;
				 assert 0 <= bitIndex && bitIndex < 2*vectorLength * shareWidth;
				 assert 0 <= bitDestination && bitDestination < 2*vectorLength * shareWidth;

				 shuffleMap.put(bitIndex, bitDestination);
			 }
		 }

		 for (int bIndex = 0; bIndex < vectorLength; bIndex++) { // Bi's
			 // that many A's * the width of one A + the Bis in front of me * the width of one Bi
			 int inputOffset = vectorLength * shareWidth + bIndex * shareWidth;
			 int pairsToSkip = bIndex;
			 //   pair of       Ai          Bi           my corresponding Ax
			 int destinationOffset = pairsToSkip * (shareWidth + shareWidth) + shareWidth;
			 for (int bitIndex = 0; bitIndex < shareWidth; bitIndex++) {
				 assert 0 <= inputOffset + bitIndex && inputOffset + bitIndex < 2*vectorLength*shareWidth;
				 assert 0 <= destinationOffset + bitIndex && destinationOffset + bitIndex < 2*vectorLength * shareWidth;
				 shuffleMap.put(inputOffset+bitIndex, destinationOffset+bitIndex);
			 }
		 }
		 Circuit inputReorderer = new Shuffle(2*vectorLength*shareWidth, shuffleMap);
		 result = inputReorderer.extendTopLeft(result, createIdentityMapping(2*vectorLength*shareWidth));

		 LOG.trace("Step 6");
		 // (6)
		 Circuit stop = new Stop();
		 Circuit stops = null;
		 for (int i = 0; i < sumWidth; i++) {
			 if (stops == null) {
				 stops = stop;
			 } else {
				 stops = stops.extendLeft(stop);
			 }
		 }

		 result = result.extendTopLeft(stops, createIdentityMapping(sumWidth));
		 return result;
	}

	private static Circuit encodeInteger(int indexIndex, int indexWidth) {
		Circuit result = null;
		String tortureChamber = "";
		for (int i = 0; i < indexWidth; i++) {
			Circuit newBit;
			if ((indexIndex & (1 << i)) > 0) {
				newBit = new Constant(true);
				tortureChamber = "1" + tortureChamber;
			} else {
				newBit = new Constant(false);
				tortureChamber = "0" + tortureChamber;
			}
			if (result == null) {
				result = newBit;
			} else {
				result = result.extendLeft(newBit);
			}
		}

		return result;
	}

    /**
     * This applies a single bit circuit to bit words of a certain width by
     * applying the single bit circuit to each bit of the bit word independently.
     *
     *
     * @param wordWidth the width of the bits to require as input
     * @param repeated the circuit to repeat
     * @return a circuit working on the words
     */
	public static Circuit times(int wordWidth, Circuit repeated) {
        assert 0 < wordWidth;
        assert repeated != null;
		/*
		 * At first, we have a circuit with
		 *  - inputs i1 i2 i3 ..
		 *  - outputs o1 o2 o3
		 *
		 * We then build wordWidth many circuits in parallel. This results
		 * in
		 *  - inputs: i11 i12 i13 ... i21 i22 i23 ...
		 *  - outputs o11 o12 o13 ... o21 o22 o23 ...
		 *
		 * We then shuffle the inputs and outputs accordingly, that is,
		 * input A of circuit B goes to input A*#Inputs + B and
		 * output C of circuit D goes to output C*N+D.
		 *
		 */
		Circuit result = null;
		for (int repetitions = 0; repetitions < wordWidth; repetitions++) {
			if (result == null) {
				result = repeated;
			} else {
				result = result.extendLeft(repeated);
			}
		}
        assert result != null;
		int inputCount = repeated.getInputCount();
        int outputCount = repeated.getOutputCount();
        HashMap<Integer, Integer> inputShuffle = new HashMap<Integer, Integer>();

        for (int blockIndex = 0; blockIndex < inputCount; blockIndex++) {
            for (int bitIndex = 0; bitIndex < wordWidth; bitIndex++) {
                int sourceIndex = blockIndex * wordWidth + bitIndex;
                int destinationIndex = bitIndex * inputCount + blockIndex;
                inputShuffle.put(sourceIndex, destinationIndex);

            }
        }

        int timesWidth = wordWidth*inputCount;
        result = new Shuffle(timesWidth, inputShuffle).extendTopLeft(result, createIdentityMapping(timesWidth));

        HashMap<Integer, Integer> outputShuffle = new HashMap<Integer, Integer>();
		for (int circuitIndex = 0; circuitIndex < wordWidth; circuitIndex++) {
			for(int outputIndex = 0; outputIndex < outputCount; outputIndex++) {
                int sourceIndex = circuitIndex * outputCount + outputIndex;
                int destinationIndex = outputIndex * wordWidth + circuitIndex;
                assert !outputShuffle.keySet().contains(sourceIndex);
                assert !outputShuffle.values().contains(destinationIndex);
                outputShuffle.put(sourceIndex, destinationIndex);
			}
		}
		result = result.extendTopLeft(new Shuffle(wordWidth*outputCount, outputShuffle), createIdentityMapping(wordWidth*outputCount));
		return result;
	}
}
