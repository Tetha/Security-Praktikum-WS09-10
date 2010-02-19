package yaquix.phase.classifier.entropy;

import java.security.SecureRandom;

public class OneWayFunctionFamiliy {
	/**
	 * contains the secure random number generator to use.
	 */
	private SecureRandom random;
	
	/**
	 * constructs a new one way function familiy. Note that the
	 * random number instance must not be the random number instance
	 * used elsewhere as the seed is reset frequently.
	 * @param random the secure random number generator to use
	 */
	public OneWayFunctionFamiliy(SecureRandom random) {
		this.random = random;
	}
	
	/**
	 * evaluates the function identified by k at the point x.
	 * @param K the key to identify the function
	 * @param x the value to evaluate the function in
	 */
	public long evaluate(long K, int x) {
		long i = K;
		for (i = 0; i < 32; i++) { // 32 bit integers
			if ((x & (1 << i)) > 0) {
				// bit is 1
				random.setSeed(i);
				random.nextLong();
				i = random.nextLong();
			} else {
				//bit is 0
				random.setSeed(i);
				i = random.nextLong();
			}
		}
		return i;
	}
}
