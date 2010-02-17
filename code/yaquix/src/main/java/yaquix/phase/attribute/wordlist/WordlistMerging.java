package yaquix.phase.attribute.wordlist;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import yaquix.Connection;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

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
	
	private Logger logger;
	
	/**
	 * Constructs a new word list merging phase
	 * @param localWordlist the local input of the merging phase
	 * @param concertedWordlist a place to store the common computed word list
	 */
	public WordlistMerging(InputKnowledge<List<String>> localWordlist,
			OutputKnowledge<List<String>> concertedWordlist) {
		logger.info("wordlistMerging");
		this.localWordlist = localWordlist;
		this.concertedWordlist = concertedWordlist;
	}

	@Override
	protected void execute(Connection connection) 
						throws IOException, ClassNotFoundException {
		
		logger.info("wordlistMerging: starting computation...");

		concertedWordlist.put(mergeWordlists(localWordlist.get(),
				 connection.exchangeWordlist(localWordlist.get())));
		
	}

	/**
	 * This joins the two word lists with a simple set union.
	 * @param localWordlist the local word list of this user
	 * @param remoteWordlist the remote word list of the other user
	 * @return the joined word list.
	 */
	private List<String> mergeWordlists(List<String> localWordlist,
									    List<String> remoteWordlist) {
		logger.info("wordlistMerging: merging wordlists...");
		
		Set<String> set = new HashSet<String>();
		
		for(String word : localWordlist)
			set.add(word);
		for(String word :remoteWordlist)
			set.add(word);
		
		return new Vector<String>(set);
	}
}
