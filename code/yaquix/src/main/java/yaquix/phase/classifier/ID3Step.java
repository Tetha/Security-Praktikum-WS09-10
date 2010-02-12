package yaquix.phase.classifier;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.slf4j.LoggerFactory;

import yaquix.Connection;
import yaquix.classifier.Branch;
import yaquix.classifier.Classifier;
import yaquix.classifier.Leaf;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.AttributeValueTable;
import yaquix.knowledge.MailType;
import yaquix.knowledge.Occurrences;
import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.Phase;
import yaquix.phase.SymmetricPhase;
import yaquix.phase.classifier.entropy.EntropySharesComputation;

/**
 * This class executes a single step in the ID3 algorithm.
 *
 * It at first checks if the output is unique using a subphase,
 * then it checks if the remaing attributes are empty, and if this
 * is the case, applies another sub phase to compute the dominating
 * class label in the attribute value set, otherwise, it computes
 * the entropy shares and computes the maximum gain using two
 * sub phases. After it knows the Attribute with the highest information
 * gain, it removes the attribute from the list of attributes,
 * partitions the values according to these tables, applies
 * new instances of the ID3step phase and combines the resulting
 * classificators.
 * @author hk
 *
 */
public class ID3Step extends SymmetricPhase {
	/**
	 * contains the remaining attributes that can still be used in the
	 * decision tree.
	 */
	private InputKnowledge<List<Attribute>> concertedRemainingAttributes;

	/**
	 * contains the values of mails for these attributes.
	 */
	private InputKnowledge<AttributeValueTable> localValues;

	private InputKnowledge<Integer> remoteMailCountLimit;
	/**
	 * requires the resulting classifier to be set.
	 */
	private OutputKnowledge<Classifier> concertedClassifier;

	/**
	 * contains a source of randomness.
	 */
	private Random randomSource;

	/**
	 * constructs a new ID3 step computation phase.
	 * @param concertedRemainingAttributes the remaining attributes
	 * @param localValues the values to consider
	 * @param concertedClassifier a place to store the classifier
	 * @param randomSource a source of random bits
	 */
	public ID3Step(
			InputKnowledge<List<Attribute>> concertedRemainingAttributes,
			InputKnowledge<AttributeValueTable> localValues,
			InputKnowledge<Integer> remoteMailCountLimit,
			OutputKnowledge<Classifier> concertedClassifier,
			Random randomSource) {
		this.concertedRemainingAttributes = concertedRemainingAttributes;
		this.localValues = localValues;
		this.remoteMailCountLimit = remoteMailCountLimit;
		this.concertedClassifier = concertedClassifier;

		logger = LoggerFactory.getLogger("yaquix.phase.classifier.ID3Step");
	}

	@Override
	protected void execute(Connection connection) throws IOException {
		logger.info("Entering Phase: ID3 Step");
		Knowledge<List<MailType>> emailLabels =
			new Knowledge<List<MailType>>();
		emailLabels.put(buildLabelList());

		Knowledge<MailType> uniqueLabel = new Knowledge<MailType>();

		Phase uniqueDecider =
			new AgreedLabelComputation(emailLabels, uniqueLabel);
		executePhase(connection, uniqueDecider);
		if (uniqueLabel.get() != null) {
			Classifier result = new Leaf(uniqueLabel.get());
			concertedClassifier.put(result);
			return;
		}


		if (concertedRemainingAttributes.get().isEmpty()) {
			Knowledge<MailType> dominatingLabel = new Knowledge<MailType>();
			Phase dominationDecider =
				new DominatingOutputComputation(emailLabels, remoteMailCountLimit, dominatingLabel);
			executePhase(connection, dominationDecider);
			Classifier result = new Leaf(dominatingLabel.get());
			concertedClassifier.put(result);
			return;
		}


		Knowledge<int[]> entropyShares = new Knowledge<int[]>();
		Phase entropyShareComputation =
			new EntropySharesComputation(localValues,
					concertedRemainingAttributes, entropyShares, randomSource);
		executePhase(connection, entropyShareComputation);

		Knowledge<Attribute> bestAttribute = new Knowledge<Attribute>();
		Phase maxGainPhase = new MaxGainComputation(entropyShares,
									concertedRemainingAttributes,
									bestAttribute);
		executePhase(connection, maxGainPhase);


		Knowledge<List<Attribute>> recursionAttributes = new Knowledge<List<Attribute>>();
		List<Attribute> unusedAttributes = new LinkedList<Attribute>();
		unusedAttributes.addAll(concertedRemainingAttributes.get());
		unusedAttributes.remove(bestAttribute.get());
		recursionAttributes.put(unusedAttributes);

		EnumMap<Occurrences, Classifier> subTrees =
			new EnumMap<Occurrences, Classifier>(Occurrences.class);
		Knowledge<AttributeValueTable> values = new Knowledge<AttributeValueTable>();
		Knowledge<Classifier> subResult = new Knowledge<Classifier>();
		for (Occurrences o : Occurrences.values()) {
			values.put(localValues.get().partition(bestAttribute.get()).get(o));

			Phase recursion = new ID3Step(recursionAttributes, values,
										  remoteMailCountLimit, subResult, randomSource);
			executePhase(connection, recursion);
			subTrees.put(o, subResult.get());
		}

		Classifier result = new Branch(bestAttribute.get(), subTrees);
		concertedClassifier.put(result);
		logger.info("Leaving Phase: ID3 Step");
	}

	/**
	 * This extracts the email labels from the attribute value table
	 * and puts them into a list. For now, they are just put in there
	 * tightly packed from the beginning.
	 * @return a list of email labels
	 */
	private List<MailType> buildLabelList() {
		List<MailType> localLabels = new LinkedList<MailType>();
		for (int i = 0; i < localValues.get().countSpamMails(); i++) {
			localLabels.add(MailType.SPAM);
		}

		for (int i = 0; i < localValues.get().countNonSpamMails(); i++) {
			localLabels.add(MailType.NONSPAM);
		}
		return localLabels;
	}
}
