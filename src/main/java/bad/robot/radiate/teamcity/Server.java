package bad.robot.radiate.teamcity;

import bad.robot.radiate.Hypermedia;
import bad.robot.radiate.Url;

import java.net.URL;

import static bad.robot.radiate.teamcity.TeamCityEndpoints.buildsEndpointFor;
import static java.lang.String.format;

@Deprecated
class Server {

    private final String host;
    private final Integer port;

    public Server(String host, Integer port) {
        this.host = host.replace("http://", "");
        this.port = port;
    }

    public URL urlFor(Hypermedia endpoint) {
        return Url.url(baseUrl() + endpoint.getHref());
    }

    public URL urlFor(BuildLocatorBuilder locator, Authorisation authorisation) {
        return Url.url(baseUrl() + buildsEndpointFor(authorisation).getHref() + locator.build());
    }

    private String baseUrl() {
        return format("http://%s:%s", host, port);
    }

}
