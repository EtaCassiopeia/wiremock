package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.verification.NearMissCalculator.NEAR_MISS_COUNT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NearMissCalculatorTest {

    private Mockery context;

    NearMissCalculator nearMissCalculator;

    StubMappings stubMappings;
    RequestJournal requestJournal;

    @Before
    public void init() {
        context = new Mockery();

        stubMappings = context.mock(StubMappings.class);
        nearMissCalculator = new NearMissCalculator(stubMappings, requestJournal);
    }

    @Test
    public void returnsNearest3MissesForSingleRequest() {
        context.checking(new Expectations() {{
            one(stubMappings).getAll(); will(returnValue(
                asList(
                    get(urlEqualTo("/righ")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/totally-wrong1")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/totally-wrong222")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/almost-right")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/rig")).willReturn(aResponse()).build(),
                    get(urlEqualTo("/totally-wrong33333")).willReturn(aResponse()).build()
                )
            ));
        }});

        List<NearMiss> nearest = nearMissCalculator.findNearestFor(mockRequest().url("/right").asLoggedRequest());

        assertThat(nearest.size(), is(NEAR_MISS_COUNT));
        assertThat(nearest.get(0).getStubMapping().getRequest().getUrl(), is("/righ"));
        assertThat(nearest.get(1).getStubMapping().getRequest().getUrl(), is("/rig"));
        assertThat(nearest.get(2).getStubMapping().getRequest().getUrl(), is("/almost-right"));
    }

    @Test
    public void returns0NearMissesForSingleRequestWhenNoStubsPresent() {
        context.checking(new Expectations() {{
            one(stubMappings).getAll(); will(returnValue(emptyList()));
        }});

        List<NearMiss> nearest = nearMissCalculator.findNearestFor(mockRequest().url("/right").asLoggedRequest());

        assertThat(nearest.size(), is(0));
    }
}
