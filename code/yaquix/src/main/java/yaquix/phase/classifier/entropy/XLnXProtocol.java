package yaquix.phase.classifier.entropy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;
import yaquix.phase.SymmetricPhase;

public class XLnXProtocol extends SymmetricPhase {
	/**
	 * contains the local input share
	 */
	private InputKnowledge<Integer> localInput;
	private OutputKnowledge<Integer> localShare;
	
	private static final Logger LOG = LoggerFactory.getLogger(XLnXProtocol.class);
	
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
	protected void execute(Connection connection) throws IOException,
			ClassNotFoundException {
		// XXX: cheated
		LOG.info("Starting Phase: XLnXProtocol (Faked)");
		int localNumber = localInput.get();
		int remoteNumber = connection.exchangeInteger(localNumber);
		
		int x = (localNumber + remoteNumber);
		double xlnx = x * Math.log(x);
		double share = xlnx / 2;
		int roundedShare = (int) Math.ceil(share);
		
		localShare.put(roundedShare);
		LOG.info("Ending Phase: XLnXProtocol");
	}


}
