package bad.robot.radiate.teamcity

import simplehttp.HeaderList._
import simplehttp.HeaderPair._
import simplehttp.{Headers, HttpResponse, MessageContent}
import bad.robot.radiate.FunctionInterfaceOps.toMessageContent
import bad.robot.radiate.ParseError
import org.scalamock.specs2.IsolatedMockFactory
import org.specs2.mutable.Specification
import org.specs2.scalaz.DisjunctionMatchers._

class JsonProjectUnmarshallerTest extends Specification with IsolatedMockFactory {

  val response = stub[HttpResponse]
  val unmarshaller = new JsonProjectUnmarshaller

  "Unmarshall project with build types" >> {
    (response.getContent _: () => MessageContent).when().returns(projectJson)
    (response.getHeaders _: () => Headers).when().returns(headers(header("content-type", "application/json")))

    val buildTypes = BuildTypes(List(
      new BuildType("example_1", "First", "/guestAuth/app/rest/buildTypes/id:example_1", "example", "example"),
      new BuildType("example_2", "Second", "/guestAuth/app/rest/buildTypes/id:example_2", "example", "example")
    ))
    val project = unmarshaller.unmarshall(response)
    project must be_\/-(new Project("example", "example", "/guestAuth/app/rest/projects/id:example", buildTypes))
  }

  "Unmarshall empty project" >> {
    (response.getContent _: () => MessageContent).when().returns(emptyProjectJson)
    (response.getHeaders _: () => Headers).when().returns(headers(header("content-type", "application/json")))

    val project = unmarshaller.unmarshall(response)
    project must be_\/-(new Project("_Root", "<Root project>", "/guestAuth/app/rest/projects/id:_Root", BuildTypes(List())))
  }

  "Bad JSON" >> {
    (response.getContent _: () => MessageContent).when().returns("I'm not even json")
    (response.getHeaders _: () => Headers).when().returns(headers(header("content-type", "application/json")))

    unmarshaller.unmarshall(response) must be_-\/(ParseError("Unexpected content found: I'm not even json"))
  }

  "Minimal project json" >> {
    val json = """ {
                 |      "id": "_Root",
                 |      "name": "<Root project>",
                 |      "href": "/guestAuth/app/rest/projects/id:_Root"
                 | }""".stripMargin

    (response.getContent _: () => MessageContent).when().returns(json)
    (response.getHeaders _: () => Headers).when().returns(headers(header("content-type", "application/json")))

    val project = unmarshaller.unmarshall(response)
    project must be_\/-(new Project("_Root", "<Root project>", "/guestAuth/app/rest/projects/id:_Root", BuildTypes(List())))
  }

  val projectJson = """{
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

  val emptyProjectJson = """{
                      |  "id": "_Root",
                      |  "name": "<Root project>",
                      |  "href": "/guestAuth/app/rest/projects/id:_Root",
                      |  "description": "Contains all other projects",
                      |  "archived": false,
                      |  "webUrl": "http://example.com:8111/project.html?projectId=_Root",
                      |  "buildTypes": {
                      |    "buildType": []
                      |  },
                      |  "templates": {
                      |    "buildType": []
                      |  },
                      |  "parameters": {
                      |    "property": []
                      |  },
                      |  "vcsRoots": {
                      |    "href": "/guestAuth/app/rest/vcs-roots?locator=project:(id:_Root)"
                      |  },
                      |  "projects": {
                      |    "project": [
                      |      {
                      |        "id": "example",
                      |        "name": "example",
                      |        "href": "/guestAuth/app/rest/projects/id:example"
                      |      }
                      |    ]
                      |  }
                      |}""".stripMargin
}