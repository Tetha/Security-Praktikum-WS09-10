package yaquix.phase.attribute.wordlist;

import yaquix.Connection;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	private static final int NUMBER_OF_WORDS = 100;

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
	 * @param localWordlist the selected word list
	 */
	public LocalWordlistComputation(InputKnowledge<Mails> localMails,
			OutputKnowledge<List<String>> localWordlist) {
		this.localMails = localMails;
		this.localWordlist = localWordlist;
	}

	@Override
	protected void execute(Connection connection) {
		logger.info("entering phase");

		Mails mails = localMails.get();
		List<String> words = new ArrayList<String>();
		List<Double> spamPercentage = new ArrayList<Double>();
		List<Double> nonSpamPercentage = new ArrayList<Double>();

		Set<String> wordset = mails.getAllWords();

		for(String word : wordset) {
			words.add(word);
			spamPercentage.add((double) mails.countWordInSpamMails(word)*100/mails.countWordInAllEmails(word));
			nonSpamPercentage.add((double) mails.countWordInNonSpamMails(word)*100/mails.countWordInAllEmails(word));
		}

		List<String> chosenWordlist = chooseWords(words, spamPercentage,  nonSpamPercentage);
		logger.debug("chosen wordlist: " + chosenWordlist);
		localWordlist.put(chosenWordlist);

		logger.info("leaving phase");
	}

	private List<String> chooseWords(List<String> words,
									 List<Double> spamPercentage,
									 List<Double> nonSpamPercentage) {
		logger.trace(String.format("chooseWords(words=%s,spamPercentage=%s,nonSpamPercentage=%s), NUM_WORDS = %d",
						words, spamPercentage, nonSpamPercentage, NUMBER_OF_WORDS));

		List<String> goodWords = new ArrayList<String>();

		for(int i = 0; i < Math.min(words.size(), NUMBER_OF_WORDS); i++){
			int bestCandidateIndex = -1;
			double bestDifference = 0;
			for (int candidateIndex = 0; candidateIndex < words.size(); candidateIndex++) {
				if (goodWords.contains(words.get(candidateIndex))) {
					continue;
				}
				double currentDifference = Math.abs(spamPercentage.get(candidateIndex) - nonSpamPercentage.get(candidateIndex));
				if (currentDifference >= bestDifference) {
					bestCandidateIndex = candidateIndex;
					bestDifference = currentDifference;
				}
			}
			assert bestCandidateIndex != -1 : "No canidate found.";
			goodWords.add(words.get(bestCandidateIndex));
		}

		return goodWords;
	}
}
