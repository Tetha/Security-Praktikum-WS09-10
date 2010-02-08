package yaquix.classifier;

import java.util.Formattable;

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
public interface Classifier extends Formattable {
	
	/**
	 * Classifies the given mail
	 * @param input the mail to classify
	 * @return Spam or Not Spam, depending on the tree and the mail
	 */
	MailType classify(Mail input);
}
