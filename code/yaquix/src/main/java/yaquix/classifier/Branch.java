package yaquix.classifier;

import java.util.EnumMap;
import java.util.Formatter;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Occurs;

import yaquix.knowledge.Attribute;
import yaquix.knowledge.Mail;
import yaquix.knowledge.MailType;
import yaquix.knowledge.Occurrences;

/**
 * Implements a branch in a decision tree.
 * This branch contains an attribute and three further decision trees
 * which correspond to the classification results of an email.
 * @author hk
 *
 */
public class Branch implements Classifier {
	/**
	 * contains the attribute to decide the further classfication by.
	 */
	private Attribute label;
	
	/**
	 * contains the subtrees to deserialize recursively in.
	 */
	private EnumMap<Occurrences, Classifier> subTrees;
	
	/**
	 * Constructs a new decision tree branch.
	 * @param label the label to store
	 * @param subTrees the subtrees to use.
	 */
	public Branch(Attribute label, EnumMap<Occurrences, Classifier> subTrees) {
		this.label = label;
		this.subTrees = subTrees;
	}

	@Override
	public MailType classify(Mail input) {
		Occurrences occ = this.label.classify(input);
		return subTrees.get(occ).classify(input);
	}

	@Override
	public void formatTo(Formatter formatter, int flags, int width,
			int precision) {
		formatter.format(this.toString());
	}
	
	public String toString() {
		return String.format("Decide(%s,%s,%s,%s)",
				label, subTrees.get(Occurrences.RARE),
				subTrees.get(Occurrences.MEDIUM),
				subTrees.get(Occurrences.OFTEN));
	}
}
