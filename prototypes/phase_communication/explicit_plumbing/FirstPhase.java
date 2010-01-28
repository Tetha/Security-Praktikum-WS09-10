public class FirstPhase {
    private String input;
    private int output;

    public void setInputString(String pInput) {
        input = pInput;
    }

    public void execute() {
        output = input.length();
    }

    public int getOutputLength() {
        return output;
    }
}
