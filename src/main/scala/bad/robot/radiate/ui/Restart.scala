package bad.robot.radiate.ui

import java.awt._
import java.awt.event.{AWTEventListener, KeyAdapter, KeyEvent}

import bad.robot.radiate.Main._
import bad.robot.radiate.monitor.MonitoringTasksFactory

class RestartS(taskFactory: MonitoringTasksFactory, keyCode: Int) extends KeyAdapter with AWTEventListener {

  override def keyPressed(event: KeyEvent) {
    if (event.getKeyCode == keyCode) {
      Radiate.stop()
      Radiate.start(taskFactory, Radiate.getCurrentFrames)
    }
  }

  def eventDispatched(event: AWTEvent) {
    if (event.getID == KeyEvent.KEY_PRESSED) keyPressed(event.asInstanceOf[KeyEvent])
  }
}