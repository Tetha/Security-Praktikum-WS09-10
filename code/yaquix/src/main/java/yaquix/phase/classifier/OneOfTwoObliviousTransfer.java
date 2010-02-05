package yaquix.phase.classifier;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

/**
 * This phase implements 1-out-of-2 oblivious transfer.
 * 
 * We implement the 1-out-of-2 oblivious transfer using RSA.
 * Compare to the specification for the detailed algorithm to
 * implement, compare with java.crypto for an RSA implementation.
 * @author hk
 *
 */
class OneOfTwoObliviousTransfer extends Phase {
	/**
	 * If the server side constructor was used and serverExecute
	 * is called, this knowledge will be examined for the two
	 * messages to select one from.
	 */
	private InputKnowledge<int[]> serverMessages;
	
	/**
	 * If the client side constructor was used and clientExecute
	 * is called, this knowledge will be examined for the
	 * bit the client wants to select.
	 */
	private InputKnowledge<Boolean> clientBit;
	
	/**
	 * If the client side constructor was used and clientExecute
	 * is called, this knowledge requires the received
	 * message to be put.
	 */
	private OutputKnowledge<Integer> clientReceivedMessage;
	
	/**
	 * This is the server side constructor. It sets the two messages to 
	 * have the client select from. Note that if you use this constructor,
	 * you MUST call serverExecute or undefined behavior happens.
	 * @param serverMessages contains the two messages to select one from.
	 */
	public OneOfTwoObliviousTransfer(InputKnowledge<int[]> serverMessages) {
		this.serverMessages = serverMessages;
	}

	/**
	 * This is the client side constructor. It sets the bit to select and
	 * eventually stores the received message in the given output knowledge.
	 * Note that if you use this constructor. you MUST call clientExecute
	 * or undefined behaviour happens.
	 * @param clientBit the index of the message to select
	 * @param clientReceivedMessage a place to store the received message.
	 */
	public OneOfTwoObliviousTransfer(InputKnowledge<Boolean> clientBit,
			OutputKnowledge<Integer> clientReceivedMessage) {
		this.clientBit = clientBit;
		this.clientReceivedMessage = clientReceivedMessage;
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
