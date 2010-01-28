public class Main implements Observer {
    private String output;

    public void handleValue(Object foo) {
        if (foo instanceof String) {
            output = (String) foo;
        } else {
            throw new IllegalArgumentException("handleValue("
                                                + foo
                                                + ") unexpeted");
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        FirstPhase first = new FirstPhase();
        SecondPhase second = new SecondPhase();

        System.out.println("Main input: space cow");
        first.registerObserver(second);
        first.handleValue("space cow");
        second.handleValue("space cow");
        second.registerObserver(main);

        first.execute();
        second.execute();
        System.out.println("Main output:" + main.output);
    }
}
