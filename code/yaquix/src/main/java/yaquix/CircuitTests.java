package yaquix;

import yaquix.circuit.Circuit;
import yaquix.circuit.CircuitBuilder;
import yaquix.circuit.GarbledCircuit;
import yaquix.circuit.base.And;
import yaquix.circuit.base.Or;
import yaquix.circuit.base.Split;
import yaquix.circuit.base.Xor;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class mostly exists to pick a single circuit, put some
 * inputs into it, execute the circuit and check what happens.
 * @author harald
 */
public class CircuitTests {
    private static SecureRandom randomSource;

    private static class CheckCircuit {
        public Circuit subject;
        public String description;
        public boolean[] input;
        public boolean[] expectation;

        public CheckCircuit(String description,
                            Circuit subject,
                            boolean[] input,
                            boolean[] expectation) {
            this.description = description;
            this.subject = subject;
            this.input = input;
            this.expectation = expectation;
        }
    }

    private static List<CheckCircuit> checks;

    static {
        checks = new LinkedList<CheckCircuit>();

        addComparerStateTransitionChecks();
        addBitwiseComparerTests();
        addCondAssignChecks();
        checkBaseXor();
        addTimesChecks();
        addMaxGainStateTransitionChecks();
        addMaxGainCircuitChecks();

        addCheck("Agreeing label computation",
                 CircuitBuilder.createAgreeingLabelComputation(2),
                 forInput(false, true,
                          false, true),
                 andExpect(false, true));

        addCheck("Agreeing label computation",
                 CircuitBuilder.createAgreeingLabelComputation(2),
                 forInput(false, false,
                          false, false),
                 andExpect(true, false));

        addCheck("Agreeing label computation",
                 CircuitBuilder.createAgreeingLabelComputation(2),
                 forInput(false, false,
                          false, true),
                 andExpect(true, true));

    }

    private static void addMaxGainCircuitChecks() {
        addCheck("Max gain circuit I",
                 CircuitBuilder.createMaximumGainCircuit(2, 2),
                 forInput(false, true,
                          false, true,
                          true, false,
                          false, false),
                 andExpect(false));

        addCheck("Max gain circuit II",
                 CircuitBuilder.createMaximumGainCircuit(2, 2),
                 forInput(false, false,
                          true, false,
                          false, true,
                          false, true),
                 andExpect(true));
    }

    private static void addMaxGainStateTransitionChecks() {
        addCheck("Max gain state transition, no update",
                CircuitBuilder.createMaxGainStatetransition(3, 2),
                forInput(
                        true, false, false, // current max sum
                        false, true, // curretn max index
                        false, true, false, // current sum
                        true, false), // current index

                andExpect(true, false, false, // previous max sum, as sum is smaller
                        false, true)); // previous max index

        addCheck("Max gain state transition, update",
                CircuitBuilder.createMaxGainStatetransition(3, 2),
                forInput(
                        false, false, false, // current max sum
                        false, false, // current max index
                        false, true, false, // current sum
                        true, false), // current index
                andExpect(false, true, false, // current sum is new max sum
                          true, false)); // current index is new max index
    }

    private static void addTimesChecks() {
        addCheck("Times, xor",
                 CircuitBuilder.times(4, new Xor()),
                 forInput(true, true, false, false, // four left inputs
                          true, false, true, false), // four right inputs
                 andExpect(false, true, true, false)); // four outputs

        // even indexes and odd indexes input into
        // even indexes and odd indexes in result
        addCheck("times, parallel and and or",
                 CircuitBuilder.times(2, new And().extendLeft(new Or())),
                 forInput(true, true, // inflated first input bit
                          true, false, // inflated second input bit
                          false, true,
                          false, false),
                 andExpect(true, true, // inflated first output bit
                           false, false)); // inflated second output

        addCheck("times, condassign",
                 CircuitBuilder.times(2, CircuitBuilder.makeCondAssign()),
                 forInput(true, false,
                          false, true,
                          true, false),
                 andExpect(true, true));

        addCheck("times split",
                 CircuitBuilder.times(2, new Split(2)),
                 forInput(true, false),
                 andExpect(true, false, true, false));
    }

    private static void checkBaseXor() {
        addCheck("xor TT",
                 new Xor(),
                 forInput(true, true),
                 andExpect(false));
        addCheck("xor TF",
                 new Xor(),
                 forInput(true, false),
                 andExpect(true));
        addCheck("xor FT",
                 new Xor(),
                 forInput(false, true),
                 andExpect(true));
        addCheck("xor FF",
                new Xor(),
                forInput(false, false),
                andExpect(false));
    }

    private static void addCondAssignChecks() {
        addCheck("Condassign, copy truth value true",
                 CircuitBuilder.makeCondAssign(),
                 forInput(true, false, true),
                 andExpect(true));
        addCheck("Condassign, copy truth value false",
                 CircuitBuilder.makeCondAssign(),
                 forInput(false, true, true),
                 andExpect(false));
        addCheck("Condassign, copy false value true",
                 CircuitBuilder.makeCondAssign(),
                 forInput(false, true, false),
                 andExpect(true));
        addCheck("Condassign, copy false value false",
                 CircuitBuilder.makeCondAssign(),
                 forInput(true, false, false),
                 andExpect(false));
    }

    private static void addBitwiseComparerTests() {
        addCheck("Bitwise comparer, equals",
                 CircuitBuilder.createComparer(3),
                 forInput(false, true, true,
                          false, true, true),
                 andExpect(false, false));
        addCheck("Bitwise comparer, lt",
                 CircuitBuilder.createComparer(3),
                 forInput(false, true, false,
                          false, false, true),
                 andExpect(true, false));
        addCheck("Bitwise comparer, gt",
                 CircuitBuilder.createComparer(3),
                 forInput(false, true, false,
                          true, false, true),
                 andExpect(true, true));

        addCheck("From maxGain state transition check",
                 CircuitBuilder.createComparer(3),
                 forInput(true, false, false,
                          false, true, false),
                 andExpect(true, false));
    }

    private static void addComparerStateTransitionChecks() {
        addCheck("Compare State transition I",
                 CircuitBuilder.createComparerStateTransition(),
                 forInput(false, false, false, false),
                 andExpect(false, false));
        addCheck("Compare Staten transition II",
                 CircuitBuilder.createComparerStateTransition(),
                 forInput(true, true, false, false),
                 andExpect(false, false));
        addCheck("Compare state transition III",
                 CircuitBuilder.createComparerStateTransition(),
                 forInput(true, false, false, false),
                 andExpect(true, false));
        addCheck("Compare state transition IV",
                 CircuitBuilder.createComparerStateTransition(),
                 forInput(false, true, false, false),
                 andExpect(true, true));
        addCheck("Compare state transition V",
                 CircuitBuilder.createComparerStateTransition(),
                 forInput(false, true, true, false),
                 andExpect(true, false));
        addCheck("Compare state transition V",
                 CircuitBuilder.createComparerStateTransition(),
                 forInput(true, false, true, true),
                 andExpect(true, true));
    }

    private static void addCheck(String description, Circuit circuit, boolean[] input, boolean[] expectation) {
        checks.add(new CheckCircuit(description, circuit, input, expectation));
    }
    public static void main(String[] arguments) throws NoSuchAlgorithmException {
        for (CheckCircuit check : checks)
            checkCircuit(check.description, check.subject, check.input, check.expectation);
    }

    private static boolean[] forInput(boolean... input) { return input; }
    private static boolean[] andExpect(boolean... output) { return output; }

    private static void checkCircuit(String description,
                                     Circuit subject,
                                     boolean[] input,
                                     boolean[] expectation) throws NoSuchAlgorithmException {
        if (subject.getInputCount() != input.length) {
            System.out.println("Input count mismatch:");
            System.out.println(describeCircuit(subject, description));
            System.out.println(String.format("%d != %d",
                                             subject.getInputCount(),
                                             input.length));
            return;
        }

        if (subject.getOutputCount() != expectation.length) {
            System.out.println("Output count mismatch:");
            System.out.println(describeCircuit(subject, description));
            System.out.println(String.format("%d != %d",
                                             subject.getOutputCount(),
                                             expectation.length));
            return;
        }

        boolean[] output = evaluateWith(subject, input);
        checkExpectation(description, subject, input, expectation, output);
    }

    private static void checkExpectation(String description,
                                         Circuit c,
                                         boolean[] input,
                                         boolean[] expectation,
                                         boolean[] output) {
        if (Arrays.equals(expectation, output)) return;
        System.out.println(describeCircuit(c, description));
        System.out.println(describeInput(input));
        System.out.println(describeExpectation(expectation));
        System.out.println(describeOutputStatus(expectation, output));
    }

    private static String describeCircuit(final Circuit c, String description) {
        return description + " " + c.toString();
    }

    private static String describeInput(final boolean[] input) {
        return "Input:\t\t" + describeBitString(input);
    }

    private static String describeExpectation(final boolean[] expectation) {
        return "Expected:\t" + describeBitString(expectation);
    }

    private static String describeOutputStatus(final boolean[] expectation,
                                               final boolean[] output) {
        String outputLine = "Output:\t\t" + describeBitString(output);
        String errorLine = "Errors:\t\t";
        for (int bitIndex = 0;
             bitIndex < Math.min(output.length, expectation.length); bitIndex++) {
            if (expectation[bitIndex] == output[bitIndex]) {
                errorLine += " ";
            } else {
                errorLine += "^";
            }
        }

        if (output.length != expectation.length)
            for (int bitIndex = 0;
                 bitIndex < Math.max(output.length, expectation.length)
                             - Math.min(output.length, expectation.length);
                 bitIndex++)
                errorLine += "?";
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
