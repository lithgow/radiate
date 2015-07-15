package bad.robot.radiate.ui;

import bad.robot.radiate.monitor.MonitoringTasksFactory;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static bad.robot.radiate.Main.*;

@Deprecated
class Restart extends KeyAdapter implements AWTEventListener {

    private final MonitoringTasksFactory taskFactory;
    private final int keyCode;

    public Restart(MonitoringTasksFactory taskFactory, int keyCode) {
        this.taskFactory = taskFactory;
        this.keyCode = keyCode;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == keyCode) {
            Radiate.stop();
            Radiate.start(taskFactory, Radiate.getCurrentFrames());
        }
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED)
            keyPressed((KeyEvent) event);
    }
}