package bad.robot.radiate.monitor;

import bad.robot.radiate.Activity;
import bad.robot.radiate.NullProgress;
import bad.robot.radiate.Progress;
import bad.robot.radiate.Status;

import java.security.SecureRandom;
import java.util.Random;

import static bad.robot.radiate.Activity.*;
import static bad.robot.radiate.Activity.Error;
import static bad.robot.radiate.Status.*;

@Deprecated
public class RandomStatus extends ThreadSafeObservable implements MonitoringTask {

    private static final Random random = new SecureRandom();
    private static final Status[] statuses = new Status[]{Ok, Ok, Ok, Ok, Ok, Ok, Ok, Ok, Ok, Broken, Unknown};
    private static final Activity[] activities = new Activity[]{Busy, Error, Idle, Progressing};

    @Override
    public void run() {
        Activity activity = randomActivity();
        notifyObservers(activity, activity == Progressing ? randomProgress() : new NullProgress());
        Status status = randomStatus();
        notifyObservers(status);
        if (status == Broken)
            notifyObservers(new RuntimeException("Example problem"));
    }

    public static Progress randomProgress() {
        return new Progress(random.nextInt(100) + 1, 100);
    }

    public static Status randomStatus() {
        return statuses[random.nextInt(statuses.length)];
    }

    private static Activity randomActivity() {
        return activities[random.nextInt(activities.length)];
    }

    public String toString() {
        return "randomly passing build monitoring";
    }
}
