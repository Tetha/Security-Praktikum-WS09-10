package yaquix.knowledge;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
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
		List<String> words = new LinkedList<String>();
		StringBuilder currentWord = new StringBuilder();
		boolean inWord = false;
		for (int i = 0; i < content.length(); i++) {
			char currentChar = content.charAt(i);
			boolean isLetter = 'a' <= currentChar && currentChar <= 'z';
			if (inWord) {
				if (isLetter) {
					// still in word
					currentWord.append(currentChar);
				} else {
					// word ended
					words.add(currentWord.toString());
					inWord = false;
				}
			} else {
				if (isLetter) {
					// now in word
					currentWord = new StringBuilder();
					currentWord.append(currentChar);
					inWord = true;
				} else {
					// still not in word
					continue;
				}
			}
		}
		if (inWord) words.add(currentWord.toString()); // last word has no following non-wordy-character
		return words;
	}

	/**
	 * returns the set of words contained in this mail.
	 * @return the set of words in the mail
	 */
	public Set<String> getWords() {
		return new HashSet<String>(parseWords());
	}

	/**
	 * counts the number of words in the mail content. Note that this is
	 * different from the magnitude of getWords(), because getWords
	 * eliminates duplicates, while this counts duplicates.
	 * @return the number of words in the email.
	 */
	public int countWords() {
		return parseWords().size();
	}

	/**
	 * This counts the number of occurrences of the word in the email content.
	 * @param needle the word to count
	 * @return the number of occurrences
	 */
	public int countWord(String needle) {
		int occurrences = 0;
		for (String word : parseWords()) {
			if (needle.equals(word)) {
				occurrences++;
			}
		}
		return occurrences;
	}

    /**
     * reads a single mail from the file.
     * @param filename the file to read
     * @return the mail containing the file contents.
     * @throws java.io.IOException
     */
    public static Mail readMail(File filename) throws IOException {

        FileReader reader = new FileReader(filename);
String tmpString;

char[] tempChars = new char[(int) filename.length()];

reader.read(tempChars);
tmpString = new String(tempChars);

return new Mail(tmpString);
    }
}
