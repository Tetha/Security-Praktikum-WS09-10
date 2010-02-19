package yaquix.knowledge;

import java.util.HashSet;
import java.util.LinkedList;
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
		spamMails = new LinkedList<Mail>();
		nonSpamMails = new LinkedList<Mail>();
	}
	
	/**
	 * This returns the list of all spam mails. 
	 * This is an independent view of the spam mails so the copy
	 * can be modified at will.
	 * @return an independent view on the spam mails
	 */
	public List<Mail> getSpamMails() {
		return spamMails;
	}
	
	/**
	 * This returns the list of all non spam mails.
	 * This is an independent view of the non spam mails so
	 * the copy can be modified at will.
	 * @return an independent view on the non spam mails
	 */
	public List<Mail> getNonSpamMails() {
		return nonSpamMails;
	}
	
	/**
	 * This returns the set of all mails.
	 * This is an independent view of the mails, so the
	 * copy can be modified at will.
	 * @return an independent view on all mails
	 */
	public List<Mail> getAllMails() {
		return spamMails;
	}
	
	/**
	 * This returns the set of unique words in all mails.
	 * @return the set of all unique words
	 */
	public Set<String> getAllWords() {
		Set<String> allWords = new HashSet<String>();
		for (Mail mail : nonSpamMails) {
			allWords.addAll(mail.getWords());
		}
		
		for (Mail mail : spamMails) {
			allWords.addAll(mail.getWords());
		}
		return allWords;
	}
		
	/**
	 * counts the occurrences of the given word in the contents of the spam 
	 * mails.
	 * @param word the word to count
	 * @return the number of occurences of the word in spam mails
	 */
	public int countWordInSpamMails(String word) {
		int count = 0;
		for (Mail mail : spamMails) {
			count += mail.countWord(word);
		}
		return count;
	}
	
	/**
	 * counts the occurrences of the given word in the contents of 
	 * non spam mails.
	 * @param word the word to count
	 * @return the number of occurrences of the word in non spam mails
	 */
	public int countWordInNonSpamMails(String word) {
		int count = 0;
		for (Mail mail : nonSpamMails) {
			count += mail.countWord(word);
		}
		return count;
	}
	
	/**
	 * Counts the occurrences of the word in all words.
	 * @param word the word to count
	 * @return the number of occurrences of the word in all 
	 * mails.
	 */
	public int countWordInAllEmails(String word) {
		return countWordInSpamMails(word) + countWordInNonSpamMails(word);
	}
	
	/**
	 * counts the number of non-unique words in spam mails.
	 * @return the number of words in spam mails.
	 */
	public int countSpamWords() {
		int amount = 0;
		for (Mail mail : spamMails) {
			amount += mail.countWords();
		}
		return amount;
	}
	
	/**
	 * counts the number of words in non spam mails.
	 * @return the number of words in non spam mails
	 */
	public int countNonSpamWords() {
		int amount = 0;
		for (Mail mail : nonSpamMails) {
			amount += mail.countWords();
		}
		return amount;
	}
	
	/**
	 * counts the number of words in all e-mails.
	 * Note that this is different from the size of the result of getWords, as
	 * this counts duplicates, while the set returned by getWords removes 
	 * duplicates.
	 * @return the number of words in all emails
	 */
	public int countAllWords() {
		return countSpamWords() + countNonSpamWords();
	}
	
	/**
	 * Adds a new spam mail to the set of all mails
	 * @param newMail the new mail to store
	 */
	public void addSpamMail(Mail newMail) {
		spamMails.add(newMail);
	}
	
	/**
	 * adds a new non spam mail to the set of all mails
	 * @param newMail the new mail to store
	 */
	public void addNonSpamMail(Mail newMail) {
		nonSpamMails.add(newMail);
	}
}
