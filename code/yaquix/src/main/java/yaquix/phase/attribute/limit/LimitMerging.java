package yaquix.phase.attribute.limit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

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
	protected void execute(Connection connection) 
									throws IOException, ClassNotFoundException {
		logger.info("entering phase");

		List<Attribute> list = new Vector<Attribute>();

		Map<String, Double> remoteMap = 
								connection.exchangeLimits(localLimits.get());

		for(String word : localLimits.get().keySet())
			list.add(new Attribute(word, localLimits.get().get(word),
														remoteMap.get(word)));

		concertedAttributes.put(list);
		logger.info("leaving phase");
	}

}
