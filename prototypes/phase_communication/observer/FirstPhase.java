import java.util.List;
import java.util.LinkedList;

public class FirstPhase implements Observer {
    private String stringInput;
    private List<Observer> outputObservers;

    public FirstPhase() {
        outputObservers = new LinkedList<Observer>();
    }

    public void registerObserver(Observer obs) {
        outputObservers.add(obs);
    }

    public void handleValue(Object foo) {
        if (foo instanceof String) {
            stringInput = (String) foo;
        } else {
            throw new IllegalArgumentException("handleValue("
                                                + foo
                                                + ") unexpeted");
        }
    }

    public void execute() {
        for (Observer obs : outputObservers) {
            obs.handleValue(stringInput.length());
        }
    }
}
