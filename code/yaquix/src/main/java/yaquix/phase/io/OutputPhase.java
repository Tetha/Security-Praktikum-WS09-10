package yaquix.phase.io;

import yaquix.Connection;
import yaquix.classifier.Classifier;
import yaquix.phase.InputKnowledge;
import yaquix.phase.SymmetricPhase;

import java.io.IOException;
import java.io.Writer;

/**
 * This class writes the resulting classifier onto the given
 * output stream.
 * @author hk
 *
 */
public class OutputPhase extends SymmetricPhase {
	/**
	 * contains the classifier constructed in an earlier phase
	 */
	private InputKnowledge<Classifier> concertedClassifier;

	/**
	 * contains a place to write the classifier to.
	 */
	private InputKnowledge<Writer> localOutput;


	/**
	 * Constructs a new OutputPhase.
	 * @param concertedClassifier the result classifier
	 * @param localOutput the place to write the classifier to
	 */
	public OutputPhase(InputKnowledge<Classifier> concertedClassifier,
			InputKnowledge<Writer> localOutput) {
		this.concertedClassifier = concertedClassifier;
		this.localOutput = localOutput;
	}


	@Override
	protected void execute(Connection connection) throws IOException {
		logger.info("Entering Output phase");
        Classifier result = concertedClassifier.get();
        assert result != null : "No Classifier computed?";
		localOutput.get().write(String.format("%s", result));
		logger.info("Exiting Output phase");
	}
}
