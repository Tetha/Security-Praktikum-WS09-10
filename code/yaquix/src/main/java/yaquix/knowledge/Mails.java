package yaquix.knowledge;

import java.util.List;
import java.util.Set;

/**
 * This class is responsible for storing the mails and storing
 * for each mail if the mail is spam or if the mail is not spam.
 * @author hk
 *
 */
public class Mails {
	/**
	 * contains all spam mails.
	 * This is a list in order to implement a multiset like behavior.
	 */
	private List<Mail> spamMails;
	
	/**
	 * contains all non spam mails.
	 */
	private List<Mail> nonSpamMails;
	
	/**
	 * This creates a new empty set of mails.
	 * This is a list in order to implement a multiset like behavior.
	 */
	public Mails() {
		// TODO Constructor
	}
	
	/**
	 * This returns the list of all spam mails. 
	 * This is an independent view of the spam mails so the copy
	 * can be modified at will.
	 * @return an independent view on the spam mails
	 */
	public List<Mail> getSpamMails() {
		// TODO: getSpamMails
		return null;
	}
	
	/**
	 * This returns the list of all non spam mails.
	 * This is an independent view of the non spam mails so
	 * the copy can be modified at will.
	 * @return an independent view on the non spam mails
	 */
	public List<Mail> getNonSpamMails() {
		// TODO: getNonSpamMails
		return null;
	}
	
	/**
	 * This returns the set of all mails.
	 * This is an independent view of the mails, so the
	 * copy can be modified at will.
	 * @return an independent view on all mails
	 */
	public List<Mail> getAllMails() {
		// TODO: getAllMails
		return null;
	}
	
	/**
	 * This returns the set of unique words in all mails.
	 * @return the set of all unique words
	 */
	public Set<String> getAllWords() {
		// TODO: getAllWords
		return null;
	}
		
	/**
	 * counts the occurrences of the given word in the contents of the spam 
	 * mails.
	 * @param word the word to count
	 * @return the number of occurences of the word in spam mails
	 */
	public int countWordInSpamMails(String word) {
		// TODO: countWordInSpamMails
		return -1;
	}
	
	/**
	 * counts the occurrences of the given word in the contents of 
	 * non spam mails.
	 * @param word the word to count
	 * @return the number of occurrences of the word in non spam mails
	 */
	public int countWordInNonSpamMails(String word) {
		// TODO: countWordInNonSpamMails
		return -1;
	}
	
	/**
	 * Counts the occurrences of the word in all words.
	 * @param word the word to count
	 * @return the number of occurrences of the word in all 
	 * mails.
	 */
	public int countWordInAllEmails(String word) {
		// TODO: countWordInAllEmails
		return -1;
	}
	
	/**
	 * counts the number of words in spam mails.
	 * @return the number of words in spam mails.
	 */
	public int countSpamWords() {
		// TODO countSpamWords
		return -1;
	}
	
	/**
	 * counts the number of words in non spam mails.
	 * @return the number of words in non spam mails
	 */
	public int countNonSpamWords() {
		// TODO countNonSpamWords
		return -1;
	}
	
	/**
	 * counts the number of words in all e-mails.
	 * Note that this is different from the size of the result of getWords, as
	 * this counts duplicates, while the set returned by getWords removes 
	 * duplicates.
	 * @return the number of words in all emails
	 */
	public int countAllWords() {
		// TODO countAllWords
		return -1;
	}
	
	/**
	 * Adds a new spam mail to the set of all mails
	 * @param newMail the new mail to store
	 */
	public void addSpamMail(Mail newMail) {
		// TODO: addSpamMail
	}
	
	/**
	 * adds a new non spam mail to the set of all mails
	 * @param newMail the new mail to store
	 */
	public void addNonSpamMail(Mail newMail) {
		// TODO addNonSpamMail
	}
}
