package bad.robot.radiate.monitor

class MonitoringExceptionS(message: String) extends Exception(message)

class NothingToMonitorExceptionS extends MonitoringExceptionS("Nothing found to monitor, check your configuration")