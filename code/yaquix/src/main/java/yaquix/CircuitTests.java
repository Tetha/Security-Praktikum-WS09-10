package yaquix;

import yaquix.circuit.Circuit;
import yaquix.circuit.CircuitBuilder;
import yaquix.circuit.GarbledCircuit;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * This class mostly exists to pick a single circuit, put some
 * inputs into it, execute the circuit and check what happens.
 * @author harald
 */
public class CircuitTests {
    private static SecureRandom randomSource;

    public static void main(String[] arguments) throws NoSuchAlgorithmException {
        Circuit subject = createSubject();

        boolean carry = true;
        boolean firstBitToAdd = true;
        boolean secondBitToAdd = true;

        boolean sumWithCarry = (firstBitToAdd ^ secondBitToAdd) ^ carry;
        boolean newCarryBit = (firstBitToAdd && secondBitToAdd) || (carry && (firstBitToAdd ^ secondBitToAdd));

        boolean[] input = {carry, firstBitToAdd, secondBitToAdd};

        boolean[] expectation = { newCarryBit, sumWithCarry };

        boolean[] output = evaluateWith(subject, input);
        checkExpectation(subject, input, expectation, output);
    }

    private static Circuit createSubject() {
        return CircuitBuilder.createFullAdder();
    }

    private static void checkExpectation(final Circuit c,
                                         final boolean[] input,
                                         final boolean[] expectation,
                                         final boolean[] output) {

        System.out.println(describeCircuit(c));
        System.out.println(describeInput(input));
        System.out.println(describeExpectation(expectation));
        System.out.println(describeOutputStatus(expectation, output));
    }

    private static String describeCircuit(final Circuit c) {
        return c.toString();
    }

    private static String describeInput(final boolean[] input) {
        return "Input:\t\t" + describeBitString(input);
    }

    private static String describeExpectation(final boolean[] expectation) {
        return "Expected:\t" + describeBitString(expectation);
    }

    private static String describeOutputStatus(final boolean[] expectation,
                                               final boolean[] output) {
        assert expectation.length == output.length;
        String outputLine = "Output:\t\t" + describeBitString(output);
        String errorLine = "Errors:\t\t";
        for (int bitIndex = 0; bitIndex < output.length; bitIndex++) {
            if (expectation[bitIndex] == output[bitIndex]) {
                errorLine += " ";
            } else {
                errorLine += "^";
            }
        }
        return outputLine + "\n" + errorLine;
    }

    private static String describeBitString(final boolean[] bitString) {
        StringBuilder result = new StringBuilder();
        for (boolean aBitString : bitString) {
            if (aBitString) {
                result.append("1");
            } else {
                result.append("0");
            }
        }
        return result.toString();
    }

    private static boolean[] evaluateWith(final Circuit subject,
                                          final boolean[] input) throws NoSuchAlgorithmException {

        randomSource = buildRandomSource();
        Map<Integer, int[]> inputMapping = buildInputMapping(input.length);
        GarbledCircuit executable = subject.garble(inputMapping, randomSource);
        setInputs(executable, input, inputMapping);
        return executable.evaluate();
    }

    private static SecureRandom buildRandomSource() throws NoSuchAlgorithmException {
        return SecureRandom.getInstance("SHA1PRNG");
    }

    private static int[] buildSingleInputMapping() {
		int trueValue;
		int falseValue;
		do {
			trueValue = randomSource.nextInt();
			falseValue = randomSource.nextInt();
		} while (trueValue == falseValue);

		int[] result = new int[2];
		result[0] = falseValue;
		result[1] = trueValue;
		return result;
	}

	/**
	 * Constructs a random mapping for INPUT_COUNT inputs.
	 * @param inputCount the number of inputs to garble
	 * @return an input mapping for each of these inputs
	 */
	private static Map<Integer, int[]> buildInputMapping(int inputCount) {
		Map<Integer, int[]> result = new HashMap<Integer, int[]>();
		for (int inputIndex = 0; inputIndex  < inputCount; inputIndex++) {
			result.put(inputIndex, buildSingleInputMapping());
		}
		return result;
	}

    private static void setInputs(final GarbledCircuit g,
                                  final boolean[] inputs,
                                  final Map<Integer, int[]> inputMapping) {
        for (int inputIndex = 0; inputIndex < inputs.length; inputIndex++) {
            boolean rawInputValue = inputs[inputIndex];
            int garbledInputValue;
            if (rawInputValue) {
                garbledInputValue = inputMapping.get(inputIndex)[1];
            } else {
                garbledInputValue = inputMapping.get(inputIndex)[0];
            }
            g.setInput(inputIndex, garbledInputValue);
        }
    }
}
