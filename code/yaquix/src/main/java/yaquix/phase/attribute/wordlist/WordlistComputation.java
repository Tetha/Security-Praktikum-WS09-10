package yaquix.phase.attribute.wordlist;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import yaquix.Connection;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
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
	 * @param concertedWordlist a place to store the word list
	 */
	public WordlistComputation(InputKnowledge<Mails> localMails,
							OutputKnowledge<List<String>> concertedWordlist) {
		super();
		this.localMails = localMails;
		this.concertedWordlist = concertedWordlist;
	}

	@Override
	protected void execute(Connection connection) 
									throws IOException, ClassNotFoundException {
		
		logger.info("entering phase");
		
		Knowledge<List<String>> tmpKnowledge = new Knowledge<List<String>>();
				
		LocalWordlistComputation localWordlistComputation = 
						new LocalWordlistComputation(localMails, tmpKnowledge);		
		executePhase(connection, localWordlistComputation);
				
		WordlistMerging wordlistMerging = 
							new WordlistMerging(tmpKnowledge, concertedWordlist);
		executePhase(connection, wordlistMerging);
		logger.info("leaving phase");
	}
}
