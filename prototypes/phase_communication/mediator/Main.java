public class Main {
    public static void main(String args[]) {
        System.out.println("Main input: space cow");
        Port<String> mainInput = new Port<String>();
        Port<Integer> lengthPipe = new Port<Integer>();
        Port<String> overallOutput = new Port<String>();

        FirstPhase first = new FirstPhase(mainInput, lengthPipe);
        SecondPhase second = new SecondPhase(mainInput, lengthPipe,
                                             overallOutput);
        mainInput.put("space cow");
        first.execute();
        second.execute();
        System.out.println("Main output: " + overallOutput.get());
    }
}
