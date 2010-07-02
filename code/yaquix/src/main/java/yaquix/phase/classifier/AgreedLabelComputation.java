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
 * This class checks if the labels of mails are unique.
 *
 * In order to do this, this class creates a circuit which
 * walks through all labels and checks if the labels are unique,
 * encodes the labels for the circuit, executes it with yaos
 * protocol and eventually decodes and returns the output of
 * the circuit.
 *
 * @author hk
 *
 */
public class AgreedLabelComputation extends Phase {
	/**
	 * contains the labels of mails.
	 */
	private InputKnowledge<List<MailType>> localLabels;

	/**
	 * requires the common attribute or null if none exists
	 * to be set.
	 */
	private OutputKnowledge<MailType> concertedUniqueLabel;

	/**
	 * contains the logger instance for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AgreedLabelComputation.class);
	/**
	 * contains the source of random bits.
	 */
	private SecureRandom randomSource;

	/**
	 * Constructs a new AgreedLabelComputation
	 * @param localLabels the local email labels
     * @param concertedUniqueLabel a place to store the common label or null on error.
     */
	public AgreedLabelComputation(InputKnowledge<List<MailType>> localLabels,
                                  OutputKnowledge<MailType> concertedUniqueLabel,
                                  SecureRandom randomSource) {
		this.localLabels = localLabels;
		this.concertedUniqueLabel = concertedUniqueLabel;
		this.randomSource = randomSource;
	}

	@Override
	public void clientExecute(Connection connection) throws ClassNotFoundException, IOException {
		logger.info("entering phase");
		List<MailType> localMailLabels = localLabels.get();
		Knowledge<boolean[]> inputLabels = new Knowledge<boolean[]>();
		LOG.trace(String.format("client local mails: %d", localMailLabels.size()));
        connection.sendInteger(localMailLabels.size());
		inputLabels.put(encodeLabels(localMailLabels));

		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();
		CircuitEvaluation subphase = new CircuitEvaluation(inputLabels, concertedOutput,
														   randomSource);
		subphase.clientExecute(connection);
		MailType result = decodeOutput(concertedOutput);
		concertedUniqueLabel.put(result);
		logger.info("entering phase");
	}

	@Override
	public void serverExecute(Connection connection) throws IOException, ClassNotFoundException {
		logger.info("entering phase");
		List<MailType> localMailLabels = localLabels.get();
        System.err.println(localMailLabels);
        int remoteMailCount = connection.receiveInteger();

		int mailCount = remoteMailCount + localMailLabels.size();
        System.err.println(String.format("total mail count -> %d = %d + %d <- remote + local",
                                         mailCount, remoteMailCount, localMailLabels.size()));
  		Circuit agreedLabel = CircuitBuilder.createAgreeingLabelComputation(mailCount);
		Knowledge<boolean[]> inputLabels = new Knowledge<boolean[]>();
		inputLabels.put(encodeLabels(localMailLabels));

		Knowledge<Circuit> inputCircuit = new Knowledge<Circuit>();
		inputCircuit.put(agreedLabel);

		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();

		CircuitEvaluation subphase = new CircuitEvaluation(inputCircuit, inputLabels, concertedOutput,
														   randomSource);
		subphase.serverExecute(connection);
		MailType result=decodeOutput(concertedOutput);
		concertedUniqueLabel.put(result);
		logger.info("leaving phase");
	}

	/**
	 * @param concertedOutput
	 * @param result
	 * @return
	 */
	private MailType decodeOutput(Knowledge<boolean[]> concertedOutput) {
		MailType result=null;
		boolean[] circuitOutput = concertedOutput.get();

		assert circuitOutput[0] || circuitOutput[1] : "Agreeing Label circuit output broken";
		if (circuitOutput[0] && circuitOutput[1]) {
			//no common label
			result=null;
		} else if (circuitOutput[0] && !circuitOutput[1]) {
			//no spam
			result= MailType.NONSPAM;
		} else if (!circuitOutput[0] && circuitOutput[1]) {
			//spam
			result=MailType.SPAM;
		}
		return result;
	}

	private boolean[] encodeLabels(List<MailType> mails) {
		boolean[] labels = new boolean[2*mails.size()];
		for (int i=0; i<mails.size(); i++) {
			MailType m = mails.get(i);
			int firstBitIndex = 2*i;
			int secondBitIndex = firstBitIndex+1;
			switch (m) {
				case SPAM:
					labels[firstBitIndex]=false;
					labels[secondBitIndex]=true;
					break;
				case NONSPAM:
					labels[firstBitIndex]=false;
					labels[secondBitIndex]=false;

                default: throw new IllegalArgumentException(m.toString());
			}
		}
		return labels;

	}


}
