package yaquix.phase.attribute.limit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yaquix.Connection;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This class computes the limit pairs for each word in a 
 * list of words. It does so by computing local limits for
 * each word first and merging those local single limits
 * with the local single limits from the other user in 
 * a second step.
 * These substeps are implemented in two other phases,
 * LocalLimitComputationPhase and LimitComputationPhase.
 * @author hk
 *
 */
public class LimitComputation extends SymmetricPhase {
	/**
	 * Contains the list of words to compute attributes for.
	 */
	private InputKnowledge<List<String>> concertedWordlist;
	
	/**
	 * Contains the list of mails to compute attributes for.
	 */
	private InputKnowledge<Mails> localMails;
	
	/**
	 * requires the list of attributes to be put.
	 */
	private OutputKnowledge<List<Attribute>> concertedAttributes;
	
	Logger logger;
	
	/**
	 * This constructs a new limit computation class
	 * @param concertedWordlist the list of words to compute attributes for
	 * @param localMails the list of mails to compute attributes for
	 * @param concertedAttributes a place to store the attributes in
	 */
	public LimitComputation(InputKnowledge<List<String>> concertedWordlist,
			InputKnowledge<Mails> localMails,
			OutputKnowledge<List<Attribute>> concertedAttributes) {
		logger.info("limitComputation");
		this.concertedWordlist = concertedWordlist;
		this.localMails = localMails;
		this.concertedAttributes = concertedAttributes;
	}

	@Override
	protected void execute(Connection connection) throws IOException, ClassNotFoundException {
		logger.info("limitComputation: starting computation...");
		Knowledge<Map<String, Double>>  tmpKnowledge = new Knowledge<Map<String, Double>>();
		
		logger.info("limitComputation: going into localLimitComputation");
		LocalLimitComputation localLimitcomputation = new LocalLimitComputation(concertedWordlist, localMails, tmpKnowledge);
		executePhase(connection, localLimitcomputation);
		
		logger.info("limitComputation: going into LimitMerging");
		LimitMerging limitMerging = new LimitMerging(tmpKnowledge, concertedAttributes);
		executePhase(connection, limitMerging);
	}

}
