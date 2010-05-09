package yaquix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.jnlp.FileContents;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yaquix.phase.InputKnowledge;
import yaquix.phase.Knowledge;
import yaquix.phase.Phase;
import yaquix.phase.ReadLearnWrite;

import ch.qos.logback.classic.BasicConfigurator;

/**
 * This class parses the command line arguments and sets up
 * the connection or reads mails for the classifier.
 * @author hk
 *
 */
@SuppressWarnings("static-access")
public class Main {
	private static final Options options;
	private static Logger LOG = LoggerFactory.getLogger(Main.class);

	static {
		options = new Options();

		OptionGroup mode = new OptionGroup();
		Option server = OptionBuilder.withLongOpt("server")
									 .withDescription("starts in server mode, excludes client and classifier modes")
									 .create();
		Option client = OptionBuilder.withLongOpt("client")
									 .withDescription("starts in client mode. excludes server and classifier modes")
									 .create();
		Option classifier = OptionBuilder.withLongOpt("classifier")
										 .withDescription("starts in classifier mode. excludes server and client mode")
										 .create();
		mode.addOption(server);
		mode.addOption(client);
		mode.addOption(classifier);
		options.addOptionGroup(mode);
		Option classifierFile = OptionBuilder.withLongOpt("classifier-file")
										     .hasArg()
										     .withArgName("FILE")
										     .withDescription("in classifier mode: a file to load the classifier from, in server or client mode: a file to store the classifier to")
										     .create('c');
		options.addOption(classifierFile);

		Option port = OptionBuilder.withLongOpt("port")
								   .hasArg()
								   .withArgName("NUMBER")
								   .withDescription("the port server and client are supposed to use. (Default: 13013)")
								   .create('p');
		options.addOption(port);

		Option host = OptionBuilder.withLongOpt("host")
								   .hasArg()
								   .withArgName("ADDRESS")
								   .withDescription("the host the client has to connect to")
								   .create('h');
		options.addOption(host);
	}

	public static void main(String[] args) {
		LOG.info("Starting application");

		CommandLine arguments;
		try {
			arguments = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			LOG.error("Error while parsing command line arguments: " + e.toString());
			return;
		}

		if (!validateArguments(arguments)) {
			LOG.error("Error in command line argument values.");
			return;
		}

		runApplication(arguments);
	}

	private static void runApplication(CommandLine arguments) {
		if (arguments.hasOption("server") || arguments.hasOption("client")) {
			if (arguments.hasOption("server")) {
				LOG.info("Server mode selected");
			} else {
				LOG.info("client mode selected");
			}

			int port;
			if (arguments.hasOption('p')) {
				String userPort = arguments.getOptionValue('p');
				port = Integer.parseInt(userPort);
			} else {
				LOG.info("Using default port 13013");
				port = 13013;
			}

			String host = arguments.getOptionValue('h');

			Connection connection = null; // TODO: fill
			if (arguments.hasOption("server")) {
				try {
					ServerSocket server = new ServerSocket(port);
					connection = new Connection(server);
				} catch (IOException e) {
					LOG.error("Cannot start server socket " + e);
					return;
				}
			} else {
				try {
					Socket client = new Socket(host, port);
					connection = new Connection(client);
				} catch (IOException e) {
					LOG.error("Cannot start client socket " + e);
					return;
				}
			}

			String mailDir = arguments.getArgs()[0];
			Knowledge<File> localSpamFolder = new Knowledge<File>();
			localSpamFolder.put(new File(mailDir, "spam"));

			Knowledge<File> localNonSpamFolder = new Knowledge<File>();
			localNonSpamFolder.put(new File(mailDir, "non_spam"));

			Writer output;
			if (arguments.hasOption('o')) {
				String outFileName = arguments.getOptionValue('o');
				try {
					output = new FileWriter(outFileName);
				} catch (IOException e) {
					LOG.error("cannot open file " + outFileName + " for writing: " + e);
					return;
				}
			} else {
				output = new OutputStreamWriter(System.out);
			}
			Knowledge<Writer> localOutput = new Knowledge<Writer>();
			localOutput.put(output);

			SecureRandom randomSource;
			try {
				randomSource = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException e) {
				LOG.error("Cannot select secure random algorithm. " + e);
				return;
			}

			Phase executedPhase = new ReadLearnWrite(localSpamFolder, localNonSpamFolder, localOutput, randomSource);

			try {
				if (arguments.hasOption("server")) {
					executedPhase.serverExecute(connection);
				} else {
					executedPhase.clientExecute(connection);
				}
			} catch (ClassNotFoundException e) {
				LOG.error("Transmitting data failed." + e);
				return;
			} catch (IOException e) {
				LOG.error("IO failed " + e);
				e.printStackTrace();
				return;
			}

		} else if (arguments.hasOption("classifier")) {
			LOG.info("Classifier mode selected");
		} else {
			assert false : "No mode selected";
		}
	}

	private static boolean validateArguments(CommandLine arguments) {
		boolean valid = true;
		int remainingArgCount = arguments.getArgs().length;
		if (remainingArgCount != 1) {
			valid = false;
			if (remainingArgCount > 1) {
				LOG.error("Too many positional arguments remaining.");
			} else {
				LOG.error("No Maildir given");
			}
		}

		valid = valid && requireMode(arguments);
		if (arguments.hasOption("server")) {
			valid = valid && validateServer(arguments);
		} else if (arguments.hasOption("client")) {
			valid = valid && validateClient(arguments);
		} else if (arguments.hasOption("classifier")) {
			valid = valid && validateClassifier(arguments);
		} else {
			assert false : "Modes not exhausted";
		}

		return valid;
	}

	private static boolean validateClassifier(CommandLine arguments) {
		return true;
	}

	private static boolean validateClient(CommandLine arguments) {
		boolean valid = true;
		if (!arguments.hasOption('h')) {
			LOG.error("No host given.");
			valid = false;
		}
		return valid;
	}

	private static boolean validateServer(CommandLine arguments) {
		boolean valid = true;
		if (arguments.hasOption('p')) {
			try {
				Integer.parseInt(arguments.getOptionValue('p'));
			} catch (NumberFormatException e) {
				valid = false;
			}
		}
		return valid;
	}

	private static boolean requireMode(CommandLine arguments) {
		if (!arguments.hasOption("server")
				&& !arguments.hasOption("client")
				&& !arguments.hasOption("classifier))")) {
			LOG.error("no mode selected");
			return false;
		} else {
			return true;
		}
	}
}
