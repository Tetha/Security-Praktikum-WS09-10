package yaquix.phase.attribute.wordlist;

import java.util.List;

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
	protected void execute(Connection connection) {
		// TODO Auto-generated method stub
	}

	/**
	 * This joins the two word lists with a simple set union.
	 * @param localWordlist the local word list of this user
	 * @param remoteWordlist the remote word list of the other user
	 * @return the joined word list.
	 */
	private List<String> mergeWordlists(List<String> localWordlist,
									    List<String> remoteWordlist) {
		// TODO: mergeWordlists
		return null;
	}
}
