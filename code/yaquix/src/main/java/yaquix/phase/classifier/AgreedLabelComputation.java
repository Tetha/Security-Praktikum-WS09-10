package yaquix.phase.classifier;

import java.util.List;

import yaquix.Connection;
import yaquix.knowledge.MailType;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

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
	 * Constructs a new AgreedLabelComputation
	 * @param localLabels the local email labels
	 * @param concertedUniqueLabel a place to store the common label or null on error.
	 */
	public AgreedLabelComputation(InputKnowledge<List<MailType>> localLabels,
			OutputKnowledge<MailType> concertedUniqueLabel) {
		this.localLabels = localLabels;
		this.concertedUniqueLabel = concertedUniqueLabel;
	}

	@Override
	public void clientExecute(Connection connection) {
		// TODO clientExecute
	}

	@Override
	public void serverExecute(Connection connection) {
		// TODO serverExecute
	}

}
