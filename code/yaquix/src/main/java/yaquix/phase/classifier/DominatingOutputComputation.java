package yaquix.phase.classifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yaquix.Connection;
import yaquix.circuit.Circuit;
import yaquix.circuit.CircuitBuilder;
import yaquix.knowledge.MailType;
import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

/**
 * This class computes the class label that occurs most often
 * in the local and remote labels.
 *
 * This is done by using the dominating labels circuit. This
 * class is responsible for encoding the input for the circuit,
 * creating the circuit, evaluating it with yaos phase and
 * decoding the overall output of the circuit.
 * @author hk
 *
 */
public class DominatingOutputComputation extends Phase {
	/**
	 * contains the list of local mail labels.
	 */
	private InputKnowledge<List<MailType>> localLabels;


	/**
	 * contains the maximum number of mails bob will ever
	 * provide.
	 */
	private InputKnowledge<Integer> remoteMailCountLimit;

	/**
	 * requires the dominating label of the local and remote
	 * labels to be stored.
	 */
	private OutputKnowledge<MailType> concertedDominatingLabel;

	/**
	 * contains a place to log stuff.
	 */
	private Logger logger;

	/**
	 * contains the random source.
	 */
	private SecureRandom randomSource;

	/**
	 * Creates a new DominatingOutputPhase.
	 * @param localLabels the local mail labels.
	 * @param concertedDominatingLabel a place to store the dominating class
	 * label
	 */
	public DominatingOutputComputation(
			InputKnowledge<List<MailType>> localLabels,
			InputKnowledge<Integer> remoteMailCountLimit,
			OutputKnowledge<MailType> concertedDominatingLabel,
            SecureRandom randomSource) {
		this.localLabels = localLabels;
		this.remoteMailCountLimit = remoteMailCountLimit;
		this.concertedDominatingLabel = concertedDominatingLabel;
		logger = LoggerFactory.getLogger(DominatingOutputComputation.class);
        this.randomSource = randomSource;
	}

	private int intLog2(int x) {
		int currentX = 1;
		int n = 0;
		while (currentX < x) {
			currentX *= 2;
			n++;
		}
		return n;
	}

	@Override
	public void clientExecute(Connection connection) throws IOException, ClassNotFoundException {
		logger.info("Entering Phase: Dominating Output Computation");
        int remoteMailCount = connection.exchangeInteger(localLabels.get().size());
        int maxLabelBound = Math.max(remoteMailCount, localLabels.get().size());
		Knowledge<boolean[]> localInput = new Knowledge<boolean[]>();
		localInput.put(encodeLabels(localLabels.get(),
					                maxLabelBound,
                                    0));

		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();
		CircuitEvaluation subPhase = new CircuitEvaluation(localInput, concertedOutput, randomSource);
		subPhase.clientExecute(connection);

		decodeOutput(concertedOutput);
		logger.info("Leaving Phase: Dominating Output Computation");
	}

	@Override
	public void serverExecute(Connection connection) throws IOException, ClassNotFoundException {
		logger.info("Entering Phase: Dominating Output Computation");
		List<MailType> labels = localLabels.get();
        int remoteMailCount = connection.exchangeInteger(labels.size());
		int maximumMailBound = Math.max(remoteMailCount, labels.size());
        logger.info(String.format("maximum mail bound: %d", maximumMailBound));
        Circuit dominationCircuit =
			CircuitBuilder.createDominatingOutputCircuit(maximumMailBound);
        logger.info(String.format("inputs of circuit: %d", dominationCircuit.getInputCount()));
		Knowledge<Circuit> circuitInput = Knowledge.withContent(dominationCircuit);

		Knowledge<boolean[]> localInputKnowledge = new Knowledge<boolean[]>();
		localInputKnowledge.put(encodeLabels(labels, maximumMailBound, 0));

		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();

		CircuitEvaluation subPhase = new CircuitEvaluation(circuitInput,
										localInputKnowledge, concertedOutput,
										randomSource);
		subPhase.serverExecute(connection);

		decodeOutput(concertedOutput);
		logger.info("Leaving Phase: Dominating Output Computation");
	}

	/**
	 * decodes the output and puts the computed label into the output
	 * knowledge
	 * @param concertedOutput the output to decode.
	 */
	private void decodeOutput(Knowledge<boolean[]> concertedOutput) {
		if (concertedOutput.get()[0]) {
			concertedDominatingLabel.put(MailType.SPAM);
		} else {
			concertedDominatingLabel.put(MailType.NONSPAM);
		}
	}

	/**
	 * Encodes the number of spam or nonspam labels into a boolean
	 * array according to the input specification of the circuit.
	 * @param labels the labels to encode
	 */
	private boolean[] encodeLabels(List<MailType> labels, int maxLabelBound, int offset) {
		boolean[] localInput;
		int spamCount = 0;
		int nonSpamCount = 0;

		localInput = new boolean[intLog2(maxLabelBound)*2];
        System.err.println("Max label bound: " + maxLabelBound);
        System.err.println(intLog2(maxLabelBound));
        System.err.println(String.format("localInputCount.length = %d", localInput.length));
        System.err.println(String.format("offset=%d", offset));
		for (MailType m : labels) {
			switch (m) {
				case SPAM: spamCount++; break;
				case NONSPAM: nonSpamCount++; break;
                default: throw new IllegalArgumentException(m.toString());
			}
		}

		for (int i = 0; i < intLog2(maxLabelBound); i++) {
			if ((spamCount & (1<<i)) > 0) {
				localInput[intLog2(maxLabelBound)-i-1 + offset] = true;
			} else {
				localInput[intLog2(maxLabelBound)-i-1 + offset] = false;
			}
		}

		for (int i = 0; i < intLog2(maxLabelBound); i++) {
			if ((nonSpamCount & (1<<i)) > 0) {
				localInput[2*intLog2(maxLabelBound)-i-1 + offset] = true;
			} else {
				localInput[2*intLog2(maxLabelBound)-i-1 + offset] = false;
			}
		}
		return localInput;
	}

}
