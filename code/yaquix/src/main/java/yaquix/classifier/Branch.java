package yaquix.classifier;

import java.util.EnumMap;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

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

	private final String PREFIX = "Decide";
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
		StringBuilder result = new StringBuilder();
		for (String line : toReadableString()) {
			result.append(line);
			result.append("\n");
		}
		return result.toString();
	}

	@Override
	public List<String> toReadableString() {
		List<String> result = new LinkedList<String>();
		result.add(prefixLabelLine());
		result.addAll(subTreeLines());
		result.add(")");
		return result;
	}

	private String prefixLabelLine() {
		return String.format("%s(%s,", PREFIX, label);
	}

	private List<String> subTreeLines() {
		List<String> result = new LinkedList<String>();
		result.addAll(withTrailingComma(indented(linesFor(subTrees.get(Occurrences.RARE)))));
		result.addAll(withTrailingComma(indented(linesFor(subTrees.get(Occurrences.MEDIUM)))));
		result.addAll(indented(linesFor(subTrees.get(Occurrences.OFTEN))));
		return result;
	}

	private List<String> linesFor(Classifier subTree) {
		return subTree.toReadableString();
	}

	private List<String> indented(List<String> lines) {
		List<String> result = new LinkedList<String>();
		String indentation = times(" ", PREFIX.length() + 1); // parenthesis
		for (String line : lines) {
			result.add(indentation + line);
		}
		return result;
	}

	private String times(String multipliedString, int desiredRepetitions) {
		StringBuilder result = new StringBuilder();
		for (int actualRepetitions = 0;
			 actualRepetitions < desiredRepetitions;
			 actualRepetitions++) {
			result.append(multipliedString);
		}
		return result.toString();
	}

	private List<String> withTrailingComma(List<String> input) {
		int lastElementIndex = input.size() -1;
		if (lastElementIndex < 0) return input;
		String lastElement = input.get(lastElementIndex);
		input.set(lastElementIndex, lastElement + ",");
		return input;
	}
}
