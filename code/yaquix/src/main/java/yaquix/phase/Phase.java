package yaquix.phase;

import org.slf4j.Logger;

import yaquix.Connection;

/**
 * A phase is the main design idea in this application.
 * 
 * A phase syncronizes the actions on the client and the
 * server, because by construction, the server and the
 * client will execute the same phase at the same time.
 * Thus, it follows that serverExecute and clientExecute
 * can be used to implement the operations required to
 * fulfill the goal of the phase. Note that serverExecute
 * and clientExecute need to use symmetric communications,
 * that is, if one calls sendFoo on the connection, the other
 * must call receiveFoo on the connection, otherwise the
 * application will hang.
 * 
 * @author hk
 *
 */
public abstract class Phase {
	/**
	 * contains a logger to log the phase start and end.
	 */
	protected Logger logger;

	/**
	 * This implements the actions the server side has to perform 
	 * in order to complete the desired goal of the phase.
	 * @param connection the connection used to communicate to
	 * the other part
	 */
	public abstract void serverExecute(Connection connection);
	
	/**
	 * This implements the actions the client has to perform
	 * in order to complete the desired goal of the phase.
	 * @param connection the connection used to communicate to
	 *  the other part
	 */
	public abstract void clientExecute(Connection connection);
}
