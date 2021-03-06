package bad.robot.radiate.teamcity

import java.net.URL

import bad.robot.UrlMatcher._
import bad.robot.radiate.UrlSyntax._
import simplehttp.EmptyHeaders._
import simplehttp.HeaderList._
import simplehttp.HeaderPair._
import simplehttp.{Headers, HttpClient, HttpResponse, StringHttpResponse}
import bad.robot.radiate.config.GuestAuthorisation
import bad.robot.radiate.{AggregateError, UnexpectedResponse, Unmarshaller}
import org.scalamock.specs2.IsolatedMockFactory
import org.specs2.scalaz.DisjunctionMatchers._
import org.specs2.mutable.Specification

import scalaz.\/-

class TeamCityTest extends Specification with IsolatedMockFactory {

  private val accept = headers(header("Accept", "application/json"))
  private val http = mock[HttpClient]

  private val Ok = new StringHttpResponse(200, "OK", "", emptyHeaders, "http://example.com")
  private val AnotherOk = new StringHttpResponse(200, "OK", "", emptyHeaders, "http://example.com")
  private val Error = new StringHttpResponse(500, "Yuk", "", emptyHeaders, "http://example.com")
  private val NotFound = new StringHttpResponse(404, "Not Found", "", emptyHeaders, "http://example.com")

  private val projects = Any.projects

  private val projectsUnmarshaller = mock[Unmarshaller[HttpResponse, Iterable[Project]]]
  private val projectUnmarshaller = mock[Unmarshaller[HttpResponse, Project]]
  private val buildUnmarshaller = mock[Unmarshaller[HttpResponse, Build]]
  private val teamcity = new TeamCity(TeamCityUrl("http://example.com:8111"), GuestAuthorisation, http, projectsUnmarshaller, projectUnmarshaller, buildUnmarshaller)

  private val buildTypes = new BuildTypes(List(Any.buildType))
  private val anotherBuildTypes = new BuildTypes(List(Any.buildType))
  private val project = Any.project(buildTypes)
  private val anotherProject = Any.project(anotherBuildTypes)

  "Should retrieve projects" >> {
    (http.get(_: URL, _: Headers)).expects(new URL("http://example.com:8111/guestAuth/app/rest/projects"), accept).once.returning(Ok)
    (projectsUnmarshaller.unmarshall _).expects(Ok).once.returning(\/-(projects))

    teamcity.retrieveProjects must be_\/-(projects)
  }

  "Should handle Http error when retrieving projects" >> {
    (http.get(_: URL, _: Headers)).expects(*, *).once.returning(Error)
    teamcity.retrieveProjects must beLeftDisjunction.like { case e: UnexpectedResponse => ok }
  }

  "Should retrieve full projects" >> {
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111${projects.head.href}"), accept).once.returning(Ok)
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111${projects.tail.head.href}"), accept).once.returning(AnotherOk)
    (projectUnmarshaller.unmarshall _).expects(Ok).once.returning(\/-(project))
    (projectUnmarshaller.unmarshall _).expects(AnotherOk).once.returning(\/-(anotherProject))

    teamcity.retrieveFullProjects(projects) must be_\/-(List(project, anotherProject))
  }

  "Should handle Http error(s) when retrieving full projects (this example attempts to load two projects and fails for both)" >> {
    (http.get(_: URL, _: Headers)).expects(*, *).anyNumberOfTimes.returning(Error)
    teamcity.retrieveFullProjects(projects) must beLeftDisjunction.like {
      case e: AggregateError => e.errors must contain(beAnInstanceOf[UnexpectedResponse])
    }
  }

  "Should retrieve build types" >> {
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111${projects.head.href}"), accept).once.returning(Ok)
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111${projects.tail.head.href}"), accept).once.returning(AnotherOk)
    (projectUnmarshaller.unmarshall _).expects(Ok).once.returning(\/-(project))
    (projectUnmarshaller.unmarshall _).expects(AnotherOk).once.returning(\/-(anotherProject))

    val actual = teamcity.retrieveBuildTypes(projects)
    actual must be_\/-(List(buildTypes.head, anotherBuildTypes.head))
  }

  "Should handle Http error when retrieving build types" >> {
    (http.get(_: URL, _: Headers)).expects(*, *).anyNumberOfTimes.returning(Error)
    teamcity.retrieveBuildTypes(projects) must beLeftDisjunction.like {
      case e: AggregateError => e.errors must contain(beAnInstanceOf[UnexpectedResponse])
    }
  }

  "Should retrieve latest running build" >> {
    val buildType = Any.buildType
    val build = Any.runningBuild
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111/guestAuth/app/rest/builds/buildType:${buildType.id},running:true"), accept).once.returning(Ok)
    (buildUnmarshaller.unmarshall _).expects(Ok).once.returning(\/-(build))

    teamcity.retrieveLatestBuild(buildType) must be_\/-(build)
  }

  "Should handle Http error when retrieving latest running build" >> {
    val buildType = Any.buildType
    (http.get(_: URL, _: Headers)).expects(urlContainingPath(s"${buildType.id},running:true"), accept).once.returning(Error)
    teamcity.retrieveLatestBuild(buildType) must beLeftDisjunction.like { case e: UnexpectedResponse => ok }
  }

  "Should retrieve latest latest non-running build" >> {
    val buildType = Any.buildType
    val build = Any.build
    (http.get(_: URL, _: Headers)).expects(urlContainingPath("running:true"), accept).once.returning(NotFound)
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111/guestAuth/app/rest/builds/buildType:${buildType.id}"), accept).once.returning(Ok)
    (buildUnmarshaller.unmarshall _).expects(Ok).once.returning(\/-(build))

    teamcity.retrieveLatestBuild(buildType) must be_\/-(build)
  }

  "Should handle Http error when retrieving latest non-running build" >> {
    val buildType = Any.buildType
    (http.get(_: URL, _: Headers)).expects(urlContainingPath("running:true"), *).once.returning(NotFound)
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111/guestAuth/app/rest/builds/buildType:${buildType.id}"), accept).once.returning(Error)

    teamcity.retrieveLatestBuild(buildType) must beLeftDisjunction.like { case e: UnexpectedResponse => ok }
  }

  "Should handle projects with no build history" >> {
    val buildType = Any.buildType
    (http.get(_: URL, _: Headers)).expects(urlContainingPath("running:true"), *).once.returning(NotFound)
    (http.get(_: URL, _: Headers)).expects(new URL(s"http://example.com:8111/guestAuth/app/rest/builds/buildType:${buildType.id}"), accept).once.returning(NotFound)

    teamcity.retrieveLatestBuild(buildType) must_== \/-(new NoBuild())
  }
}