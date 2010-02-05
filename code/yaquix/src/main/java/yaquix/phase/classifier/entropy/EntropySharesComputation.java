package yaquix.phase.classifier.entropy;

import java.util.List;
import java.util.Random;

import yaquix.Connection;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.AttributeValueTable;
import yaquix.phase.Phase;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;

/**
 * This class takes the attributes and attribute values and computes
 * a vector of shares such that the point wise sum of this share
 * vector is an approximation to the information gain obtained
 * if splitting the data according to this attribute.
 * 
 * This is achieved by computing a first approximation of ln(x),
 * improving this approximation with the taylor expansion of ln(x)
 * and finally multiplying this with x, all in private. These 
 * steps are implemented as separate sub phases.
 * 
 * @author hk
 *
 */
public class EntropySharesComputation extends Phase {
	/**
	 * contains the values of the attributes to compute the
	 * entropies from.
	 */
	private InputKnowledge<AttributeValueTable> localAttributeValues;
	
	/**
	 * contains the attribute list to compute the entropies from.
	 */
	private InputKnowledge<List<Attribute>> concertedAttributes;
	
	/**
	 * contains a place to store the computed shares.
	 */
	private OutputKnowledge<int[]> localShares;
	
	
	/**
	 * Creates a new entropy share computation phase.
	 * @param localAttributeValues the attribute values to compute the entropy of
	 * @param concertedAttributes the attributes to compute the entropy of
	 * @param localShares a place to store the computes entropy shares
	 * @param randomSource the random number generate used
	 */
	public EntropySharesComputation(
			InputKnowledge<AttributeValueTable> localAttributeValues,
			InputKnowledge<List<Attribute>> concertedAttributes,
			OutputKnowledge<int[]> localShares, Random randomSource) {
		this.localAttributeValues = localAttributeValues;
		this.concertedAttributes = concertedAttributes;
		this.localShares = localShares;
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
