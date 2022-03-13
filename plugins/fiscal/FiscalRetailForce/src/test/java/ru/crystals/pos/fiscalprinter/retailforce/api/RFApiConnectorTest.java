package ru.crystals.pos.fiscalprinter.retailforce.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.util.UriComponentsBuilder;
import ru.crystals.pos.fiscalprinter.retailforce.RetailForceConfig;

public class RFApiConnectorTest {

    private RFApiConnector api;
    private MockRestServiceServer mockJson;
    private MockRestServiceServer mockText;

    @Before
    public void setUp() {
        RetailForceConfig config = new RetailForceConfig();
        config.setUrl("http://localhost:7678");
        config.setIdentification("DE1234567890");
        config.setCloudApiKey("apikey");
        config.setCloudApiSecret("apisecret");
        config.setStoreNumber("S1");
        config.setTerminalNumber("T1");
        api = new RFApiConnector(config);

        mockJson = MockRestServiceServer.bindTo(api.getRtJson()).bufferContent().build();
        mockText = MockRestServiceServer.bindTo(api.getRtText()).bufferContent().build();

    }

    @Test
    public void register() {
        final String url = UriComponentsBuilder.fromHttpUrl("http://localhost:7678/api/v1/management/clients/byCloud")
                .queryParam("type", "[0] VatNumber")
                .queryParam("identification", "DE1234567890")
                .queryParam("storeNumber", "S1")
                .queryParam("terminalNumber", "T1")
                .queryParam("cloudApiKey", "apikey")
                .encode()
                .build().toUriString();

        mockText.expect(ExpectedCount.once(), MockRestRequestMatchers.method(HttpMethod.PUT))
                .andExpect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.content().string("'apisecret'"))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(MockRestResponseCreators.withSuccess().body("uuid"));

        final String result = api.registerClient();

        Assert.assertEquals("uuid", result);

        mockText.verify();
    }

    @Test
    public void connect() {
        final String url = UriComponentsBuilder.fromHttpUrl("http://localhost:7678/api/v1/management/cloud/connect")
                .queryParam("cloudApiKey", "apikey")
                .queryParam("clientId", "5c13392b-5951-4053-a05a-c536771ca475")
                .encode()
                .build().toUriString();

        mockText.expect(ExpectedCount.once(), MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.content().string("'apisecret'"))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(MockRestResponseCreators.withSuccess());

        api.connect("5c13392b-5951-4053-a05a-c536771ca475");

        mockText.verify();
    }

    @Test
    public void getVersion() {
        final String url = UriComponentsBuilder.fromHttpUrl("http://localhost:7678/api/v1/information/version")
                .encode()
                .build().toUriString();

        mockJson.expect(ExpectedCount.once(), MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo(url))
                .andRespond(MockRestResponseCreators.withSuccess()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("\"0.13.3.0\""));

        final String result = api.getVersion();

        Assert.assertEquals("0.13.3.0", result);
        mockJson.verify();
    }
}