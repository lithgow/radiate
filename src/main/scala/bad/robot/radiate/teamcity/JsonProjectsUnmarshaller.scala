package bad.robot.radiate.teamcity

import argonaut.Argonaut._
import bad.robot.http.HttpResponse
import bad.robot.radiate.{ParseError, Unmarshaller}
import bad.robot.radiate.Error

import scalaz.\/

class JsonProjectsUnmarshaller extends Unmarshaller[HttpResponse, Iterable[Project]] {
  def unmarshall(response: HttpResponse): Error \/ Iterable[Project] = {
    val json = new JsonResponse(response).body
    json.decodeEither[Projects].leftMap(ParseError)
  }
}