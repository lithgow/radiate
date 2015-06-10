package bad.robot.radiate.teamcity

import argonaut.Argonaut._
import bad.robot.http.HttpResponse
import bad.robot.radiate.UnmarshallerS

import scalaz.{\/-, -\/}

class JsonProjectUnmarshallerS extends UnmarshallerS[HttpResponse, ProjectScala] {
  def unmarshall(response: HttpResponse): ProjectScala = {
    val json = new JsonResponseS(response).body
    val project = json.decodeEither[FullProjectS].valueOr(error => throw new Exception(error))
    project
  }
}

object Test extends App {
  val json = """{
               |  "id": "example",
               |  "name": "example",
               |  "href": "/guestAuth/app/rest/projects/id:example",
               |  "description": "",
               |  "archived": false,
               |  "webUrl": "http://localhost:8111/project.html?projectId=example",
               |  "parentProject": {
               |    "id": "_Root",
               |    "name": "<Root project>",
               |    "href": "/guestAuth/app/rest/projects/id:_Root"
               |  },
               |  "buildTypes": {
               |    "buildType": [
               |      {
               |        "id": "example_1",
               |        "name": "First",
               |        "href": "/guestAuth/app/rest/buildTypes/id:example_1",
               |        "projectName": "example",
               |        "projectId": "example",
               |        "webUrl": "http://localhost:8111/viewType.html?buildTypeId=example_1"
               |      },
               |      {
               |        "id": "example_2",
               |        "name": "Second",
               |        "href": "/guestAuth/app/rest/buildTypes/id:example_2",
               |        "projectName": "example",
               |        "projectId": "example",
               |        "webUrl": "http://localhost:8111/viewType.html?buildTypeId=example_2"
               |      }
               |    ]
               |  },
               |  "templates": {
               |    "buildType": []
               |  },
               |  "parameters": {
               |    "property": []
               |  },
               |  "vcsRoots": {
               |    "href": "/guestAuth/app/rest/vcs-roots?locator=project:(id:example)"
               |  },
               |  "projects": {
               |    "project": []
               |  }
               |}""".stripMargin
  json.decodeEither[FullProjectS] match {
    case -\/(msg) => println(msg)
    case \/-(project) => {
      println(project)
      project.foreach(println)
    }
  }

  val x: FullProjectS = json.decodeEither[FullProjectS].valueOr(error => throw new scala.Exception(error))
  println(x.buildTypes)

}

object DecodeExample extends App {

  import argonaut._
  import Argonaut._

  import scalaz._
  import Scalaz._

  case class Person(name: String, age: Int, greeting: String)

  implicit def PersonCodecJson: CodecJson[Person] = casecodec3(Person.apply, Person.unapply)("name", "age", "greeting")

  val valid = """ { "name" : "Toddler", "age" : 2, "greeting": "gurgle!" } """
  val parseError = """ { "name" : "Toddler", "age" 2, "greeting": "gurgle!" } """
  val decodeError = """ { "name" : "Toddler", "XXX" : 2, "greeting": "gurgle!" } """
  val json = parseError
  val jsonX = """
      | {
      |   "name" : "Toddler",
      |   "age" : 2,
      |   "greetings": {
      |     "greeting": [
      |     {
      |       "value": "gurgle!"
      |     },
      |     {
      |       "value": "another gurgle!"
      |     },
      |   }
      | }
      |""".stripMargin

  // Decode ignoring error messages
  val option: Option[Person] = Parse.decodeOption[Person](json)

  // Decode getting either error message or value (ignoring if it was parse or decode error)
  val result1: String \/ Person = Parse.decodeEither[Person](json)

  // Decode getting validation of error message or value
  val result2: Validation[String, Person] = Parse.decodeValidation[Person](json)

  // Decode getting either parse error message or decode error message with history or value
  val result3: (String \/ (String, CursorHistory)) \/ Person = Parse.decode[Person](json)

  // decode handling success and parse failure and decode failure with functions
  val greeting1: String = Parse.decodeWith[String, Person](json,
    _.greeting,
    msg => "got an error parsing: " + msg,
    {
      case (msg, history) => "got an error decoding: " + msg + " - " + history.shows }
    )

  // decode handling success and either parse failure or decode failure with functions
  val greeting2: String = Parse.decodeWithEither[String, Person](json,
    _.greeting, {
      case -\/(msg) => "got an error parsing: " + msg
      case \/-((msg, history)) => "got an error decoding: " + msg + " - " + history.shows
    })

  // decode handling success and failure message (ignoring if it was parse or decode).
  val greeting3: String = Parse.decodeWithMessage[String, Person](json, _.greeting, msg => "got an error: " + msg)

  // decode handling success and or providing a default for failure
  val greeting4: String = Parse.decodeOr[String, Person](json, _.greeting, "howdy")

  println("option = " + option)
  println("result1 = " + result1)
  println("result2 = " + result2)
  println("result3 = " + result3)
  println("greeting1 = " + greeting1)
  println("greeting2 = " + greeting2)
  println("greeting3 = " + greeting3)
  println("greeting4 = " + greeting4)
}