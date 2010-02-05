package yaquix.phase.attribute.wordlist;

import java.util.List;

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
	
	/**
	 * This creates a new local discretization phase
	 * @param localMails the mails to compute a word list for
	 * @param ownWordlist the selected word list
	 */
	public LocalWordlistComputation(InputKnowledge<Mails> localMails,
			OutputKnowledge<List<String>> localWordlist) {
		this.localMails = localMails;
		this.localWordlist = localWordlist;
	}

	@Override
	protected void execute(Connection connection) {
		// TODO execute

	}
	
	private List<String> chooseWords(List<String> words, 
									 List<Double> spamPercentage, 
									 List<Double> nonSpamPercentage) {
		//TODO: chooseWords
		return null;
	}
}
