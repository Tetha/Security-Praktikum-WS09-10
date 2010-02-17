package yaquix.phase.attribute.wordlist;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import yaquix.Connection;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This computes a local wordlist for each user. 
 * In order to do this, we compute the relative number of occurences
 * of a word in the spam words and the non spam words and select
 * those words that have a high difference between these two values,
 * because we assume that such words result in a good classification.
 * @author hk
 *
 */
class LocalWordlistComputation extends SymmetricPhase {
	/**
	 * contains the maximum number of words to select for the global word 
	 * list.
	 */
	private final int NUMBER_OF_WORDS = 100;
	
	/**
	 * This contains the mails to create a wordlist for.
	 */
	private InputKnowledge<Mails> localMails;
	
	/**
	 * This requires the computed local wordlist to be stored.
	 */
	private OutputKnowledge<List<String>> localWordlist;
	
	private Logger logger;
	
	/**
	 * This creates a new local discretization phase
	 * @param localMails the mails to compute a word list for
	 * @param localWordlist the selected word list
	 */
	public LocalWordlistComputation(InputKnowledge<Mails> localMails,
			OutputKnowledge<List<String>> localWordlist) {
		logger.info("localWordlistcomputation");
		
		this.localMails = localMails;
		this.localWordlist = localWordlist;
	}

	@Override
	protected void execute(Connection connection) {
		logger.info("localWordlistcomputation: starting computation...");
		
		Mails mails = localMails.get();
		List<String> words = new Vector<String>();
		List<Double> spamPercentage = new Vector<Double>();
		List<Double> nonSpamPercentage = new Vector<Double>();
		
		logger.info("localWordlistComputation: computing words in our emails" +
							" and corresponding spam-/nonSpam-percentages...");
		
		Set<String> wordset = mails.getAllWords();
		
		for(String word : wordset){
			words.add(word);
			spamPercentage.add((double) mails.countWordInSpamMails(word)*100/mails.countWordInAllEmails(word));
			nonSpamPercentage.add((double) mails.countWordInNonSpamMails(word)*100/mails.countWordInAllEmails(word));
		}
		
		localWordlist.put(chooseWords(words, spamPercentage,  nonSpamPercentage));
	}
	
	private List<String> chooseWords(List<String> words, 
									 List<Double> spamPercentage, 
									 List<Double> nonSpamPercentage) {
		
		logger.info("localWordlistComputation: choosing the best  words (max: " + NUMBER_OF_WORDS  + "...");
		
		double key;
		List<String> goodWords = new Vector<String>();
		SortedMap<Double, String> map = new TreeMap<Double, String>();
		
		for(int i = 0; i < words.size(); i++){			
			map.put((Math.abs(spamPercentage.get(i) - nonSpamPercentage.get(i))), words.get(i));
		}
		
		for(int i = 0; i < NUMBER_OF_WORDS; i++){
			key = map.lastKey();
			goodWords.add(map.get(key));
			map.remove(key);
		}
		
		return goodWords;
	}
}
