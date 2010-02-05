package yaquix;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

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
	
	/**
	 * constructs the server side of the connection.
	 * In order to do this, a socket is accepted and the server socket is
	 * closed afterwards. According to the prototype, 
	 * creating the InputStreams first and flushing them and
	 * afterwards creating the OutputStreams is crucial, because
	 * otherwise, a deadlock occurs if the output streams wait
	 * for the stream header. 
	 * @param socket the socket to wait for a connection
	 */
	public Connection(ServerSocket socket) {
	}
	
	
	/**
	 * constructs the client side of the connection.
	 * 
	 * @param socket
	 */
	public Connection(Socket socket) {
	}
	
	/**
	 * sends an integer to the other side. According to
	 * the prototype, flushing is crucial.
	 * @param localInteger the integer to send
	 */
	public void sendInteger(int localInteger) {
		// TODO sendInt
	}
	
	/**
	 * receives an integer from the other side.
	 * @return the integer received
	 */
	public int receiveInteger() {
		// TODO receiveInt
		return -1;
	}
	
	/**
	 * Sends a list of bitstrings to the other side. According
	 * to the prototype, flushing is crucial.
	 * @param localBitstrings the bitstrings to send
	 */
	public void sendListOfBitStrings(List<boolean[]> localBitstrings) {
		// TODO sendListOfBitStrings
	}
	
	/**
	 * Receives a list of bitstrings from the other side.
	 * @return the bitstrings received.
	 */
	public List<boolean[]> receiveListOfBitStrings() {
		// TODO receiveListOfBitStrings
		return null;
	}
	
	/**
	 * sends a list of integers to the other side. According
	 * to the prototype, flushing is crucial.
	 * @param localIntegers the integers to send
	 */
	public void sendIntegers(int[] localIntegers) {
		// TODO sendInts
	}
	
	/**
	 * receives a list of integers from the other side
	 * @return the received integers.
	 */
	public int[] receiveIntegers() {
		// TODO receiveIntegers
		return null;
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
	 */
	public List<String> exchangeWordlist(List<String> localWordlist) {
		// TODO exchangeWordlist
		return null;
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
	 */	
	public Map<String, Double> exchangeLimits(Map<String, Double> localLimits) {
		// TODO exchangeLimits
		return null;
	}
	
	/**
	 * Sends a garbled circuit to the other side. According to the
	 * prototype, flushing is crucial. 
	 * @param localCircuit the circuit to transmit
	 */
	public void sendGarbledCircuit(GarbledCircuit localCircuit) {
		// TODO sendGarbledCircuit
	}
	
	/**
	 * receives a sent garbled circuit.
	 * @return the received garbled circuit
	 */
	public GarbledCircuit receiveGarbledCircuit() {
		// TODO receiveGarbledCircuit
		return null;
	}
	
	/**
	 * sends a bitstring to the other side. According
	 * to the prototype, flushing is crucial.
	 * @param localBitstring the local bitstring
	 */
	public void sendBitstring(boolean[] localBitstring) {
		//TODO sendBitstring
	}
	
	/**
	 * receives a sent bitstring
	 * @return the remote bistring
	 */
	public boolean[] receiveBitstring() {
		//TODO receiveBitstring
		return null;
	}
	
	/**
	 * closes the connection.
	 */
	public void close() {
	}
}
