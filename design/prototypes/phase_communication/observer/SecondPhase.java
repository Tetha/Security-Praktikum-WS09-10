import java.util.List;
import java.util.LinkedList;

public class SecondPhase implements Observer {
    private String stringInput;
    private int intInput;
    private List<Observer> outputObservers;

    public SecondPhase() {
        outputObservers = new LinkedList<Observer>();
    }


    public void handleValue(Object foo) {
        if (foo instanceof Integer) {
            intInput = (Integer) foo;
        } else if (foo instanceof String) {
            stringInput = (String) foo;
        } else {
            throw new IllegalArgumentException("handleValue("
                                              + foo
                                              + ") unexpected");
        }
    }

    public void registerObserver(Observer obs) {
        outputObservers.add(obs);
    }

    public void execute() {
        String output = stringInput + " has length " + intInput;
        for (Observer obs : outputObservers) {
            obs.handleValue(output);
        }
    }
}
