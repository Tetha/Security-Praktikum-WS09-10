package yaquix.phase.classifier;

import java.util.List;

import yaquix.Connection;
import yaquix.knowledge.Attribute;
import yaquix.phase.InputKnowledge;
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
	 * Constructs a new maximum gain computation phase.
	 * @param localEntropyShares the local entropy shares
	 * @param concertedAttributes the attributes we are using
	 * @param concertedBestAttribute where to store the best attribute
	 */
	public MaxGainComputation(InputKnowledge<int[]> localEntropyShares,
			InputKnowledge<List<Attribute>> concertedAttributes,
			OutputKnowledge<Attribute> concertedBestAttribute) {
		this.localEntropyShares = localEntropyShares;
		this.concertedAttributes = concertedAttributes;
		this.concertedBestAttribute = concertedBestAttribute;
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
