package bad.robot.radiate;

import org.junit.Test;

import java.util.Collections;

import static bad.robot.radiate.Activity.*;
import static bad.robot.radiate.Activity.Error;
import static bad.robot.radiate.ActivityAggregator.aggregated;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

public class ActivityAggregatorTest {

    @Test
    public void emptyList() throws Exception {
        assertThat(aggregated(Collections.<Activity>emptyList()).getActivity(), is(Idle));
    }

    @Test
    public void shouldBeIdle() throws Exception {
        assertThat(aggregated(asList(Idle, Idle)).getActivity(), is(Idle));
    }

    @Test
    public void shouldBeProgressing() throws Exception {
        assertThat(aggregated(asList(Idle, Progressing)).getActivity(), is(Progressing));
    }

    @Test
    public void shouldBeBusy() {
        assertThat(aggregated(asList(Busy, Idle, Progressing)).getActivity(), is(Busy));
    }

    @Test
    public void shouldBeError() throws Exception {
        assertThat(aggregated(asList(Busy, Idle, Error, Progressing)).getActivity(), is(Error));
    }
}
