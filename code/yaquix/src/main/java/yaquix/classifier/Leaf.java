package yaquix.classifier;

import java.util.Formatter;

import yaquix.knowledge.Mail;
import yaquix.knowledge.MailType;

/**
 * This is a leaf of a decision tree. It contains a class
 * label and classifies all mails as this attribute.
 * @author hk
 *
 */
public class Leaf implements Classifier {
	private MailType label;
	
	
	/**
	 * Constructs a new Leaf with the given label
	 * @param label the label the leaf is supposed to output.
	 */
	public Leaf(MailType label) {
		this.label = label;
	}

	@Override
	public MailType classify(Mail input) {
		// TODO classify
		return null;
	}

	@Override
	public void formatTo(Formatter formatter, int flags, int width,
			int precision) {
		// TODO formatTo
		
	}

}
