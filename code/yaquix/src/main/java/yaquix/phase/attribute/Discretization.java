package yaquix.phase.attribute;

import java.util.List;

import yaquix.Connection;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.AttributeValueTable;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This phase computes a list of attributes and an attribute value table
 * from the set of own local emails. These attributes are the same
 * for both users.
 * 
 * This is implemented by computing the concerted word list first,
 * then using this to compute limits for discretizing the relative
 * occurences of each word in the wordlist in the content of a mail
 * and finally using these attributes in order to discretize each
 * input mail into the final attribute value table.
 * 
 * Each of these steps is implemented in a sub phase.
 * @author hk
 *
 */
public class Discretization extends SymmetricPhase {
	private InputKnowledge<Mails> localMails;
	private OutputKnowledge<AttributeValueTable> localAttributeValues;
	private OutputKnowledge<List<Attribute>> concertedAttributes;
	
	
	/**
	 * This constructs a new Discretization phase. 
	 * @param localMails the local mails to build attributes from and 
	 * discretize
	 * @param localAttributeValues a place to store the computed 
	 * attribute value table
	 * @param concertedAttributes a place to store the computed attributes
	 */
	public Discretization(InputKnowledge<Mails> localMails,
			OutputKnowledge<AttributeValueTable> localAttributeValues,
			OutputKnowledge<List<Attribute>> concertedAttributes) {
		this.localMails = localMails;
		this.localAttributeValues = localAttributeValues;
		this.concertedAttributes = concertedAttributes;
	}


	@Override
	protected void execute(Connection connection) {
		// TODO execute
		
	}

}
