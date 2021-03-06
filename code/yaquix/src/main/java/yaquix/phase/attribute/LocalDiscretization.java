package yaquix.phase.attribute;

import yaquix.Connection;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.AttributeValueTable;
import yaquix.knowledge.Mail;
import yaquix.knowledge.Mails;
import yaquix.knowledge.Occurrences;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This phase turns the set of emails into an attribute value table,
 * given a list of attributes. this is done by fetching every mail in
 * the set of mails, handing it to the various attributes and storing
 * the classification result of the email in a mapping. Once we did
 * this for all attributes, the mapping is stored in the result
 * attribute value table.
 * @author hk
 *
 */
class LocalDiscretization extends SymmetricPhase {
	/**
	 * This contains the list of attributes to use during discretization.
	 */
	private InputKnowledge<List<Attribute>> commonAttributes;
	/**
	 * This contains the set of emails to discretize.
	 */
	private InputKnowledge<Mails> localEmails;

	/**
	 * This requires the resulting attribute value table.
	 */
	private OutputKnowledge<AttributeValueTable> localAttributeValues;

	/**
	 * Constructs a new Local Discretization phase.
	 * @param commonAttributes the attributes to put into the table
	 * @param localEmails the mails to examine
	 * @param localAttributeValues a place to store the result table
	 */
	public LocalDiscretization(InputKnowledge<List<Attribute>> commonAttributes,
			InputKnowledge<Mails> localEmails,
			OutputKnowledge<AttributeValueTable> localAttributeValues) {
		this.commonAttributes = commonAttributes;
		this.localEmails = localEmails;
		this.localAttributeValues = localAttributeValues;
	}

	@Override
	protected void execute(Connection connection) {
		logger.info("entering phase");
		List<Attribute> attributeList = new ArrayList<Attribute>();	
		AttributeValueTable valueTable = new AttributeValueTable();
		Map<Attribute,Occurrences> map;


		logger.info("localDiscretization: discretizing SpamMails...");
		List<Mail> mailList = localEmails.get().getSpamMails();

		for(Mail mail: mailList){
			map = new HashMap<Attribute,Occurrences>();
			attributeList = commonAttributes.get();

			for(Attribute attribute : attributeList){
				map.put(attribute, attribute.classify(mail));
			}
			valueTable.addSpamMail(map);
		}

		logger.info("localDiscretization: discretizing nonSpamMails...");
		mailList = localEmails.get().getNonSpamMails();

		for(Mail mail: mailList){
			map = new HashMap<Attribute,Occurrences>();

			for(Attribute attribute : attributeList){
				map.put(attribute, attribute.classify(mail));
			}
			valueTable.addNonSpamMail(map);
		}

		localAttributeValues.put(valueTable);
		logger.info("leaving phase");
	}
}
