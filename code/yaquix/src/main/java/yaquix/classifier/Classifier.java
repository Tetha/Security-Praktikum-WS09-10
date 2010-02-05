package yaquix.classifier;

import yaquix.knowledge.Mail;
import yaquix.knowledge.MailType;

/**
 * This is the interface for all decision tree nodes. 
 * It provides method to classify a mail and to turn the 
 * decision tree into a string which can be parsed by the
 * ClassifierParser.
 * @author hk
 *
 */
public interface Classifier {
	
	/**
	 * Classifies the given mail
	 * @param input the mail to classify
	 * @return Spam or Not Spam, depending on the tree and the mail
	 */
	MailType classify(Mail input);
	
	/**
	 * Turns the Classifier into a string which can be parsed by
	 * the ClassifierParser.
	 * @return a parse-able representation of the Classifier
	 */
	String formatAsOutput();
}
