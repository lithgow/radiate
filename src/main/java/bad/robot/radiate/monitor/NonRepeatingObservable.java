package bad.robot.radiate.monitor;

import java.util.HashSet;
import java.util.Set;

@Deprecated
public class NonRepeatingObservable extends ThreadSafeObservable {

    private final Set<Information> previous = new HashSet<>();

    @Override
    public void notifyObservers(Information information) {
        if (previous.add(information)) {
            super.notifyObservers(information);
        }
    }
}
