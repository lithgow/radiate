package bad.robot.radiate.teamcity;

import bad.robot.http.*;
import bad.robot.radiate.Environment;
import bad.robot.radiate.Unmarshaller;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static bad.robot.http.EmptyHeaders.emptyHeaders;
import static bad.robot.http.HeaderList.headers;
import static bad.robot.http.HeaderPair.header;
import static bad.robot.http.HttpClients.anApacheClient;
import static bad.robot.radiate.Url.url;
import static com.googlecode.totallylazy.Sequences.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TeamCityTest {

    @Rule public Mockery context = new JUnit4Mockery();

    private final Headers accept = headers(header("Accept", "application/json"));
    private final HttpClient http = context.mock(HttpClient.class);

    private final HttpResponse ok = new StringHttpResponse(200, "OK", "", emptyHeaders());
    private final HttpResponse anotherOk = new StringHttpResponse(200, "OK", "", emptyHeaders());
    private final HttpResponse error = new StringHttpResponse(500, "Yuk", "", emptyHeaders());
    private final Iterable<Project> projects = Any.projects();

    private final Unmarshaller<HttpResponse, Iterable<Project>> projectsUnmarshaller = context.mock(Unmarshaller.class, "projects unmarshaller");
    private final Unmarshaller<HttpResponse, Project> projectUnmarshaller = context.mock(Unmarshaller.class, "project unmarshaller");
    private final TeamCity teamcity = new TeamCity(new Server("example.com"), http, projectsUnmarshaller, projectUnmarshaller);


    @Test
    @Ignore
    public void exampleUsage() throws MalformedURLException {
        String host = Environment.getEnvironmentVariable("teamcity.host");

        CommonHttpClient http = anApacheClient();
        Unmarshaller<HttpResponse, Iterable<Project>> projectsUnmarshaller = new JsonProjectsUnmarshaller();
        Unmarshaller<HttpResponse, Project> projectUnmarshaller = new JsonProjectUnmarshaller();
        TeamCity teamcity = new TeamCity(new Server(host), http, projectsUnmarshaller, projectUnmarshaller);

        Iterable<Project> projects = teamcity.retrieveProjects();
        Iterable<BuildType> buildTypes = teamcity.retrieveBuildTypes(projects);
        for (BuildType buildType : buildTypes) {
            Build build = teamcity.retrieveLatestBuild(buildType);
            System.out.printf("%s: #%s (id:%s) - %s%n", build.getBuildType().getName(), build.getNumber(), build.getId(), build.getStatusText());
        }
    }

    @Test
    public void shouldRetrieveProjects() throws MalformedURLException {
        context.checking(new Expectations() {{
            oneOf(http).get(new URL("http://example.com:8111/guestAuth/app/rest/projects"), accept); will(returnValue(ok));
            oneOf(projectsUnmarshaller).unmarshall(ok); will(returnValue(projects));
        }});
        assertThat(teamcity.retrieveProjects(), Matchers.<Iterable<Project>>is(projects));
    }

    @Test (expected = UnexpectedResponse.class)
    public void shouldHandleHttpErrorWhenRetrievingProjects() {
        context.checking(new Expectations() {{
            oneOf(http).get(with(any(URL.class)), with(any(Headers.class))); will(returnValue(error));
        }});
        teamcity.retrieveProjects();
    }

    @Test
    public void shouldRetrieveBuildTypes() throws MalformedURLException {
        final BuildTypes buildTypes = new BuildTypes(Any.buildType());
        final Project project = Any.project(buildTypes);
        final BuildTypes anotherBuildTypes = new BuildTypes(Any.buildType());
        final Project anotherProject = Any.project(anotherBuildTypes);

        context.checking(new Expectations() {{
            exactly(4).of(http).get(new URL("http://example.com:8111" + first(projects).getHref()), accept); will(returnValue(ok));
            exactly(4).of(http).get(new URL("http://example.com:8111" + second(projects).getHref()), accept); will(returnValue(anotherOk));
            exactly(4).of(projectUnmarshaller).unmarshall(ok); will(returnValue(project));
            exactly(4).of(projectUnmarshaller).unmarshall(anotherOk); will(returnValue(anotherProject));
        }});

        Iterable<BuildType> actual = teamcity.retrieveBuildTypes(projects);
        assertThat(actual, Matchers.<Iterable<BuildType>>is(sequence(first(buildTypes), first(anotherBuildTypes))));
    }

    private URL proxy() {
        return url("http://localhost:8888");
    }

}
