package yaquix.classifier;

import java.util.EnumMap;

import yaquix.knowledge.Attribute;
import yaquix.knowledge.Mail;
import yaquix.knowledge.MailType;

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
	private EnumMap<MailType, Classifier> subTrees;
	
	/**
	 * Constructs a new decision tree branch.
	 * @param label the label to store
	 * @param subTrees the subtrees to use.
	 */
	public Branch(Attribute label, EnumMap<MailType, Classifier> subTrees) {
		this.label = label;
		this.subTrees = subTrees;
	}

	@Override
	public MailType classify(Mail input) {
		// TODO classify
		return null;
	}

	@Override
	public String formatAsOutput() {
		// TODO formatAsOutput
		return null;
	}

}
