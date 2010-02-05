package yaquix.knowledge;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This table stores the attribute values for each mail. If one
 * looks at an example for the ID3-algorithm, the table with the
 * values ("sunny? yes") is implemented by this class.
 * @author hk
 *
 */
public class AttributeValueTable {
	/**
	 * This contains the attribute value mappings for mails classified
	 * as spam.
	 */
	private List<Map<Attribute, Occurrences>> spamMails;
	
	/**
	 * This contains the attribute value mappings for mails classified
	 * as non spam.
	 */
	private List<Map<Attribute, Occurrences>> nonSpamMails;
	
	/**
	 * constructs a new empty attribute value table
	 */
	public AttributeValueTable() {
		// TODO: constructor
	}
	
	/**
	 * adds a new mail classified as spam to the attribute value table.
	 * @param newMail the new mail to add
	 */
	public void addSpamMail(Map<Attribute, Occurrences> newMail) {
		// TODO: addSpamMail
	}
	
	/**
	 * adds a new mail, classified as non spam to the attribute value table.
	 * @param newMail the new mail to add.
	 */
	public void addNonSpamMail(Map<Attribute, Occurrences> newMail) {
		// TODO: addNonSpamMail
	}
	
	/**
	 * counts the spam mails in the table
	 * @return the number of spam mails in the table.
	 */
	 public int countSpamMails() {
		 // TODO: countSpamMails
		 return -1;
	 }
	 
	 /**
	  * counts the non spam mails in the table
	  * @return the number of non spam mails in the table
	  */
	 public int countNonSpamMails() {
		 // TODO: countNonSpamMails
		 return -1;
	 }
	 
	 /**
	  * This partitions the table into new tables, one for each possible
	  * value of the attribute.
	  * @param partitionAttribute the attribute to partition on
	  * @return the new tables, identified by the attribute value
	  */
	 public EnumMap<Occurrences, AttributeValueTable> partition(Attribute partitionAttribute) {
		 return null;
	 }
}
