package yaquix.phase.attribute.wordlist;

import java.util.List;

import yaquix.Connection;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This computes and concerts a word list from the mails of both 
 * users. This is implemented by first computing word lists 
 * locally on the own mails of each user and later merging those
 * local word lists into a concerted word list.
 * These two substeps are implemented as two sub phases.
 * @author hk
 *
 */
public class WordlistComputation extends SymmetricPhase {
	private InputKnowledge<Mails> localMails;
	private OutputKnowledge<List<String>> concertedWordlist;
	
	
	/**
	 * Creates a new WordListComputation phase with the given
	 * inputs and outputs.
	 * @param localMails the local mail input
	 * @param concertedWordlist a plcae to store the word list
	 */
	public WordlistComputation(InputKnowledge<Mails> localMails,
			OutputKnowledge<List<String>> concertedWordlist) {
		this.localMails = localMails;
		this.concertedWordlist = concertedWordlist;
	}


	@Override
	protected void execute(Connection connection) {
		// TODO execute
	}
}
