package yaquix.phase.classifier;

import java.util.List;

import yaquix.Connection;
import yaquix.knowledge.MailType;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

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
	 * requires the dominating label of the local and remote
	 * labels to be stored.
	 */
	private OutputKnowledge<MailType> concertedDominatingLabel;
	
	
	/**
	 * Creates a new DominatingOutputPhase.
	 * @param localLabels the local mail labels.
	 * @param concertedDominatingLabel a place to store the dominating class 
	 * label
	 */
	public DominatingOutputComputation(
			InputKnowledge<List<MailType>> localLabels,
			OutputKnowledge<MailType> concertedDominatingLabel) {
		this.localLabels = localLabels;
		this.concertedDominatingLabel = concertedDominatingLabel;
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
