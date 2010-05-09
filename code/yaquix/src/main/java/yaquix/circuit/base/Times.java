package yaquix.circuit.base;

import yaquix.circuit.Circuit;

/**
 * This class repeats the given given circuit n times.
 * 
 * If the initial circuit requires I input bits, then the
 * repeated input circuit requires n*I input bits.
 * If the initial circuit prdouces O output bits, then the
 * repeated circuit produces n*O output bits.
 * 
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
		
	}
}
