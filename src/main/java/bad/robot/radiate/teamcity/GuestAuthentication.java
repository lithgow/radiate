package bad.robot.radiate.teamcity;

import bad.robot.http.CommonHttpClient;

@Deprecated
public class GuestAuthentication extends BasicAuthConfiguration {

    public GuestAuthentication() {
        super(null, null, null);
    }

    @Override
    public void applyTo(CommonHttpClient client) {
        // no-op
    }
}
