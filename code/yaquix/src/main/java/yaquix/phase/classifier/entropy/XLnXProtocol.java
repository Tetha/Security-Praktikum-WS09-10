package yaquix.phase.classifier.entropy;

import java.io.IOException;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

public class XLnXProtocol extends Phase {
	/**
	 * contains the local input share
	 */
	private InputKnowledge<Integer> localInput;
	private OutputKnowledge<Integer> localShare;
	
	/**
	 * constructs new execution of the XLnX-Protocol.
	 * @param localInput the local addend of X
	 * @param localShare the local share of x*ln(x)
	 */
	public XLnXProtocol(InputKnowledge<Integer> localInput,
			OutputKnowledge<Integer> localShare) {
		super();
		this.localInput = localInput;
		this.localShare = localShare;
	}

	@Override
	public void clientExecute(Connection connection) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void serverExecute(Connection connection) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub

	}

}
