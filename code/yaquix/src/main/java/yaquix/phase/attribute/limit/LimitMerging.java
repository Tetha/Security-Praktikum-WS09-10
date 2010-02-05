package yaquix.phase.attribute.limit;

import java.util.List;
import java.util.Map;

import yaquix.Connection;
import yaquix.knowledge.Attribute;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This phase takes limits from each user and merges them into 
 * the common attributes. This is done by transmitting the
 * word-limit-mapping to the other user and constructing the
 * attributes locally from the limits.
 * @author hk
 *
 */
class LimitMerging extends SymmetricPhase {
	/**
	 * contains the local own limits to merge.
	 */
	private InputKnowledge<Map<String, Double>> localLimits;
	
	/**
	 * requires the computed attributes.
	 */
	private OutputKnowledge<List<Attribute>> concertedAttributes;
	
	/**
	 * This constructs a new ListMergingPhase
	 * @param localLimits the source of the limits to merge
	 * @param concertedAttributes the destination of the computed attributes
	 */
	public LimitMerging(InputKnowledge<Map<String, Double>> localLimits,
			OutputKnowledge<List<Attribute>> concertedAttributes) {
		this.localLimits = localLimits;
		this.concertedAttributes = concertedAttributes;
	}

	@Override
	protected void execute(Connection connection) {
		// TODO execute
	}

}
