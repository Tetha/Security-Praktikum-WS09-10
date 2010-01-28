public class SecondPhase {
    private String stringInput;
    private int intInput;

    private String stringOutput;

    public void setInputString(String in) {
        stringInput = in;
    }

    public void setInputInt(int in) {
        intInput = in;
    }

    public void execute() {
        stringOutput = stringInput + " has length " + intInput;
    }

    public String getOutputString() {
        return stringOutput;
    }
}
