package yaquix.phase.io;


import yaquix.Connection;
import yaquix.knowledge.Mail;
import yaquix.knowledge.Mails;
import yaquix.phase.InputKnowledge;
import yaquix.phase.OutputKnowledge;
import yaquix.phase.SymmetricPhase;

import java.io.File;
import java.io.IOException;

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
		super();
		this.localSpamFolder = localSpamFolder;
		this.localNonSpamFolder = localNonSpamFolder;
		this.localMails = localMails;
	}

	@Override
	protected void execute(Connection connection) throws IOException {
		logger.info("entering phase");
		Mails mails = new Mails();

		logger.info("reading spam mails");
		File[] files = localSpamFolder.get().listFiles();

		assert files != null : localSpamFolder.get() + " is no directory";
		for(File file : files) {
			mails.addSpamMail(Mail.readMail(file));
		}

		logger.info("reading non spam mails");
		files = localNonSpamFolder.get().listFiles();

		assert files != null : localNonSpamFolder.get() + " is no directory";
		for(File file : files){
			mails.addNonSpamMail(Mail.readMail(file));
		}

		localMails.put(mails);
		logger.info("leaving phase");
	}
}
