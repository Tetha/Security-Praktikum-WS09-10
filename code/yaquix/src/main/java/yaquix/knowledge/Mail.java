package yaquix.knowledge;

import java.util.List;
import java.util.Set;

/**
 * This class contains the content of an mail and implements
 * several operations on it.
 * @author hk
 *
 */
public class Mail {
	/**
	 * contains the string content of the mail
	 */
	private String content;
	
	public Mail(String content) {
		this.content = content;
	}

	/**
	 * This parses the words contained in content. A word is a sequence of
	 * alphanumeric characters, non-alphanumeric characters between words
	 * are ignored.
	 * @return
	 */
	private List<String> parseWords() {
		// TODO: parseWords;
		return null;
	}
	
	/**
	 * returns the set of words contained in this mail.
	 * @return the set of words in the mail
	 */
	public Set<String> getWords() {
		// TODO: getWords
		return null;
	}
	
	/**
	 * counts the number of words in the mail content. Note that this is
	 * different from the magnitude of getWords(), because getWords
	 * eliminates duplicates, while this counts duplicates.
	 * @return the number of words in the email.
	 */
	public int countWords() {
		// TODO: countWords
		return -1;
	}
	
	/**
	 * This counts the number of occurrences of the word in the email content.
	 * @param word the word to count
	 * @return the number of occurrences
	 */
	public int countWord(String word) {
		// TODO: countWord
		return -1;
	}
}
