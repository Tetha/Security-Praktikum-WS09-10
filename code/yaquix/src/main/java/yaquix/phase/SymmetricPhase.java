package yaquix.phase;

import java.io.IOException;

import yaquix.Connection;

/**
 * This implements symmetric phases. Symmetric phases differ only in the
 * order of send/receive's (e.g. the client receives first, while the server
 * sends first). This difference is abstracted in the Connection class
 * via exchange-methods, and thus, we can remove duplication by implementing
 * this class which maps the different execute methods into a single
 * execute method, which is executed by both client and server.
 * @author hk
 *
 */
public abstract class SymmetricPhase extends Phase {
	/**
	 * contains if serverExecute or clientExecute was called.
	 * True is serverExecute, false is clientExecute
	 */
	private boolean wasServer;

	/**
	 * This executes the symmetric operations of the phase. Note
	 * that inside the symmetric execution, you must not use send
	 * and receive-methods, but exchange-methods or no communication
	 * at all, as send/receive is intrinsically asymmetric. (if
	 * the server sends, the client must receive, otherwise we
	 * have a deadlock.)
	 * @param connection the connection to communicate with the other part
	 * @throws IOException
	 */
	protected abstract void execute(Connection connection) throws IOException;

	@Override
	public void serverExecute(Connection connection) throws IOException {
		wasServer = true;
		execute(connection);
	}

	@Override
	public void clientExecute(Connection connection) throws IOException {
		wasServer = false;
		execute(connection);
	}

	/**
	 * calls the appropriate execution method on the phase.
	 * @param connection the connection to pass to the sub phase
	 * @param subPhase the phase to execute
	 * @throws IOException
	 */
	protected void executePhase(Connection connection, Phase subPhase) throws IOException {
		if (wasServer) {
			subPhase.serverExecute(connection);
		} else {
			subPhase.clientExecute(connection);
		}
	}
}
