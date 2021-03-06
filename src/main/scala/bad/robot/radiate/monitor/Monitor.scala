package bad.robot.radiate.monitor

import java.util.concurrent.ScheduledFuture

trait Monitor {
  def start(tasks: List[MonitoringTask]): List[ScheduledFuture[_]]
  def cancel(tasks: List[ScheduledFuture[_]])
  def stop: List[Runnable]
}
