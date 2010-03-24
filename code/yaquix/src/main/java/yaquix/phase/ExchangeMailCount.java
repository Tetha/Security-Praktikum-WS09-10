package yaquix.phase;

import java.io.IOException;

import yaquix.Connection;

public class ExchangeMailCount extends SymmetricPhase {

	
	/**
	 * contains the local number of mails
	 */
	InputKnowledge<Integer> localMailCount;

	/**
	 * contains the remote number of mails
	 */
	OutputKnowledge<Integer> remoteMailCount;

	public ExchangeMailCount(InputKnowledge<Integer> localMailCount,
			OutputKnowledge<Integer> remoteMailCount) {
		super();
		this.remoteMailCount = remoteMailCount;
		this.localMailCount = localMailCount;
	}

	@Override
	protected void execute(Connection connection) throws IOException,
			ClassNotFoundException {
		logger.info("entering phase");
		remoteMailCount.put(connection.exchangeInteger(localMailCount.get()));
		logger.info("leaving phase");
	}

}
