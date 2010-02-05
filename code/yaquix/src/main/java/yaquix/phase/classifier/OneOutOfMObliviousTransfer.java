package yaquix.phase.classifier;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

/**
 * This implements 1-out-of-M oblivious transfer.
 *
 * Compare to the specification for the detailed algorithm involved.
 * @author hk
 *
 */
class OneOutOfMObliviousTransfer extends Phase {
	/**
	 * This contains the messages the client can choose from, if the
	 * server constructor was used.
	 */
	private InputKnowledge<int[]> serverMessages;
	
	/**
	 * This contains the index the client selects, if the client
	 * side constructor was used.
	 */
	private InputKnowledge<Integer> clientIndex;
	
	/**
	 * This contains the message the client received if the client
	 * constructor was used and clientExecute was called. 
	 */
	private OutputKnowledge<Integer> clientMessage;
	
	
	/**
	 * This is the server side constructor. Note that you MUST
	 * call serverExecute if you call this constructor, otherwise
	 * undefined behavior happens.
	 * @param serverMessages contains the messages the server can
	 * choose from
	 */
	public OneOutOfMObliviousTransfer(InputKnowledge<int[]> serverMessages) {
		this.serverMessages = serverMessages;
	}
	 
	/**
	 * This the client side constructor. Note that you MUST
	 * call clientExecute if you call this constructor, otherwise
	 * undefined behaviour happens.
	 * @param clientIndex this contains the index the client wants to choose.
	 * @param clientMessage a place to store the message the client received
	 */
	public OneOutOfMObliviousTransfer(InputKnowledge<Integer> clientIndex,
			OutputKnowledge<Integer> clientMessage) {
		this.clientIndex = clientIndex;
		this.clientMessage = clientMessage;
	}


	@Override
	public void clientExecute(Connection connection) {
		// TODO client execute

	}

	@Override
	public void serverExecute(Connection connection) {
		// TODO server execute

	}

}
