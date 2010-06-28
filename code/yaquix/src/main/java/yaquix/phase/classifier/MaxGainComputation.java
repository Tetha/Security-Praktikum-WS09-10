package yaquix.phase.classifier;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yaquix.Connection;
import yaquix.circuit.Circuit;
import yaquix.circuit.CircuitBuilder;
import yaquix.knowledge.Attribute;
import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

/**
 * This phase computes the attribute, which provides the
 * greates information gain if we split the set of data
 * according to this attribute.
 * This computation is based on a sequence of shares such
 * that the sum of these shares is the information gained
 * if we split the data according to the attribute with the
 * same index.
 *
 *  In order to compute this, we construct the maximum gain
 *  circuit and execute it by using yaos protocol.
 * @author hk
 *
 */
public class MaxGainComputation extends Phase {
	/**
	 * This contains the local entropy shares of the user.
	 */
	private InputKnowledge<int[]> localEntropyShares;

	/**
	 * This contains the attributes ordered exactly the same
	 * as the local entropy shares.
	 */
	private InputKnowledge<List<Attribute>> concertedAttributes;

	/**
	 * This requires the best attribute to be stored.
	 */
	private OutputKnowledge<Attribute> concertedBestAttribute;

	/**
	 * contains the logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MaxGainComputation.class);

	/**
	 * Contains the random source of the application.
	 */
	private SecureRandom randomSource;

	private final int shareWidth;

	/**
	 * Constructs a new maximum gain computation phase.
	 * @param localEntropyShares the local entropy shares
	 * @param concertedAttributes the attributes we are using
	 * @param concertedBestAttribute where to store the best attribute
	 */
	public MaxGainComputation(InputKnowledge<int[]> localEntropyShares,
			InputKnowledge<List<Attribute>> concertedAttributes,
			OutputKnowledge<Attribute> concertedBestAttribute,
			SecureRandom randomSource) {
		this.localEntropyShares = localEntropyShares;
		this.concertedAttributes = concertedAttributes;
		this.concertedBestAttribute = concertedBestAttribute;
		this.randomSource = randomSource;
		shareWidth = 32;
	}

	@Override
	public void clientExecute(Connection connection) throws ClassNotFoundException, IOException {
		LOG.info("Starting Phase: Max Gain Computation");
		Knowledge<boolean[]> localInput = new Knowledge<boolean[]>();
		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();
		Phase usedPhase = new CircuitEvaluation(localInput, concertedOutput, randomSource);

		evaluateCircuit(connection, localInput, concertedOutput, usedPhase, false);

		LOG.info("Ending Phase: Max Gain Computation");
	}

	private void evaluateCircuit(Connection connection,
			Knowledge<boolean[]> localInput,
			Knowledge<boolean[]> concertedOutput, Phase usedPhase,
			boolean isServer)
			throws IOException, ClassNotFoundException {
		localInput.put(encodeInput());
		if (isServer) {
			usedPhase.serverExecute(connection); // (1)
		} else {
			usedPhase.clientExecute(connection); // (1)
		}
		List<Attribute> attributes = concertedAttributes.get();
		boolean[] circuitOutput = concertedOutput.get();
		Attribute outputAttribute = decodeOutput(attributes, circuitOutput);
		concertedBestAttribute.put(outputAttribute);
	}

	@Override
	public void serverExecute(Connection connection) throws ClassNotFoundException, IOException {
		LOG.info("Starting Phase: Max Gain Computation");
		
		int[] shares = localEntropyShares.get();

		int shareCount = shares.length;
		Circuit maxGain = CircuitBuilder.createMaximumGainCircuit(shareCount, shareWidth);
		LOG.trace("circuit created");
		Knowledge<Circuit> evaluatedCircuit = new Knowledge<Circuit>();
		evaluatedCircuit.put(maxGain);

		Knowledge<boolean[]> localInput = new Knowledge<boolean[]>();
		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();
		Phase evaluationPhase = new CircuitEvaluation(evaluatedCircuit, localInput, concertedOutput, randomSource);

		evaluateCircuit(connection, localInput, concertedOutput, evaluationPhase, true);
		LOG.info("Ending Phase: Max Gain Computation");
	}

	private Attribute decodeOutput(List<Attribute> attributes, boolean[] output) {
		int attributeIndex = 0;
		
		StringBuilder outputContents = new StringBuilder();
		for (int i = 0; i < output.length; i++) {
			boolean currentBit = output[i];
			if (currentBit) {
				outputContents.append("1");
			} else {
				outputContents.append("0");
			}
		}
		System.err.println(outputContents);
		for (int bitIndex = 0; bitIndex < output.length; bitIndex++) {
			attributeIndex = attributeIndex * 2;
			if (output[bitIndex]) {
				attributeIndex = attributeIndex+1;
			}
		}
		
		Attribute result = attributes.get(attributeIndex);

		return result;
	}

	private boolean[] encodeInput() {
		//int[] localShares = localEntropyShares.get();
		int[] localShares = {0};
		int shareCount = localShares.length;

		boolean[] input = new boolean[shareCount * shareWidth];
		for (int shareIndex = 0; shareIndex < shareCount; shareIndex ++) {
			encodeShare(input, localShares[shareIndex], shareIndex * shareWidth, shareWidth);
		}
		return input;
	}

	private void encodeShare(boolean[] bitDestination, int share, int offset, int bitWidth) {
		for (int bitIndex = 0; bitIndex < bitWidth; bitIndex++) {
			bitDestination[offset+bitIndex] = ((share & (1 << bitIndex)) != 0);
		}
	}

	private int intLog2(int x) {
		int currentX = 1;
		int n = 0;
		while (currentX <= x) {
			currentX *= 2;
			n++;
		}
		return n;
	}



}
