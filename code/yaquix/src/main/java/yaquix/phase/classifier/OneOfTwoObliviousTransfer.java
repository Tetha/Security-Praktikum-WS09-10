package yaquix.phase.classifier;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * contains a source of random bits.
	 */
	private SecureRandom randomSource;

	/**
	 * This is the server side constructor. It sets the two messages to
	 * have the client select from. Note that if you use this constructor,
	 * you MUST call serverExecute or undefined behavior happens.
	 * @param serverMessages contains the two messages to select one from.
	 */
	public OneOfTwoObliviousTransfer(InputKnowledge<int[]> serverMessages, SecureRandom randomSource) {
		this.serverMessages = serverMessages;
		this.randomSource = randomSource;
		logger = LoggerFactory.getLogger("yaquix.phase.classifier.OneOfTwoObliviousTransfer");

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
			OutputKnowledge<Integer> clientReceivedMessage, SecureRandom randomSource) {
		this.clientBit = clientBit;
		this.clientReceivedMessage = clientReceivedMessage;
		this.randomSource = randomSource;
	}

	/**
	 * gets an RSA Cipher and handles exceptions happening
	 */
	private Cipher getCipher() {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA/ECB/NoPadding");
		} catch(NoSuchPaddingException e) {
			throw new RuntimeException(e); // TODO exception
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e); // TODO exception
		}
		return cipher;
	}

	private static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}


	private static final int byteArrayToInt(byte [] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
	}

	@Override
	public void clientExecute(Connection connection) throws IOException, ClassNotFoundException {
		logger.info("entering phase");
		logger.debug(String.format("Selecting %s", clientBit.get() ? "1" : "0"));
		
//		Key publicKey = connection.receiveKey();
//		int x0 = connection.receiveInteger();
//		int x1 = connection.receiveInteger();
//		int k = randomSource.nextInt();
//		int kPrime;
//
//		Cipher cipher = getCipher();
//		try {
//			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//		} catch (InvalidKeyException e) {
//			throw new RuntimeException(e); // TODO exception
//		}
//
//		try {
//			kPrime = byteArrayToInt(cipher.doFinal(intToByteArray(k)));
//		} catch(BadPaddingException e) {
//			throw new RuntimeException(e); // TODO exception
//		} catch (IllegalBlockSizeException e) {
//			throw new RuntimeException(e); // TODO exception
//		}
//
//		int v;
//		if (clientBit.get()) {
//			v = x1;
//		} else {
//			v = x0;
//		}
//		// if I understand the whole keygen initialization right,
//		// N has 1024 bits now, which means that N is around 2^1000,
//		// which is more than java integers can ever handle, so the modulo
//		// should not matter.
//		v = (v + kPrime);
//
//		connection.sendInteger(v);
//
//		int l0 = connection.receiveInteger();
//		int l1 = connection.receiveInteger();
//
//		int result;
//		if (clientBit.get()) {
//			result = l0 - kPrime;
//		} else {
//			result = l1 - kPrime;
//		}
//		clientReceivedMessage.put(result);
		
		int firstMessage = connection.receiveInteger(); // (1)
		int secondMessage = connection.receiveInteger(); // (2)
		
		int result;
		if (clientBit.get()) {
			result = secondMessage;
		} else {
			result = firstMessage;
		}
		clientReceivedMessage.put(result);
		logger.debug(String.format("received %d", result));
		logger.info("leaving phase");
	}

	@Override
	public void serverExecute(Connection connection) throws IOException {
		logger.info("entering phase");
		logger.info(String.format("serverExecute(%s, %s)", serverMessages.get()[0], serverMessages.get()[1]));
//		KeyPairGenerator keyGenerator;
//		try {
//			keyGenerator = KeyPairGenerator.getInstance("RSA");
//		} catch (NoSuchAlgorithmException exc) {
//			throw new RuntimeException(exc); // TODO exception
//		}
//		keyGenerator.initialize(1024, randomSource);
//		KeyPair keys = keyGenerator.generateKeyPair();
//
//		int m0 = serverMessages.get()[0];
//		int m1 = serverMessages.get()[1];
//		logger.debug(String.format("server messages: %d %d", m0, m1));
//		
//		Key privateKey = keys.getPrivate();
//		Key publicKey = keys.getPublic();
//		int x0 = randomSource.nextInt();
//		int x1 = randomSource.nextInt();
//
//		Cipher cipher = getCipher();
//		try {
//			cipher.init(Cipher.DECRYPT_MODE, privateKey);
//		} catch (InvalidKeyException exc) {
//			throw new RuntimeException(exc); // TODO exception
//		}
//
//		connection.sendKey(publicKey);
//		connection.sendInteger(x0);
//		connection.sendInteger(x1);
//
//		int v = connection.receiveInteger();
//		int k0;
//		int k1;
//		try {
//			byte[] k0Bytes = intToByteArray(v - x0);
//			byte[] k1Bytes = intToByteArray(v - x1);
//
//			k0 = byteArrayToInt(cipher.doFinal(k0Bytes));
//			k1 = byteArrayToInt(cipher.doFinal(k1Bytes));			
//		} catch (IllegalBlockSizeException exc) {
//			throw new RuntimeException(exc);
//		} catch (BadPaddingException exc) {
//			throw new RuntimeException(exc);
//		}
//
//		connection.sendInteger(m0+k0);
//		connection.sendInteger(m1+k1);
		connection.sendInteger(serverMessages.get()[0]); // (1)
		connection.sendInteger(serverMessages.get()[1]); // (2)
		logger.info("leaving phase");
	}

}
