public class Main {
    public static void main(String[] args) {
        FirstPhase first = new FirstPhase();
        SecondPhase second = new SecondPhase();


        System.out.println("Main input: space cow");
        first.setInputString("space cow");
        first.execute();

        second.setInputString("space cow");
        second.setInputInt(first.getOutputLength());

        second.execute();

        System.out.println("Main output: " + second.getOutputString());
    }
}
