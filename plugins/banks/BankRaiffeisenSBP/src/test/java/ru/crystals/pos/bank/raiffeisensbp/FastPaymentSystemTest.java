package ru.crystals.pos.bank.raiffeisensbp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.testng.Assert;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.raiffeisensbp.api.request.QRType;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RefundRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RegistrationQRRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.response.CancelQrResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.QRInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.ResponseStatusCode;
import ru.crystals.pos.bank.raiffeisensbp.api.status.PaymentStatus;
import ru.crystals.pos.bank.raiffeisensbp.api.status.RefundStatus;
import ru.crystals.pos.currency.CurrencyUtil;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class FastPaymentSystemTest {

    private FastPaymentSystem fastPay;
    private SBPConfig fastPayConfig;

    private MockRestServiceServer mockServer;

    @Before
    public void init() {
        fastPayConfig = new SBPConfig();
        fastPayConfig.setUrl(RaiffeisenSBPURL.PRODUCTION.name());
        fastPayConfig.setSecretKey("test_secret_key");
        fastPayConfig.setAccount("40702810200001448172");
        fastPayConfig.setSbpMerchantId("MA0000002371");
        fastPayConfig.setQrExpiration(5L);
        fastPay = new FastPaymentSystem(fastPayConfig);
        mockServer = MockRestServiceServer.createServer(fastPay.getRestTemplate());
    }

    @Test
    public void testRegistrationQR() throws JsonProcessingException, URISyntaxException, BankCommunicationException {
        QRInfoResponse expectedResponse = new QRInfoResponse(ResponseStatusCode.SUCCESS,
                null, "AD10000LIELKGQDK9TSAUC4MOQHTLRPU",
                "https://qr.nspk.ru/AD10000LIELKGQDK9TSAUC4MOQHTLRPU?type=02&bank=raiffei1test&sum=111000&cur=RUB&crc=277B",
                "https://test.ecom.raiffeisen.ru/api/sbp/v1/qr/AD10000LIELKGQDK9TSAUC4MOQHTLRPU/image");

        RegistrationQRRequest registrationRequest = RegistrationQRRequest.builder()
                .setAccount("40702810200001448172")
                .setAmount(CurrencyUtil.convertMoney(1110L))
                .setCreateDate(OffsetDateTime.now(ZoneOffset.of("+03:00")))
                .setCurrency("RUB")
                .setOrder("1-22-333")
                .setQrType(QRType.QR_DYNAMIC)
                .setQrExpirationDate(OffsetDateTime.now(ZoneOffset.of("+03:00")).plusDays(1))
                .setSbpMerchantId("MA0000002371").build();

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV1() + "/qr/register")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fastPay.getObjectMapper().writeValueAsString(expectedResponse))
                );

        QRInfoResponse realityResponse = fastPay.registrationQR(registrationRequest);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testCheckQR() throws JsonProcessingException, URISyntaxException {
        String qrId = "AD10000LIELKGQDK9TSAUC4MOQHTLRPU";
        QRInfoResponse expectedResponse = new QRInfoResponse(ResponseStatusCode.SUCCESS,
                null, "AD10000LIELKGQDK9TSAUC4MOQHTLRPU",
                "https://qr.nspk.ru/AD10000LIELKGQDK9TSAUC4MOQHTLRPU?type=02&bank=raiffei1test&sum=111000&cur=RUB&crc=277B",
                "https://test.ecom.raiffeisen.ru/api/sbp/v1/qr/AD10000LIELKGQDK9TSAUC4MOQHTLRPU/image");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV1() + "/qr/" + qrId + "/info")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + fastPayConfig.getSecretKey()))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fastPay.getObjectMapper().writeValueAsString(expectedResponse))
                );

        QRInfoResponse realityResponse = fastPay.checkQR(qrId);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testCheckPaymentStatus() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        String qrId = "AD10000LIELKGQDK9TSAUC4MOQHTLRPU";
        PaymentInfoResponse expectedResponse = new PaymentInfoResponse(null, BigDecimal.valueOf(1110),
                ResponseStatusCode.SUCCESS, null, OffsetDateTime.now(),
                "RUB", 0L,
                "1-22-333", PaymentStatus.SUCCESS,
                "AD10000LIELKGQDK9TSAUC4MOQHTLRPU", "MA0000002371",
                OffsetDateTime.now().plusDays(1), 2087);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV1() + "/qr/" + qrId + "/payment-info")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + fastPayConfig.getSecretKey()))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fastPay.getObjectMapper().writeValueAsString(expectedResponse))
                );

        PaymentInfoResponse realityResponse = fastPay.checkPaymentStatus(qrId);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testRefund() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        RefundRequest refundRequest = new RefundRequest(BigDecimal.valueOf(100), "1-22-333", "1-22-333r", null);
        RefundInfoResponse expectedResponse = new RefundInfoResponse(ResponseStatusCode.SUCCESS, null, BigDecimal.valueOf(1100), RefundStatus.COMPLETED);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV1() + "/refund")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + fastPayConfig.getSecretKey()))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fastPay.getObjectMapper().writeValueAsString(expectedResponse))
                );

        RefundInfoResponse realityResponse = fastPay.refund(refundRequest);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void checkRefundStatus() throws JsonProcessingException, URISyntaxException, BankCommunicationException {
        final String REFUND_ORDER = "1-22-333r";
        RefundInfoResponse expectedResponse = new RefundInfoResponse(ResponseStatusCode.SUCCESS, null, BigDecimal.valueOf(1100), RefundStatus.COMPLETED);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV1() + "/refund/" + REFUND_ORDER)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + fastPayConfig.getSecretKey()))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fastPay.getObjectMapper().writeValueAsString(expectedResponse))
                );

        RefundInfoResponse realityResponse = fastPay.checkRefundStatus(REFUND_ORDER);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testCancelQrOk() throws URISyntaxException, BankCommunicationException {
        String qrId = "AD10000LIELKGQDK9TSAUC4MOQHTLRPU";

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV2() + "/qrs/" + qrId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Bearer " + fastPayConfig.getSecretKey()))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                );

        fastPay.cancelQr(qrId);
        // проверяем случай body == null
        mockServer.verify();
    }

    @Test
    public void testCancelQrBadRequestWithErrorCode() throws URISyntaxException, BankCommunicationException, JsonProcessingException {
        String qrId = "AD10000LIELKGQDK9TSAUC4MOQHTLRPU";
        CancelQrResponse expectedResponse = new CancelQrResponse(ResponseStatusCode.WRONG_QR, "Нельзя сменить статус QR-кода с EXPIRED на CANCELLED");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV2() + "/qrs/" + qrId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Bearer " + fastPayConfig.getSecretKey()))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fastPay.getObjectMapper().writeValueAsString(expectedResponse))
                );

        fastPay.cancelQr(qrId);
        // проверяем случай, когда вернулась особенная ошибка, но метод не падает
        mockServer.verify();
    }

    @Test(expected = BankCommunicationException.class)
    public void testCancelQrBadRequestWithoutErrorCode() throws URISyntaxException, BankCommunicationException, JsonProcessingException {
        String qrId = "AD10000LIELKGQDK9TSAUC4MOQHTLRPU";
        CancelQrResponse expectedResponse = new CancelQrResponse(ResponseStatusCode.UNKNOWN, "Нельзя!");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(fastPayConfig.getUrl().getUrlV2() + "/qrs/" + qrId)))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Bearer " + fastPayConfig.getSecretKey()))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fastPay.getObjectMapper().writeValueAsString(expectedResponse))
                );

        fastPay.cancelQr(qrId);
        // проверяем случай, когда вернулась другая ошибка - метод падает
        mockServer.verify();
    }
}
