package yaquix.circuit.base;

import yaquix.circuit.Circuit;

/**
 * This class repeats the given given circuit n times.
 * For each input of the circuit, the repeated circuit requires
 * an input word of n bits instead of a single bit input. The
 * 0th repetition of the circuit operates on the inputs with 
 * index i % n = 0, the 1st repetition operates on the input bits
 * with index i % n = 1, ...
 * 
 * Let m be the number of output bits of the repeated circuit.
 * The output is enlarged into words, that is,
 * each single bit output of the repeated circuit becomes an n-bit
 * output word. Thus, the output has n*m bits.
 *  - output index with n
 *  - circuit index simple
 * Thus, the i'th output of the j'th circuit has index i*n+j. 
 * Thus, the 0th output bit of the 0th circuit
 * is the bit 0+0*n, the 1st output bit of the 0th circuit is the
 * bit 0+1*n, and the 2nd output bit of the first circuit is the bit
 * 1+2*n.
 * @author kraemer
 *
 */
public class Times extends Circuit {
	/**
	 * This creates a circuit that operates on words. 
	 * @param wordWidth the width of the words (n in the class comment)
	 * @param repeatedCircuit the circuit to enlarge
	 */
	public Times(int wordWidth, Circuit repeatedCircuit) {
	}
}
