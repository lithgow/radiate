package bad.robot.radiate.teamcity

import bad.robot.radiate.monitor.{ThreadSafeObservable, MonitoringTask, MonitoringTasksFactory}
import bad.robot.radiate.Error
import scalaz.\/
import scalaz.syntax.either._

// todo do we need this?
class AllProjectsAsSingleTask extends ThreadSafeObservable with MonitoringTasksFactory {
  def create: Error \/ List[MonitoringTask] = {
    List(new AllProjectsMonitor).right
  }

  override def toString = "multiple projects as a single aggregate"
}