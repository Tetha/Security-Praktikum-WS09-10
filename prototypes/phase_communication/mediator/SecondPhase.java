public class SecondPhase {
    private Port<String> stringInput;
    private Port<Integer> intInput;
    private Port<String> stringOutput;

    public SecondPhase(Port<String> pStringInput,
                       Port<Integer> pIntInput,
                       Port<String> pStringOutput) {
        stringInput = pStringInput;
        intInput = pIntInput;
        stringOutput = pStringOutput;
    }

    public void execute() {
        stringOutput.put(stringInput.get() + " has length " + intInput.get());
    }
}
