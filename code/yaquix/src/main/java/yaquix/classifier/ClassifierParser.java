package yaquix.classifier;

import yaquix.classifier.error.ClassifierParseException;
import yaquix.classifier.error.LimitTooLargeException;
import yaquix.classifier.error.LimitsUnsortedException;
import yaquix.classifier.error.NotEnoughSubtreesException;
import yaquix.classifier.error.SyntaxException;
import yaquix.classifier.error.TooManySubtreesException;
import yaquix.knowledge.Attribute;
import yaquix.knowledge.MailType;
import yaquix.knowledge.Occurrences;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * This parses a classifier in the format defined in the
 * specification.
 *
 * The parser implemented is a simple backtracking recursive
 * descent parser. That means, each parse method stores
 * the current input position and tries to parse whatever
 * it can parse by checking the prefix of the input for a certain
 * constant string or by calling other methods and checking
 * their return type for validness until either all possibilities
 * are exhausted and the parse fails or until a parse succeeded.
 * Furthermore, some productions perform a semantic evaluation of
 * the parse result and potentially throw parse exceptions.
 *
 * Note the difference between a syntax error, a parse failure and a
 * semantic error.
 *
 * A syntax error occurs if we could deduce that
 * a certain production must be in effect now (which we can easily do,
 * as the grammar is LL(1)), but the production matches only partially.
 * Nothing can rescue us in this case, so we raise a syntax error.
 *
 * A parse failure occurs if we try to match the first part of
 * a rule and this fails (for example, in the tree production, there is no
 * 'Decide' in the beginning of the string). This simply fails, because
 * another rule might still work.
 *
 * A semantic error occurs if we managed to parse something, but
 * whatever we managed to parse violates some invariants (sorted limits,
 * small limits, number of sub trees). This is decided after actually
 * parsing something and results in exceptions, too.
 * @author hk
 *
 */
public class ClassifierParser {
	/**
	 * contains the string to parse.
	 */
	private String input;

	/**
	 * contains the position we are at right now in input.
	 */
	private int positionInInput;

	/**
	 * contains the column in the line we are in.
	 * Note that this is different from positionInInput
	 * once we encountered a line break, as column is reset
	 * to 0 on a line break, while positionInInput just increases.
	 */
	private int column;

	/**
	 * contains the line we are in (which essentially is the number
	 * of linebreaks we have seen so far + 1)
	 */
	private int line;

	/**
	 * This creates a new Parser that parses the entire contents from
	 * the input stream.
     */
	public ClassifierParser(Reader input)
            throws IOException {
        this(allContents(input));
	}

    private static String allContents(Reader input)
        throws IOException {
        StringBuilder result = new StringBuilder();
        while (true) {
            int currentChar = input.read();
            if (currentChar == -1) {
                return result.toString();
            } else {
                result.append((char)currentChar);
            }
        }
    }
	/**
	 * This creates a new Parser to parse the given input.
	 * @param input the string to parse
	 */
	public ClassifierParser(String input) {
		positionInInput = 0;
		line = 0;
		column = 0;
		this.input = input;
	}

	/**
	 * parses the given input. This basically parses a tree
	 * and checks that the entire input was consumed. If
	 * something other than whitespace follows the
	 * classifier, a SyntaxError must be raised, as something
	 * is strange then.
	 * @return
	 */
	public Classifier parse() {
		Classifier classifier = parseTree();
		skipWhitespace();
		if(positionInInput != input.length()){
			throw new ClassifierParseException(line, column, "There is something strange here...");
		}
		return classifier;
	}

	/**
	 * Parses the tree production:
	 * tree -> 'Decide' '(' attribute (',' tree)+ ')'
	 * 		 | 'Output' '(' class ')'.
	 * If the number of trees is wrong, this raises
	 * TooManySubtreesExceptions or NotEnoughSubtreesExceptions.
	 * If the parse does not work at all, this throws a
	 * SyntaxException. This happens, e.g. if we manage
	 * to match 'Decide (', but not attribute. Since the grammar
	 * is small, we can see that no further match is possible then
	 * and thus, the syntax is broken.
	 * @return a classifier or null on failure
	 */
	private Classifier parseTree() {

		if(matchLiteral("Decide")){
			if(!matchLiteral("(")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {"("});

			Attribute attribute = parseAttribute();
			EnumMap<Occurrences, Classifier> map = new EnumMap<Occurrences, Classifier>(Occurrences.class);

			if(!matchLiteral(",")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {","});

			map.put(Occurrences.RARE, parseTree());

			if(map.get(Occurrences.RARE) == null)
				throw new NotEnoughSubtreesException(line, column, 3, new Classifier[] {map.get(Occurrences.RARE)});

			if(!matchLiteral(",")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {","});

			map.put(Occurrences.MEDIUM, parseTree());

			if(map.get(Occurrences.MEDIUM) == null)
				throw new NotEnoughSubtreesException(line, column, 3,
						new Classifier[] {map.get(Occurrences.RARE), map.get(Occurrences.MEDIUM)});

			if(!matchLiteral(",")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {","});

			map.put(Occurrences.OFTEN, parseTree());

			List<Classifier> testClassifier = new ArrayList<Classifier>();

			while(matchLiteral(",")){
				testClassifier.add(parseTree());
			}

			if(testClassifier.size() > 3) throw new TooManySubtreesException(line, column, 3, testClassifier);

			if(!matchLiteral(")")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {")"});

			return new Branch(attribute, map);

		} else if(matchLiteral("Output")){
			MailType lable;
			if(!matchLiteral("(")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {"("});

			lable = parseClass();
			if(lable == null)
					throw(new ClassifierParseException(line, column, "Something went wrong while parsing a label."));

			if(!matchLiteral(")")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {")"});

			return new Leaf(lable);

		} else throw new SyntaxException(line, column, input.substring(positionInInput, positionInInput+6), new String[] {"Decide", "Output"});
	}

	/**
	 * Parses the class production:
	 * class -> 'Spam' | 'Not Spam'.
	 * @return the class label or null on failure.
	 */
	private MailType parseClass() {
		MailType lable = null;
		if(matchLiteral("Spam")){
			lable = MailType.SPAM;
		}
		else if(matchLiteral("Not Spam")){
			lable = MailType.NONSPAM;
		} else {
            throw new ClassifierParseException(line, column, "Label not <Spam> or <Not Spam>");
        }
		return lable;
	}

	/**
	 * Parses the attribute production
	 * attribute -> '(' word ',' probability ',' probability ')'.
	 * If the limits are not ordered properly, this raises
	 * a LimitsUnsortedException. If the parse does not work at
	 * all (e.g. by matching '(' word, but not the ','), we can
	 * see that no parse will ever succeed here and thus, we raise
	 * a SyntaxException in that case.
	 * @return an Attribute or null on failure
	 */
	private Attribute parseAttribute() {
		String word;
		double low;
		double high;

		if(!matchLiteral("(")) throw new SyntaxException(line, column,
				Character.toString(input.charAt(positionInInput)), new String[] {"("});

		word = parseWord();

		if(!matchLiteral(",")) throw new SyntaxException(line, column,
				Character.toString(input.charAt(positionInInput)), new String[] {","});

		low = parseProbability();

		if(!matchLiteral(",")) throw new SyntaxException(line, column,
				Character.toString(input.charAt(positionInInput)), new String[] {","});

		high = parseProbability();

		if(!matchLiteral(")")) throw new SyntaxException(line, column,
				Character.toString(input.charAt(positionInInput)), new String[] {")"});

		if(low > high) throw new LimitsUnsortedException(line, column, low, high);

		return new Attribute(word, low, high);
	}

	/**
	 * Parses the word production
	 * word -> ('a'|'b'|...|'z'|'A'|...|'Z')+
	 * @return a string or null on failure
	 */
	private String parseWord() {
		StringBuilder tmpString = new StringBuilder();
		boolean found = false;
		char tmpChar;

		skipWhitespace();
		for(int i = positionInInput;i < input.length() ;i++){
			tmpChar = input.charAt(i);
			if(Character.isLetter(tmpChar)){
				positionInInput++;
				column++;
				found = true;
				tmpString.append(tmpChar);
			}
			else break;
		}
        if(found) return tmpString.toString(); else return null;
	}

	/**
	 * This parses the probability production:
	 * probability -> number '.' number
	 * If the Probability is larger than 1, a LmitTooLarge-Exception
	 * is raised. If the parse does not work at all (e.g. if we
	 * match number, but not ',', we raise a Syntax exception).
	 * @return the parsed number or -1 on failure
	 */
	private double parseProbability() {
		int integerPart;
		int realPart;

		skipWhitespace();
		integerPart = parseNumber();

		if(!matchLiteral(".")) throw new SyntaxException(line, column,
					Character.toString(input.charAt(positionInInput)), new String[] {"."});

		realPart = parseNumber();

		if(integerPart == -1 || realPart == -1)
			throw new ClassifierParseException(line, column, "Something went wrong while parsing a probability.");

		double probability = Double.parseDouble(integerPart + "." + realPart);

		if(probability > 1)
			throw new LimitTooLargeException(line, column, probability);

		return probability;
	}

	/**
	 * This parses the number production.
	 * number -> ('0' | '1' | ... | '9')+
	 * @return an integer on success or -1 on failure.
	 */
	private int parseNumber() {
		StringBuilder tmpString = new StringBuilder();
		char tmpChar;
		boolean found = false;

		skipWhitespace();
		for(int i = positionInInput;i < input.length(); i++){
			tmpChar = input.charAt(i);
			if(Character.isDigit(tmpChar)){
				found = true;
				positionInInput++;
				column++;
				tmpString.append(tmpChar);
			}
			else break;
		}
        if (found)
            return Integer.parseInt(tmpString.toString());
        else
            return -1;
	}

	/**
	 * This skips whitespaces. During skipping whitespaces, the line
	 * counter is incremented whenever a '\n' is encountered and the
	 * column counter is increased. This may also skip nothing.
	 */
	private void skipWhitespace() {
		for(int i = positionInInput; i < input.length(); i++){
			char current = input.charAt(i);
			if (current == '\n') {
				positionInInput++;
				column = 0;
				line++;
			} else if (Character.isWhitespace(current)) {
				positionInInput++;
				column++;
			}
			else break;
		}
	}

	/**
	 * This checks if the given literal is a prefix of the remaining string,
	 * starting at the positionInInput. In case of a successful match, the
	 * positionInString is updated.
	 * @param literal the literal to match
	 * @return true if the literal is a prefix, false otherwise.
	 */
	private boolean matchLiteral(String literal) {
		boolean matched = false;
		skipWhitespace();
		if(input.startsWith(literal, positionInInput)){
			matched = true;
			positionInInput += literal.length();
			column += literal.length();
		}
		return matched;
	}
}
