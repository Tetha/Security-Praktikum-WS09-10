public class FirstPhase {
    private Port<String> stringInput;
    private Port<Integer> intOutput;

    public FirstPhase(Port<String> pStringInput,
                      Port<Integer> pIntOutput) {
        stringInput = pStringInput;
        intOutput = pIntOutput;
    }

    public void execute() {
        intOutput.put(stringInput.get().length());
    }
}
