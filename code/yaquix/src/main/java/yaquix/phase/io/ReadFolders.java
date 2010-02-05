package yaquix.phase.io;


import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yaquix.Connection;
import yaquix.knowledge.Mail;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

/**
 * This class takes a folder for the spam mail files and a folder
 * for the non spam mails and reads the contents of the folders
 * into mail objects and stores those mail objects in a 
 * mails object.
 * @author hk
 *
 */
public class ReadFolders extends SymmetricPhase {
	/**
	 * contains the name of the base folder containing the spam mail files.
	 */
	private InputKnowledge<File> localSpamFolder;
	
	/**
	 * contains the name of the base folder containing the non spam mail files.
	 */
	private InputKnowledge<File> localNonSpamFolder;
	
	/**
	 * contains a place to store the read mails.
	 */
	private OutputKnowledge<Mails> localMails;
	
	/**
	 * Constructs a new phase to read the input folders
	 * @param localSpamFolder the folder containing spam mails
	 * @param localNonSpamFolder the folder containing non spam mails
	 * @param localMails a place to store the read mails
	 */
	public ReadFolders(InputKnowledge<File> localSpamFolder,
			InputKnowledge<File> localNonSpamFolder,
			OutputKnowledge<Mails> localMails) {
		this.localSpamFolder = localSpamFolder;
		this.localNonSpamFolder = localNonSpamFolder;
		this.localMails = localMails;
	}


	@Override
	protected void execute(Connection connection) {
		// TODO execute		
	}
	
	/**
	 * reads a single mail from the file.
	 * @param filename the file to read
	 * @return the mail containing the file contents.
	 */
	private Mail readMail(File filename) {
		// TODO: readMail
		return null;
	}
}