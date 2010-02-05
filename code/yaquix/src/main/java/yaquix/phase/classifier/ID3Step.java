package yaquix.phase.classifier;

import java.util.List;

import yaquix.Connection;
import yaquix.classifier.Classifier;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.AttributeValueTable;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;

/**
 * This class executes a single step in the ID3 algorithm.
 * 
 * It at first checks if the output is unique using a subphase,
 * then it checks if the remaing attributes are empty, and if this
 * is the case, applies another sub phase to compute the dominating
 * class label in the attribute value set, otherwise, it computes
 * the entropy shares and computes the maximum gain using two
 * sub phases. After it knows the Attribute with the highest information
 * gain, it removes the attribute from the list of attributes,
 * partitions the values according to these tables, applies
 * new instances of the ID3step phase and combines the resulting
 * classificators.
 * @author hk
 *
 */
public class ID3Step extends Phase {
	/**
	 * contains the remaining attributes that can still be used in the
	 * decision tree.
	 */
	private InputKnowledge<List<Attribute>> concertedRemainingAttributes;
	
	/**
	 * contains the vlaues of emails for these attributes.
	 */
	private InputKnowledge<AttributeValueTable> localValues;
	
	/**
	 * requires the resulting classifier to be set.
	 */
	private OutputKnowledge<Classifier> concertedClassifier;
	
	
	/**
	 * constructs a new ID3 step computation phase.
	 * @param concertedRemainingAttributes the remaining attributes
	 * @param localValues the values to consider
	 * @param concertedClassifier a place to store the classifier
	 */
	public ID3Step(
			InputKnowledge<List<Attribute>> concertedRemainingAttributes,
			InputKnowledge<AttributeValueTable> localValues,
			OutputKnowledge<Classifier> concertedClassifier) {
		this.concertedRemainingAttributes = concertedRemainingAttributes;
		this.localValues = localValues;
		this.concertedClassifier = concertedClassifier;
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
