package yaquix.phase.classifier;

import java.util.List;

import yaquix.Connection;
import yaquix.circuit.Circuit;
import yaquix.circuit.CircuitBuilder;
import yaquix.knowledge.MailType;
import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
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
	 * contains the remote number of mails
	 */
	private InputKnowledge<Integer> remoteMailCount;
	
	
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
		List<MailType> localMailLabels = localLabels.get();
		Knowledge<boolean[]> inputLabels = new Knowledge<boolean[]>();
		inputLabels.put(encodeLabels(localMailLabels));
		
		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();
		CircuitEvaluation subphase = new CircuitEvaluation(inputLabels, concertedOutput);
		subphase.clientExecute(connection);
		MailType result = decodeOutput(concertedOutput);
		concertedUniqueLabel.put(result);
		
	}

	@Override
	public void serverExecute(Connection connection) {
		logger.info("entering Agreed Label computation..");
		List<MailType> localMailLabels = localLabels.get();
		int mailCount = remoteMailCount.get() + localMailLabels.size();
		Circuit agreedLabel = CircuitBuilder.createAgreeingLabelComputation(mailCount);
		Knowledge<boolean[]> inputLabels = new Knowledge<boolean[]>();
		inputLabels.put(encodeLabels(localMailLabels));
		
		Knowledge<Circuit> inputCircuit = new Knowledge<Circuit>();
		inputCircuit.put(agreedLabel);
		
		Knowledge<boolean[]> concertedOutput = new Knowledge<boolean[]>();
		
		CircuitEvaluation subphase = new CircuitEvaluation(inputCircuit, inputLabels, concertedOutput);
		subphase.serverExecute(connection);
		MailType result=decodeOutput(concertedOutput);
		concertedUniqueLabel.put(result);
		logger.info("leaving Agreed Label computation...");
	}

	/**
	 * @param concertedOutput
	 * @param result
	 * @return
	 */
	private MailType decodeOutput(Knowledge<boolean[]> concertedOutput) {
		MailType result=null;
		boolean[] circuitOutput = concertedOutput.get();

		if (circuitOutput[0] && circuitOutput[1]) {
			//no common label
			result=null;
		} else if (circuitOutput[0] && !circuitOutput[1]) {
			//no spam
			result= MailType.NONSPAM;
		} else if (!circuitOutput[0] && circuitOutput[1]) {
			//spam
			result=MailType.SPAM;
		} else if (!circuitOutput[0] && !circuitOutput[1]) {
			//no mail: shouldn't happen
			throw new IllegalStateException(); 
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
			}
		}
		return labels;
	
	}
		

}
