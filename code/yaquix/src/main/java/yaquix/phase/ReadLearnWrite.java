package yaquix.phase;

import java.io.File;
import java.io.Writer;

import yaquix.Connection;
import yaquix.classifier.Classifier;

/**
 * This phase ties all the phases together into a single phase which
 * takes a bunch of files for spam mails and non spam mails and
 * computes a classifier from those in a private and distributed
 * fashion.
 * @author hk
 *
 */
public class ReadLearnWrite extends Phase {
	/**
	 * contains the folder containing the spam mail contents.
	 */
	private InputKnowledge<File> localSpamFolder;
	
	/**
	 * contains the folder containing the non spam mail contents.
	 */
	private InputKnowledge<File> localNonSpamFolder;
	
	/**
	 * requires the learnt classifier to be put here.
	 */
	private OutputKnowledge<Writer> localClassifierOutput;
	
	
	/**
	 * This constructs a new phase to read files and learn the classifier
	 * @param localSpamFolder the folder containing the spam mail contents
	 * @param localNonSpamFolder the folder containing the non spam mail contents
	 * @param localClassifierOutput a place to store the result classifier
	 */
	public ReadLearnWrite(InputKnowledge<File> localSpamFolder,
			InputKnowledge<File> localNonSpamFolder,
			OutputKnowledge<Writer> localClassifierOutput) {
		super();
		this.localSpamFolder = localSpamFolder;
		this.localNonSpamFolder = localNonSpamFolder;
		this.localClassifierOutput = localClassifierOutput;
	}

	@Override
	public void clientExecute(Connection connection) {
		// TODO clientExecute

	}

	@Override
	public void serverExecute(Connection connection) {
		// TODO serverExecute

	}

}
