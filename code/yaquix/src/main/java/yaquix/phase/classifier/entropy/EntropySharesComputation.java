package yaquix.phase.classifier.entropy;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Occurs;

import yaquix.Connection;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.AttributeValueTable;
import yaquix.knowledge.MailType;
import yaquix.knowledge.Occurrences;
import yaquix.phase.Knowledge;
import yaquix.phase.Phase;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This class takes the attributes and attribute values and computes
 * a vector of shares such that the point wise sum of this share
 * vector is an approximation to the information gain obtained
 * if splitting the data according to this attribute.
 * 
 * This is achieved by computing a first approximation of ln(x),
 * improving this approximation with the taylor expansion of ln(x)
 * and finally multiplying this with x, all in private. These 
 * steps are implemented as separate sub phases.
 * 
 * @author hk
 *
 */
public class EntropySharesComputation extends SymmetricPhase {
	/**
	 * contains the values of the attributes to compute the
	 * entropies from.
	 */
	private InputKnowledge<AttributeValueTable> localAttributeValues;
	
	/**
	 * contains the attribute list to compute the entropies from.
	 */
	private InputKnowledge<List<Attribute>> concertedAttributes;
	
	/**
	 * contains a place to store the computed shares.
	 */
	private OutputKnowledge<int[]> localShares;
	
	private static final Logger log = LoggerFactory.getLogger(EntropySharesComputation.class);
	/**
	 * Creates a new entropy share computation phase.
	 * @param localAttributeValues the attribute values to compute the entropy of
	 * @param concertedAttributes the attributes to compute the entropy of
	 * @param localShares a place to store the computes entropy shares
	 * @param randomSource the random number generate used
	 */
	public EntropySharesComputation(
			InputKnowledge<AttributeValueTable> localAttributeValues,
			InputKnowledge<List<Attribute>> concertedAttributes,
			OutputKnowledge<int[]> localShares, Random randomSource) {
		this.localAttributeValues = localAttributeValues;
		this.concertedAttributes = concertedAttributes;
		this.localShares = localShares;
	}


	@Override
	protected void execute(Connection connection) throws IOException,
			ClassNotFoundException {
		log.info("Entering Phase");
		List<Attribute> attributes = concertedAttributes.get();
		int attributeCount = attributes.size();
		AttributeValueTable values = localAttributeValues.get();
		int[][] attributeSetShares = new int[attributeCount][3]; // 3 possible attribute values: rare, medium, often
		int[][][] attributeClassSetShares = new int [attributeCount][3][2]; // 2 Classes only: spam, not spam
		
		Knowledge<Integer> localInput = new Knowledge<Integer>();
		Knowledge<Integer> localOutput = new Knowledge<Integer>();
		Phase xlnx = new XLnXProtocol(localInput, localOutput);
		for(int a = 0; a < attributes.size(); a++) {
			EnumMap<Occurrences, AttributeValueTable> mailsByAttributeValue = values.partition(attributes.get(a));
			for (int aj = 0; aj < Occurrences.values().length; aj++) {
				Occurrences attributeValue = Occurrences.values()[aj];
				AttributeValueTable mailsWithValue = mailsByAttributeValue.get(attributeValue);
				localInput.put(mailsWithValue.countAllMails());
				executePhase(connection, xlnx);
				attributeSetShares[a][aj] = localOutput.get();
			
				for (int cj = 0; cj < MailType.values().length; cj++) {
					int amount;
					switch(MailType.values()[cj]) {
					case SPAM:
						amount = mailsWithValue.countSpamMails();
					break;
					
					case NONSPAM:
						amount = mailsWithValue.countNonSpamMails();
					break;
					
					default:
						throw new IllegalStateException("Unexpected mailtype " + MailType.values()[cj]);
					}
					localInput.put(amount);
					attributeClassSetShares[a][aj][cj] = localOutput.get();
				}
			}
			log.info("leaving phase");
		}
		
		int[] shares = new int[attributes.size()];
		for (int a = 0; a < attributes.size(); a++) {
			int share = 0;
			for (int aj = 0; aj < attributeSetShares[a].length; aj++) {
				share += attributeSetShares[a][aj];
				for (int cj = 0; cj < attributeClassSetShares[a][aj].length; cj++) {
					share += attributeClassSetShares[a][aj][cj];
				}
			}
			shares[a] = share;
		}
		localShares.put(shares);
	}

}
