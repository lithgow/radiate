package bad.robot.radiate

import java.net.{URI, URL}

object Url {

  implicit class UrlOps(url: URL) {
    def withDefaultPort(port: Int): URL = {
      if (url.getPort == -1) new URL(url.getProtocol, url.getHost, port, url.getFile)
      else url
    }
  }

  def url(url: String): URL = new URL(url.replace(" ", "%20"))

}


