package yaquix.phase.attribute.wordlist;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This phase takes two local word lists and merges them into
 * a common global word list. This is implemented by exchanging
 * the word lists via the connection and joining them with the
 * set union operation.
 * @author hk
 *
 */
class WordlistMerging extends SymmetricPhase {
	private InputKnowledge<List<String>> localWordlist;
	private OutputKnowledge<List<String>> concertedWordlist;

	/**
	 * Constructs a new word list merging phase
	 * @param localWordlist the local input of the merging phase
	 * @param concertedWordlist a place to store the common computed word list
	 */
	public WordlistMerging(InputKnowledge<List<String>> localWordlist,
			OutputKnowledge<List<String>> concertedWordlist) {
		this.localWordlist = localWordlist;
		this.concertedWordlist = concertedWordlist;
	}

	@Override
	protected void execute(Connection connection)
						throws IOException, ClassNotFoundException {

		logger.info("entering phase");

		List<String> wordlist = localWordlist.get();
		List<String> remoteWordlist = connection.exchangeWordlist(wordlist);
		concertedWordlist.put(mergeWordlists(wordlist, remoteWordlist));
		logger.info("leaving phase");

	}

	/**
	 * This joins the two word lists with a simple set union.
	 * @param localWordlist the local word list of this user
	 * @param remoteWordlist the remote word list of the other user
	 * @return the joined word list.
	 */
	private List<String> mergeWordlists(List<String> localWordlist,
									    List<String> remoteWordlist) {
		Set<String> set = new HashSet<String>();
		set.addAll(localWordlist);
		set.addAll(remoteWordlist);

		return new ArrayList<String>(set);
	}
}
