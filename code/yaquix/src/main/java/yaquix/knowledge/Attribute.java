package yaquix.knowledge;

/**
 * The attribute contains a word and two limits which classify
 * the relative occurences of a word in an email as rare,
 * middle and often. 
 * @author hk
 *
 */
public class Attribute {
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
		// TODO: constructor
	}


	/**
	 * classifies a single mail.
	 * @param mail the mail to classify
	 * @return the number of occurences in the mail
	 */
	public MailType classify(Mail mail) {
		// TODO classify
		return null;
	}
	
	/**
	 * Formats the output in a way the classifier parser can parse.
	 * @return a representation of the attribute to parse
	 */
	public String formatAsOutput() {
		// TODO formatAsOutput
		return null;
	}
}
