package yaquix;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yaquix.circuit.GarbledCircuit;

/**
 * This class handles all the communication between client
 * and server.
 * @author hk
 *
 */
public class Connection {
	/**
	 * contains true if this is the server side of the connection.
	 */
	private Boolean isServer;

	/**
	 * Stuff written here (and flushed afterwards) goes to the
	 * other side.
	 */
	private ObjectInputStream fromRemote;

	/**
	 * Stuff from the other side arrives here.
	 */
	private ObjectOutputStream toRemote;

	/**
	 * the socket providing the connection
	 */
	private Socket connection;
	private ServerSocket serverSocket;
	private Logger logger;

	/**
	 * constructs the server side of the connection.
	 * In order to do this, a socket is accepted and the server socket is
	 * closed afterwards. According to the prototype,
	 * creating the InputStreams first and flushing them and
	 * afterwards creating the OutputStreams is crucial, because
	 * otherwise, a deadlock occurs if the output streams wait
	 * for the stream header.
	 * @param socket the socket to wait for a connection
	 * @throws Exception
	 */
	public Connection(ServerSocket socket) throws IOException {
		logger.info("initializing serverside connection");
		serverSocket = socket;

		logger.info("connection: connecting client");
		connection = socket.accept();
		logger.info("client connected");

		logger.info("setting streams");
		toRemote= new ObjectOutputStream(connection.getOutputStream());
		fromRemote = new ObjectInputStream(connection.getInputStream());
	}


	/**
	 * constructs the client side of the connection.
	 *
	 * @param socket
	 * @throws Exception
	 */
	public Connection(Socket socket) throws IOException {
			logger.info("initializing clientside connection");

			connection = socket;

			logger.info("setting streams");
			toRemote = new ObjectOutputStream(connection.getOutputStream());
			fromRemote= new ObjectInputStream(connection.getInputStream());
	}

	/**
	 * sends an integer to the other side. According to
	 * the prototype, flushing is crucial.
	 * @param localInteger the integer to send
	 * @throws IOException
	 */
	public void sendInteger(int localInteger) throws IOException {
		logger.info("connection: sending integer ");
		toRemote.writeInt(localInteger);
		toRemote.flush();
	}

	/**
	 * receives an integer from the other side.
	 * @return the integer received
	 * @throws IOException
	 */
	public int receiveInteger() throws IOException {
		logger.info("connection: receiving integer");
		return fromRemote.readInt();
	}

	/**
	 * Sends a list of bitstrings to the other side. According
	 * to the prototype, flushing is crucial.
	 * @param localBitstrings the bitstrings to send
	 */
	public void sendListOfBitStrings(List<boolean[]> localBitstrings) throws IOException{
		logger.info("connection: sending list of bitstrings ");
		toRemote.writeObject(localBitstrings);
		toRemote.flush();
	}

	/**
	 * Receives a list of bitstrings from the other side.
	 * @return the bitstrings received.
	 * @throws Exception
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public List<boolean[]> receiveListOfBitStrings() throws IOException, ClassNotFoundException {
		logger.info("connection: receiving list of bitstrings ");

		return (List<boolean[]>) fromRemote.readObject();
	}

	/**
	 * sends a list of integers to the other side. According
	 * to the prototype, flushing is crucial.
	 * @param localIntegers the integers to send
	 * @throws IOException
	 */
	public void sendIntegers(int[] localIntegers) throws IOException {
		logger.info("connection: sending list of integers ");
		toRemote.writeObject(localIntegers);
		toRemote.flush();
	}


	/**
	 * receives a list of integers from the other side
	 * @return the received integers.
	 * @throws Exception
	 * @throws IOException
	 */
	public int[] receiveIntegers() throws IOException, ClassNotFoundException {
		logger.info("connection: receiving list of integers ");
		return (int[]) fromRemote.readObject();
	}

	/**
	 * This exchanges word lists. If we are the server, we need
	 * to send the word lists first before receiving the clients
	 * word list, while the client needs to receive the word list
	 * first and send it's own list afterwards. Flushing is
	 * crucial while sending according to the prototype.
	 *
	 * @param localWordlist the local word list
	 * @return the remote word list
	 * @throws IOException
	 * @throws Exception
	 */
	public List<String> exchangeWordlist(List<String> localWordlist)
										throws IOException, ClassNotFoundException {
		logger.info("connection: exchanging wordlists " + localWordlist.toString());
		if(isServer){
			logger.info("connection: sending wordlist");
			toRemote.writeObject(localWordlist);
			toRemote.flush();
			logger.info("connection: receiving wordlist");
			return (List<String>) fromRemote.readObject();
		} else {
			logger.info("connection: receiving wordlist");
			List<String> receivedWordlist = (List<String>) fromRemote.readObject();
			logger.info("connection: sending wordlist");
			toRemote.writeObject(localWordlist);
			toRemote.flush();

			return receivedWordlist;
		}
	}

	/**
	 * This exchanges limits. If we are the server, we need
	 * to send the limits first before receiving the clients
	 * limits, while the client needs to receive the limits
	 * first and send it's own limits  afterwards. Flushing is
	 * crucial while sending according to the prototype.
	 *
	 * @param localLimits the local limits
	 * @return the remote limits
	 * @throws Exception
	 * @throws IOException
	 */
	public Map<String, Double> exchangeLimits(Map<String, Double> localLimits)
										throws IOException, ClassNotFoundException {
		logger.info("connection: exchanging limits");
		if(isServer){
			logger.info("connection: sending limit");
			toRemote.writeObject(localLimits);
			toRemote.flush();

			logger.info("connection: receiving limit");
			return (Map<String, Double>) fromRemote.readObject();
		} else {
			logger.info("connection: receiving limit");
			Map<String, Double> receivedLimits = (Map<String, Double>) fromRemote.readObject();

			logger.info("connection: sending limit");
			toRemote.writeObject(localLimits);
			toRemote.flush();

			return receivedLimits;
		}
	}

	/**
	 * Sends a garbled circuit to the other side. According to the
	 * prototype, flushing is crucial.
	 * @param localCircuit the circuit to transmit
	 * @throws IOException
	 */
	public void sendGarbledCircuit(GarbledCircuit localCircuit) throws IOException {
		logger.info("connection: sending garbled circuit");

		toRemote.writeObject(localCircuit);
		toRemote.flush();
	}

	/**
	 * receives a sent garbled circuit.
	 * @return the received garbled circuit
	 * @throws Exception
	 * @throws IOException
	 */
	public GarbledCircuit receiveGarbledCircuit() throws IOException, ClassNotFoundException {
		logger.info("connection: receiving garbled circuit");
		return (GarbledCircuit) fromRemote.readObject();
	}

	/**
	 * sends a bitstring to the other side. According
	 * to the prototype, flushing is crucial.
	 * @param localBitstring the local bitstring
	 * @throws Exception
	 */
	public void sendBitstring(boolean[] localBitstring) throws IOException {
		logger.info("connection: sending bitstring");
		toRemote.writeObject(localBitstring);
		toRemote.flush();
	}

	/**
	 * receives a sent bitstring
	 * @return the remote bistring
	 * @throws Exception
	 * @throws IOException
	 */
	public boolean[] receiveBitstring() throws IOException, ClassNotFoundException {
		logger.info("connection: receiving bitstring");
		return (boolean[]) fromRemote.readObject();
	}

	/**
	 * closes the connection.
	 * @throws Exception
	 */
	public void close() throws IOException {
		if(isServer) serverSocket.close();
		connection.close();
	}


	/**
	 * sends a key to the other side. According to the
	 * prototype, flushing is crucial.
	 * @param publicKey
	 * @throws IOException
	 */
	public void sendKey(Key publicKey) throws IOException {
		logger.info("connection: sending key");
		toRemote.writeObject(publicKey);
		toRemote.flush();
	}


	/**
	 * receives the sent key.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Key receiveKey() throws IOException, ClassNotFoundException {
		logger.info("connection: receiving key");
		return (Key) fromRemote.readObject();
	}

	public Integer exchangeInteger(Integer localKey) {
		return null;
	}
}
