package yaquix.phase;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import yaquix.Connection;
import yaquix.classifier.Classifier;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.AttributeValueTable;
import yaquix.knowledge.Mails;
import yaquix.phase.attribute.Discretization;
import yaquix.phase.classifier.ID3Step;
import yaquix.phase.io.OutputPhase;
import yaquix.phase.io.ReadFolders;

/**
 * This phase ties all the phases together into a single phase which
 * takes a bunch of files for spam mails and non spam mails and
 * computes a classifier from those in a private and distributed
 * fashion.
 * @author hk
 *
 */
public class ReadLearnWrite extends SymmetricPhase {
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
	private InputKnowledge<Writer> localClassifierOutput;
	
	/**
	 * the randomsource for the id3-algorithm
	 */
	private SecureRandom randomSource;
	
	/**
	 * This constructs a new phase to read files and learn the classifier
	 * @param localSpamFolder the folder containing the spam mail contents
	 * @param localNonSpamFolder the folder containing the non spam mail contents
	 * @param localClassifierOutput a place to store the result classifier
	 */
	public ReadLearnWrite(InputKnowledge<File> localSpamFolder,
			InputKnowledge<File> localNonSpamFolder,
			InputKnowledge<Writer> localClassifierOutput,
			SecureRandom randomSource) {
		super();
		this.localSpamFolder = localSpamFolder;
		this.localNonSpamFolder = localNonSpamFolder;
		this.localClassifierOutput = localClassifierOutput;
		this.randomSource = randomSource;
	}


	@Override
	protected void execute(Connection connection) throws IOException,
			ClassNotFoundException {
		logger.info("Entering ReadLeardWrite phase");
		
		List<Phase> phases = new LinkedList<Phase>();
		Knowledge<Mails> localMails = new Knowledge<Mails>();
		phases.add(new ReadFolders(localSpamFolder, localNonSpamFolder, localMails));
		
		Knowledge<AttributeValueTable> localAttributeValues = new Knowledge<AttributeValueTable>();
		Knowledge<List<Attribute>> concertedAttributes = new Knowledge<List<Attribute>>();
		phases.add(new Discretization(localMails, localAttributeValues, concertedAttributes));
		
		Knowledge<Integer> localMailCount = new Knowledge<Integer>();
		localMailCount.put(localMails.get().getAllMails().size());
		Knowledge<Integer> remoteMailCount = new Knowledge<Integer>();
		phases.add(new ExchangeMailCount(localMailCount, remoteMailCount));
		
		Knowledge<Classifier> concertedClassifier = new Knowledge<Classifier>();
		phases.add(new ID3Step(concertedAttributes, localAttributeValues, remoteMailCount, concertedClassifier, randomSource));

		phases.add(new OutputPhase(concertedClassifier, localClassifierOutput));
		
		for (Phase p : phases) {
			executePhase(connection, p);
		}
		
		logger.info("Leaving ReadLearnWrite");
	}



}
