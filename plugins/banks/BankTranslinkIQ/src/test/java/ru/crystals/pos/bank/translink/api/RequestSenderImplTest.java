package ru.crystals.pos.bank.translink.api;

import org.apache.http.NoHttpResponseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.ResourceAccessException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.translink.api.dto.events.BaseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestSenderImplTest {

    private static final String NO_MORE_EVENTS_JSON = "/api/responses/events/no_more_events.json";
    private static final String GET_EVENT_URL = "http://localhost:6678/v102/getEvent";

    private MockRestServiceServer mockServer;
    private RequestSenderImpl rs;

    @Before
    public void setUp() {
        rs = new RequestSenderImpl();
        mockServer = MockRestServiceServer.createServer(rs.getRestTemplate());
    }

    @Test
    public void getEventSuccess() throws BankCommunicationException {
        mockServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(GET_EVENT_URL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(request -> makeResponse(NO_MORE_EVENTS_JSON));

        Assert.assertSame(BaseEvent.NO_MORE_EVENTS, rs.getEvent());
        mockServer.verify();
    }

    @Test
    public void getEventRepeatOnNoHttpResponse() throws BankCommunicationException {
        AtomicInteger failedAttempts = new AtomicInteger(3);
        mockServer.expect(ExpectedCount.times(3), MockRestRequestMatchers.requestTo(GET_EVENT_URL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(request -> {
                    if (failedAttempts.decrementAndGet() > 0) {
                        return noHttpResponseException();
                    }
                    return makeResponse(NO_MORE_EVENTS_JSON);
                });

        Assert.assertSame(BaseEvent.NO_MORE_EVENTS, rs.getEvent());
        mockServer.verify();
    }

    @Test
    public void getEventRepeatOnNoHttpResponseMaxAttempts() {
        mockServer.expect(ExpectedCount.times(5), MockRestRequestMatchers.requestTo(GET_EVENT_URL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(request ->  noHttpResponseException());
        try {
            rs.getEvent();
            Assert.fail("No expected exception");
        } catch (BankCommunicationException bce) {
            mockServer.verify();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void getEventRepeatOnAnotherError() {
        mockServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(GET_EVENT_URL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new ResourceAccessException("", new IOException(""));
                });
        try {
            rs.getEvent();
            Assert.fail("No expected exception");
        } catch (BankCommunicationException bce) {
            mockServer.verify();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception: " + e);
        }
    }

    private ClientHttpResponse noHttpResponseException() {
        throw new ResourceAccessException("", new NoHttpResponseException(""));
    }

    private ClientHttpResponse makeResponse(final String resourceName) {
        return new AbstractClientHttpResponse() {
            InputStream resourceAsStream;

            @Override
            public int getRawStatusCode() {
                return 200;
            }

            @Override
            public String getStatusText() {
                return "OK";
            }

            @Override
            public void close() {
                try {
                    resourceAsStream.close();
                } catch (Exception ignored) {
                }
                resourceAsStream = null;
            }

            @Override
            public InputStream getBody() {
                resourceAsStream = RequestSenderImplTest.class.getResourceAsStream(resourceName);
                return resourceAsStream;
            }

            @Override
            public HttpHeaders getHeaders() {
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                return httpHeaders;
            }
        };
    }
}