package yaquix.phase.attribute.limit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yaquix.Connection;
import yaquix.knowledge.Mail;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This class computes the local limits. In order to to this, it 
 * computes the average number of occurrences of each word in the
 * list of important words and uses this as the own local limit.
 * @author hk
 *
 */
class LocalLimitComputation extends SymmetricPhase {
	/**
	 * contains the list of words to compute limits for.
	 */
	private InputKnowledge<List<String>> concertedWordlist;
	
	/**
	 * contains the mails to compute the limits from.
	 */
	private InputKnowledge<Mails> localMails;
	
	/**
	 * requires the mapping from words to own local limit.
	 */
	private OutputKnowledge<Map<String, Double>> localLimits;
	
	Logger logger;
	
	/**
	 * This constructs a new LocalLimitComputation.
	 * @param concertedWordlist the list of words to compute the limits for
	 * @param localMails the list of mails to compute the limits from
	 * @param localLimits a place to store the computed limits
	 */
	public LocalLimitComputation(InputKnowledge<List<String>> concertedWordlist,
			InputKnowledge<Mails> localMails,
			OutputKnowledge<Map<String, Double>> localLimits) {
		
		logger.info("localLimitComputation");
		
		this.concertedWordlist = concertedWordlist;
		this.localMails = localMails;
		this.localLimits = localLimits;
	}

	@Override
	protected void execute(Connection connection) {
		
		double tmpValue;
		Map<String, Double>  map;
		
		logger.info("localLimitComputation: starting computation...");
		
		for(String word : concertedWordlist.get()){
		
			tmpValue = 0;
			map = new HashMap<String, Double>();
			
			for(Mail mail : localMails.get().getAllMails()){
			
				tmpValue += mail.countWords()/mail.countWord(word);
				map.put(word, tmpValue);
			}
			
			localLimits.put(map);			
		}
	}
}
