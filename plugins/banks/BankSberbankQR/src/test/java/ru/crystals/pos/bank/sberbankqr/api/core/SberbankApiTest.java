package ru.crystals.pos.bank.sberbankqr.api.core;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.web.client.RestClientException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.sberbankqr.api.dto.OperationType;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderOperationParamType;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderState;
import ru.crystals.pos.bank.sberbankqr.api.dto.cancel.OrderCancelQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.creation.OrderCreationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.pay.PayRusClientQRRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.revocation.OrderRevocationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.status.OrderStatusRequestQrRs;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class SberbankApiTest {

    private SberbankApi sberbankApi;
    private MockRestServiceServer server;

    @Before
    public void setUp() throws Exception {
        TimeSupplier timeSupplier = mock(TimeSupplier.class);
        when(timeSupplier.currentMoscowTime())
                .thenReturn(ZonedDateTime.parse("2020-06-22T16:51:00.000+03:00[Europe/Moscow]"));
        RequestUidGenerator requestUidGenerator = mock(RequestUidGenerator.class);
        when(requestUidGenerator.generateRqUID()).thenReturn("39f791cbaa0b46a1a5340848c4edee53");
        sberbankApi = new SberbankApi(config(), timeSupplier, requestUidGenerator);
        server = MockRestServiceServer
                .bindTo(sberbankApi.getRestTemplate())
                .bufferContent()
                .build();
    }

    private SberbankApiConfig config() throws Exception {
        URL resourceUrl = getClass().getClassLoader().getResource("certificate.txt");
        assertNotNull(resourceUrl);
        Path resource = Paths.get(resourceUrl.toURI());
        String certificate = Files.lines(resource).collect(Collectors.joining());

        SberbankApiConfig config = new SberbankApiConfig();
        config.setCertificate(certificate);
        config.setClientId("6a98e5a7-5073-403a-abf5-5e40dab0e8ed");
        config.setClientSecret("U4cL5bP1qF7tJ1qU3kP7kM8rN8aO5jF3yN7oJ4bN6pJ8rU6wV7");
        config.setCertificatePassword("Yakovlevaa1");
        config.setUrl(SberbankQrUrl.TEST);
        config.setMemberId("000001");
        config.setTerminalId("21325622");
        config.setIdQR("20305");
        return config;
    }

    @Test
    public void testAuth() throws Exception {
        serverExpectAuthForPay();
        String authToken = sberbankApi.getAuthorizationToken(SberbankApiScope.PAY);
        server.verify();
        assertEquals("cbc72766-badd-4aa6-ab1e-b3dae39e281f", authToken);
    }

    @Test
    public void testPay() throws Exception {
        String expectedPayRequest = fileContent("requests/pay1.json");
        String payResponse = fileContent("responses/pay-ok.json");

        serverExpectAuthForPay();

        server.expect(once(), requestTo("https://uat.api.sberbank.ru:8443/prod/qr/bscanc/v1/pay"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedPayRequest, true))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payResponse));
        String qrPayLoad = "https://sberbank.ru/qr/?ClientIdQr=40ac47c5ed7944cf8e0775a398552f2c" +
                "&HashId=bf891fc9fb52ba8816cdd4c022fe6db5ec0d63296a7f3dafc17110180fadc303&TimeStamp=1592834161&online";
        int amount = 48000;
        PayRusClientQRRs.Status response = sberbankApi.pay(qrPayLoad, amount);
        server.verify();

        PayRusClientQRRs.Status expectedStatus = new PayRusClientQRRs.Status();
        expectedStatus.setRqUID("34554d25398b4bcab5407dd67e1def9a");
        expectedStatus.setRqTm(ZonedDateTime.parse("2020-06-22T16:57:00.000+03:00[Europe/Moscow]"));
        expectedStatus.setIdQR("20305");
        expectedStatus.setErrorCode("000000");
        expectedStatus.setErrorDescription("");
        expectedStatus.setPartnerOrderNumber("2132562220200622165100");
        expectedStatus.setMerchantId("000001");
        expectedStatus.setOrderId("63685a0e319149a9aec6adf98b5761cc");
        expectedStatus.setOrderState(OrderState.PAID);
        expectedStatus.setTerminalId("21325622");
        OrderOperationParamType expectedParam = new OrderOperationParamType();
        expectedParam.setOperationDateTime(ZonedDateTime.parse("2020-06-22T16:57:05.000+03:00[Europe/Moscow]"));
        expectedParam.setResponseCode("00");
        expectedParam.setResponseDesc("");
        expectedParam.setOperationCurrency("643");
        expectedParam.setOperationSum(48000);
        expectedParam.setOperationType(OperationType.PAY);
        expectedParam.setOperationId("34554d25398b4bcab5407dd67e1def9a");
        expectedParam.setRrn("017491333930");
        expectedParam.setAuthCode("223425");
        expectedStatus.setOrderOperationParam(expectedParam);

        assertEquals(expectedStatus, response);
    }

    @Test
    public void testStatus() throws Exception {
        String expectedStatusRequest = fileContent("requests/status1.json");
        String statusResponse = fileContent("responses/status-ok.json");

        serverExpectAuth("status");
        server.expect(once(), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/status"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedStatusRequest, true))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(statusResponse));

        OrderStatusRequestQrRs.Status response = sberbankApi.status("6721471923617792").getBody().getStatus();
        server.verify();

        OrderStatusRequestQrRs.Status expectedStatus = new OrderStatusRequestQrRs.Status();
        expectedStatus.setRqUID("cbc72766-badd-4aa6-ab1e-b3dae39e281f");
        expectedStatus.setRqTm(ZonedDateTime.parse("2020-06-22T16:57:00.000+03:00[Europe/Moscow]"));
        expectedStatus.setMerchantId("000001");
        expectedStatus.setTerminalId("21325622");
        expectedStatus.setIdQR("20305");
        expectedStatus.setOrderId("10001000518956637");
        expectedStatus.setOrderState(OrderState.PAID);
        expectedStatus.setErrorCode("000000");
        expectedStatus.setErrorDescription("");
        OrderOperationParamType expectedParam = new OrderOperationParamType();
        expectedParam.setOperationId("10001HFYYR8956637");
        expectedParam.setOperationDateTime(ZonedDateTime.parse("2020-06-22T16:57:05.000+03:00[Europe/Moscow]"));
        expectedParam.setRrn("664773635423");
        expectedParam.setOperationType(OperationType.PAY);
        expectedParam.setOperationSum(30000);
        expectedParam.setOperationCurrency("643");
        expectedParam.setAuthCode("885967");
        expectedParam.setResponseCode("00");
        expectedParam.setResponseDesc("");
        expectedStatus.setOrderOperationParams(Collections.singletonList(expectedParam));

        assertEquals(expectedStatus, response);
    }

    @Test
    public void testCancel() throws Exception {
        String expectedCancelRequest = fileContent("requests/cancel1.json");
        String cancelResponse = fileContent("responses/cancel-ok.json");

        serverExpectAuth("cancel");
        server.expect(once(), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedCancelRequest, true))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(cancelResponse));

        OrderCancelQrRs.Status response = sberbankApi.cancel("10001000518956637", "10001HFYYR8956637", "885967", 30000, "20305_orig");
        server.verify();

        OrderCancelQrRs.Status expectedStatus = new OrderCancelQrRs.Status();
        expectedStatus.setRqUID("cbc72766-badd-4aa6-ab1e-b3dae39e281f");
        expectedStatus.setRqTm(ZonedDateTime.parse("2020-06-30T16:05:01.000+03:00[Europe/Moscow]"));
        expectedStatus.setOrderId("10001000518956637");
        expectedStatus.setOrderState(OrderState.REVERSED);
        expectedStatus.setOperationId("10001HFYYR8956637");
        expectedStatus.setOperationDateTime(ZonedDateTime.parse("2020-06-30T15:05:01.000+03:00[Europe/Moscow]"));
        expectedStatus.setOperationType("Отмена");
        expectedStatus.setOperationSum(30000);
        expectedStatus.setOperationCurrency("643");
        expectedStatus.setAuthCode("885967");
        expectedStatus.setRrn("664773635423");
        expectedStatus.setTerminalId("75863425");
        expectedStatus.setIdQR("20305");
        expectedStatus.setErrorCode("000000");
        expectedStatus.setErrorDescription("");

        assertEquals(expectedStatus, response);
    }

    @Test
    public void testCreation() throws Exception {
        String expectedCreationRequest = fileContent("requests/creation1.json");
        String creationResponse = fileContent("responses/creation-ok.json");

        serverExpectAuth("create");
        server.expect(once(), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/creation"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedCreationRequest, true))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(creationResponse));

        OrderCreationQrRs.Status response = sberbankApi.creation(100);
        server.verify();

        OrderCreationQrRs.Status expectedStatus = new OrderCreationQrRs.Status();
        expectedStatus.setRqUID("cbc72766badd4aa6ab1eb3dae39e281f");
        expectedStatus.setRqTm(ZonedDateTime.parse("2020-07-27T12:59:07.000+03:00[Europe/Moscow]"));
        expectedStatus.setOrderFormUrl("https://sberbank.ru/qr/?dynamicQr=3a3d4baf63eb44739a569d1bda673b63");
        expectedStatus.setOrderId("3a3d4baf63eb44739a569d1bda673b63");
        expectedStatus.setOrderNumber("2132562220200622165100");
        expectedStatus.setErrorCode("000000");
        expectedStatus.setOrderState(OrderState.CREATED);

        assertEquals(expectedStatus, response);
    }

    @Test
    public void testRevocation() throws Exception {
        String expectedRevocationRequest = fileContent("requests/revocation1.json");
        String revocationResponse = fileContent("responses/revocation-ok.json");

        serverExpectAuth("revoke");
        server.expect(once(), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/revocation"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedRevocationRequest, true))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(revocationResponse));

        OrderRevocationQrRs.Status response = sberbankApi.revocation("3a3d4baf63eb44739a569d1bda673b63");
        server.verify();

        OrderRevocationQrRs.Status expectedStatus = new OrderRevocationQrRs.Status();
        expectedStatus.setRqUID("cbc72766badd4aa6ab1eb3dae39e281f");
        expectedStatus.setRqTm(ZonedDateTime.parse("2020-07-27T12:59:07.000+03:00[Europe/Moscow]"));
        expectedStatus.setOrderId("3a3d4baf63eb44739a569d1bda673b63");
        expectedStatus.setErrorCode("000000");
        expectedStatus.setOrderState(OrderState.REVOKED);

        assertEquals(expectedStatus, response);
    }

    @Test(expected = BankCommunicationException.class)
    public void testStatusWithRetriesFailed5times() throws Exception {
        String expectedStatusRequest = fileContent("requests/status1.json");

        serverExpectAuth(5, "status");

        server.expect(times(5), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/status"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedStatusRequest, true))
                .andRespond(request -> timeoutException());

        sberbankApi.statusWithRetries("6721471923617792");
        server.verify();
    }

    @Test
    public void testStatusWithRetriesFailed4times() throws Exception {
        String expectedStatusRequest = fileContent("requests/status1.json");
        String statusResponse = fileContent("responses/status-ok.json");

        serverExpectAuth(5, "status");

        server.expect(times(4), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/status"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedStatusRequest, true))
                .andRespond(request -> timeoutException());
        server.expect(once(), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/status"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedStatusRequest, true))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(statusResponse));

        sberbankApi.statusWithRetries("6721471923617792");
        server.verify();
    }

    @Test(expected = BankCommunicationException.class)
    public void testStatusWithRetriesWhenAuthFailed() throws Exception {
        String expectedAuthRequest = fileContent("requests/auth-status.txt");

        server.expect(times(5), requestTo("https://dev.api.sberbank.ru/ru/prod/tokens/v2/oauth"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().string(expectedAuthRequest))
                .andRespond(request -> timeoutException());

        sberbankApi.statusWithRetries("6721471923617792");
        server.verify();
    }

    @Test
    public void testStatusWithRetriesWhenAuthFailedOnce() throws Exception {
        String expectedAuthRequest = fileContent("requests/auth-status.txt");
        String expectedStatusRequest = fileContent("requests/status1.json");
        String statusResponse = fileContent("responses/status-ok.json");

        // 1. Первый запрос - отвалился auth
        server.expect(once(), requestTo("https://dev.api.sberbank.ru/ru/prod/tokens/v2/oauth"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().string(expectedAuthRequest))
                .andRespond(request -> timeoutException());
        // 2. Следующие 4 запроса - auth в порядке
        serverExpectAuth(4, "status");
        // 3. 3 из этих запросов - отвалился status
        server.expect(times(3), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/status"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedStatusRequest, true))
                .andRespond(request -> timeoutException());
        // 4. Последний status Ок (нет BankCommunicationException)
        server.expect(once(), requestTo("https://dev.api.sberbank.ru/ru/prod/order/v1/status"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json(expectedStatusRequest, true))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(statusResponse));

        sberbankApi.statusWithRetries("6721471923617792");
        server.verify();
    }

    private ClientHttpResponse timeoutException() {
        throw new RestClientException("any text", new SocketTimeoutException());
    }

    private void serverExpectAuthForPay() throws Exception {
        String expectedAuthRequest = fileContent("requests/auth-pay.txt");
        String authResponse = fileContent("responses/auth-pay-ok.json");

        server.expect(once(), requestTo("https://uat.api.sberbank.ru:8443/prod/tokens/v2/oauth"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().string(expectedAuthRequest))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(authResponse));
    }

    private void serverExpectAuth(String scope) throws Exception {
        serverExpectAuth(1, scope);
    }

    private void serverExpectAuth(int times, String scope) throws Exception {
        String expectedAuthRequest = fileContent("requests/auth-" + scope + ".txt");
        String authResponse = fileContent("responses/auth-" + scope + "-ok.json");

        server.expect(times(times), requestTo("https://dev.api.sberbank.ru/ru/prod/tokens/v2/oauth"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().string(expectedAuthRequest))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(authResponse));
    }

    private String fileContent(String resourcePath) throws Exception {
        URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);
        assertNotNull(resourceUrl);
        Path resource = Paths.get(resourceUrl.toURI());
        return Files.lines(resource).collect(Collectors.joining());
    }
}