package yaquix.phase.classifier;

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

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

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
	private List<Attribute> concertedRemainingAttributes;

	/**
	 * contains the values of mails for these attributes.
	 */
	private AttributeValueTable localValues;

	private Integer remoteMailCountLimit;
	/**
	 * requires the resulting classifier to be set.
	 */
	private OutputKnowledge<Classifier> concertedClassifier;

	/**
	 * contains a source of randomness.
	 */
	private SecureRandom randomSource;
    private Knowledge<List<MailType>> localEmailLabels;
    private Knowledge<MailType> concertedUnanimousLabel;
    private Connection connection;
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
			SecureRandom randomSource) {
		this.concertedRemainingAttributes = concertedRemainingAttributes.get();
		this.localValues = localValues.get();
        this.localEmailLabels = Knowledge.withContent(buildLabelList());
		this.remoteMailCountLimit = remoteMailCountLimit.get();
		this.concertedClassifier = concertedClassifier;
		this.randomSource = randomSource;
        this.concertedUnanimousLabel = new Knowledge<MailType>();
	}

	@Override
	protected void execute(Connection connection)
            throws IOException, ClassNotFoundException {
        logger.info("Entering Phase: ID3 Step");
        this.connection = connection;
        Classifier result;
        checkForUnanimousResult();
        if (labelsAreUnanimous()) {
			result = makeClassifierForUnanimousLabels();
		} else if (allAttributesAreUsed()) {
            result = makeMajorityClassifier();
		} else {
            result = makeRecursiveClassifier();
        }
        concertedClassifier.put(result);
		logger.info("Leaving Phase: ID3 Step");
	}

    private Classifier makeRecursiveClassifier()
            throws IOException, ClassNotFoundException {
    	logger.info("Computing recursive classifier");
        Attribute bestAttribute = selectMostInformativeAttribute();
        return new Branch(bestAttribute, computeSubtreesForPartitionsBy(bestAttribute));
    }

    private EnumMap<Occurrences, Classifier> computeSubtreesForPartitionsBy(Attribute bestAttribute)
            throws IOException, ClassNotFoundException {
        EnumMap<Occurrences, Classifier> result = new EnumMap<Occurrences, Classifier>(Occurrences.class);

        List<Attribute> remainingAttributes = unusedAttributesWithout(bestAttribute);

        for (Occurrences o : Occurrences.values()) {
            AttributeValueTable valuesInPartition = localValues.partitionByAndGet(bestAttribute, o);
            result.put(o, computeClassifierFor(remainingAttributes, valuesInPartition));
        }
        return result;
    }

    private Classifier computeClassifierFor(List<Attribute> remainingAttributes,
                                            AttributeValueTable valuesInPartition)
            throws IOException, ClassNotFoundException {
        int remoteMailsInPartitionCount = connection.exchangeInteger(valuesInPartition.countAllMails());
        Classifier subResult;
        if (oneMailSetIsEmpty(valuesInPartition, remoteMailsInPartitionCount)) {
            subResult = makeMajorityClassifier();
        } else {
            subResult = computeClassifierByRecursion(remainingAttributes, valuesInPartition);
        }
        return subResult;
    }

    private boolean oneMailSetIsEmpty(AttributeValueTable valuesInPartition, int remoteMailsInPartitionCount) {
        return valuesInPartition.countAllMails() == 0
                || remoteMailsInPartitionCount == 0;
    }

    private Classifier computeClassifierByRecursion(List<Attribute> attributesInRecursion,
                                                    AttributeValueTable valuesInPartition)
            throws IOException, ClassNotFoundException {
        Knowledge<Classifier> subResultMediator = new Knowledge<Classifier>();
        Phase recursion = new ID3Step(Knowledge.withContent(attributesInRecursion),
                                      Knowledge.withContent(valuesInPartition),
                                      Knowledge.withContent(remoteMailCountLimit),
                                      subResultMediator,
                                      randomSource);
        executePhase(connection, recursion);
        return subResultMediator.get();
    }

    private List<Attribute> unusedAttributesWithout(Attribute bestAttribute) {
        List<Attribute> unusedAttributes = new LinkedList<Attribute>();
        unusedAttributes.addAll(concertedRemainingAttributes);
        unusedAttributes.remove(bestAttribute);
        return unusedAttributes;
    }

    private Attribute selectMostInformativeAttribute()
            throws IOException, ClassNotFoundException {
        Knowledge<int[]> entropyShares = new Knowledge<int[]>();
        Phase entropyShareComputation =
            new EntropySharesComputation(Knowledge.withContent(localValues),
                                         Knowledge.withContent(concertedRemainingAttributes),
                                         entropyShares,
                                         randomSource);
        executePhase(connection, entropyShareComputation);

        Knowledge<Attribute> bestAttribute = new Knowledge<Attribute>();
        Phase maxGainPhase = new MaxGainComputation(entropyShares,
                                                    Knowledge.withContent(concertedRemainingAttributes),
                                                    bestAttribute,
                                                    randomSource);
        executePhase(connection, maxGainPhase);
        return bestAttribute.get();
    }

    private Classifier makeMajorityClassifier()
            throws IOException, ClassNotFoundException {
        Knowledge<MailType> dominatingLabel = new Knowledge<MailType>();
        Phase dominationDecider =
            new DominatingOutputComputation(localEmailLabels,
                                            Knowledge.withContent(remoteMailCountLimit),
                                            dominatingLabel,
                                            randomSource);
        executePhase(connection, dominationDecider);
        return new Leaf(dominatingLabel.get());
    }

    private boolean allAttributesAreUsed() {
        return concertedRemainingAttributes.isEmpty();
    }

    private Leaf makeClassifierForUnanimousLabels() {
        return new Leaf(concertedUnanimousLabel.get());
    }

    private boolean labelsAreUnanimous() {
        return this.concertedUnanimousLabel.get() != null;
    }

    private void checkForUnanimousResult() throws IOException, ClassNotFoundException {
        Phase uniqueDecider = new AgreedLabelComputation(localEmailLabels,
                concertedUnanimousLabel,
                                                         randomSource);
        executePhase(connection, uniqueDecider);
    }

    /**
	 * This extracts the email labels from the attribute value table
	 * and puts them into a list. For now, they are just put in there
	 * tightly packed from the beginning.
	 * @return a list of email labels
	 */
	private List<MailType> buildLabelList() {
		List<MailType> localLabels = new LinkedList<MailType>();
        localLabels.addAll(Collections.nCopies(localValues.countSpamMails(), MailType.SPAM));
        localLabels.addAll(Collections.nCopies(localValues.countNonSpamMails(), MailType.NONSPAM));
		return localLabels;
	}
}
