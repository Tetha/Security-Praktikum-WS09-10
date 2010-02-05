package yaquix.phase;

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
	 * This executes the symmetrisc operations of the phase. Note
	 * that inside the symmetric execution, you must not use send
	 * and receive-methods, but exchange-methods or no communication
	 * at all, as send/receive is intrinsically asymetric. (if 
	 * the server sends, the client must receive, otherwise we
	 * have a deadlock.)
	 * @param connection the connection to communicate with the other part
	 */
	protected abstract void execute(Connection connection);
	
	@Override
	public void serverExecute(Connection connection) {
		// TODO: serverExecute
	}
	
	@Override
	public void clientExecute(Connection connection) {
		// TODO: clientExecute
	}
}
