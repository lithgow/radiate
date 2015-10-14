package bad.robot.radiate.teamcity

import bad.robot.http.HttpClient
import bad.robot.http.HttpClients.anApacheClient
import bad.robot.http.configuration.HttpTimeout.httpTimeout
import bad.robot.radiate.config.Config
import com.google.code.tempusfugit.temporal.Duration.minutes

object HttpClientFactory {
  def apply() = new HttpClientFactory()
}

class HttpClientFactory private {
  def create(config: Config): HttpClient = {
    val client = anApacheClient
    Authentication(config).applyTo(client)
    client.`with`(httpTimeout(minutes(10)))
  }
}