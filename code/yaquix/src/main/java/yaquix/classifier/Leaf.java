package yaquix.classifier;

import yaquix.knowledge.Mail;
import yaquix.knowledge.MailType;

import java.util.Collections;
import java.util.Formatter;
import java.util.List;

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
		return label;
	}

	@Override
	public void formatTo(Formatter formatter, int flags, int width,
			int precision) {
		formatter.format(this.toString());
	}

	public String toString() {
		return String.format("Output(%s)", formatLabel(this.label));
	}

	@Override
	public List<String> toReadableString() {
		return Collections.singletonList(toString());
	}

    private String formatLabel(MailType label) {
        switch(label) {
            case SPAM: return "Spam";
            case NONSPAM: return  "Not Spam";
            default: throw new IllegalArgumentException(label.toString());
        }
    }
}
