package yaquix.knowledge;

import java.util.Formattable;
import java.util.Formatter;

/**
 * The attribute contains a word and two limits which classify
 * the relative occurences of a word in an email as rare,
 * middle and often. 
 * @author hk
 *
 */
public class Attribute implements Formattable {
	/**
	 * the word the attribute cares about.
	 */
	private String word;
	
	/**
	 * the lower threshold
	 */
	private double low;
	
	/**
	 * the higher threshold
	 */
	private double high;
	
	
	/**
	 * Constructs an attribute from local and remote limit.
	 * Note that local and remote limit are not ordererd (in fact,
	 * if low and high are different, they must be in wrong order
	 * on the client or the server), so it is necessary to
	 * sort them.
	 * @param word the word to care about
	 * @param localLimit the local limit 
	 * @param remoteLimit the remote limit
	 */
	public Attribute(String word, double localLimit, double remoteLimit) {
		this.word = word;
		if (localLimit<remoteLimit) {
			this.low=localLimit;
			this.high=remoteLimit;
		} else {
			this.low=remoteLimit;
			this.high=localLimit;
		}
	}


	/**
	 * classifies a single mail.
	 * @param mail the mail to classify
	 * @return the number of occurences in the mail
	 */
	public Occurrences classify(Mail mail) {
		double seen=mail.countWord(word);
		seen=seen/mail.countWords();
		Occurrences occurrence;
		if (seen<this.low) {
			occurrence = Occurrences.RARE;
		} else if (seen<this.high) {
			//must be between low and high
			occurrence=Occurrences.MEDIUM;
		} else {
			//must be above high
			occurrence=Occurrences.OFTEN;
		}
		return occurrence;
	}
	
	@Override
	public void formatTo(Formatter arg0, int arg1, int arg2, int arg3) {
		// TODO formatTo
	}
	
	/**
	 * returns a description of the attribute as a string
	 * @return the describing string
	 * 
	 */
	public String toString() {
		return String.format("(%s,%f,%f)",word,low,high);
	}
}
