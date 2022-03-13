package ru.crystals.pos.bank.tinkoffsbp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.testng.Assert;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.commonsbpprovider.StateOfRequest;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfFullAmountDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfPartAmountRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RegistrationQRRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.PaymentInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.QRInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.RefundInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;
import ru.crystals.pos.bank.tinkoffsbp.api.response.InitResponse;
import ru.crystals.pos.bank.tinkoffsbp.api.response.QRInfoResponse;
import ru.crystals.pos.bank.tinkoffsbp.api.response.RefundResponse;
import ru.crystals.pos.bank.tinkoffsbp.api.response.RegistrationQRResponse;
import ru.crystals.pos.bank.tinkoffsbp.api.response.ResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(MockitoJUnitRunner.class)
public class BankTinkoffSBPProviderTest {

    @InjectMocks
    private BankTinkoffSBPProvider bankTinkoffSBPProvider;

    private RequestExecutor requestExecutor;

    private TinkoffSBPConfig sbpConfig;

    private MockRestServiceServer mockServer;

    @Before
    public void start() {
        MockitoAnnotations.initMocks(this);

        sbpConfig = new TinkoffSBPConfig();

        sbpConfig.setUrl("http://localhost:8080");
        sbpConfig.setMaxNumberOfRetries("5");
        sbpConfig.setQrExpiration(5L);
        sbpConfig.setPassword("xtiprrmg73jkiklh");
        sbpConfig.setTerminalKey("1605280872049");
        sbpConfig.setSbpMerchantId("1605280872049");

        requestExecutor = new RequestExecutor(sbpConfig);

        mockServer = MockRestServiceServer
                .bindTo(requestExecutor.getRestTemplate())
                .bufferContent()
                .build();

        bankTinkoffSBPProvider = spy(bankTinkoffSBPProvider);
        bankTinkoffSBPProvider.setConfig(sbpConfig);
        bankTinkoffSBPProvider.setRequestExecutor(requestExecutor);
    }

    @Test
    public void testRegistrationQr() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        QRInfoResponseDTO expectedResponse = new QRInfoResponseDTO("700000162012",
                "https://qr.nspk.ru/AD10006269TNA0JH9378574UQ65OM04Q?type=02&bank=100000000015&sum=1000&cur=RUB&crc=AA34", HttpStatus.OK);

        RegistrationQRResponse tinkoffResponse = new RegistrationQRResponse("1605280872049", "AD10006269TNA0JH9378574UQ65OM04Q", true,
                "https://qr.nspk.ru/AD10006269TNA0JH9378574UQ65OM04Q?type=02&bank=100000000015&sum=1000&cur=RUB&crc=AA34",
                "700000162012", "0", null, null);

        InitResponse initResponse = new InitResponse(sbpConfig.getTerminalKey(), 500L, "AD10006269TNA0JH9378574UQ65OM04Q", true, "NEW",
                700000162012L, "0", null, null);

        RegistrationQRRequestDTO registrationRequest = RegistrationQRRequestDTO.builder()
                .setAmount(500)
                .setCurrency("RUB")
                .setSbpMerchantId(sbpConfig.getSbpMerchantId())
                .build();

        //TODO как избавиться от spy?
        doReturn(initResponse).when(bankTinkoffSBPProvider).initPaymentSession(registrationRequest);


        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(sbpConfig.getUrl() + "/v2/GetQr")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(tinkoffResponse))
                );


        QRInfoResponseDTO realityResponse = bankTinkoffSBPProvider.registrationQR(registrationRequest);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testGetPaymentStatus() throws JsonProcessingException, URISyntaxException, BankCommunicationException {
        String qrId = "700000138546";

        QRInfoResponse expectedPaymentInfoResponse = new QRInfoResponse("1605280872049", "1234567", true, ResponseStatus.FORM_SHOWED, 15664L, "0", 555L, null, null);
        StateOfRequest stateOfRequest = new StateOfRequest(sbpConfig.getDelayInSeconds(), sbpConfig.getMaxNumberOfRequest(), sbpConfig.getMaxNumberOfRetries());

        PaymentInfoResponseDTO expectedResponse = new PaymentInfoResponseDTO(Status.NOT_STARTED, "0", "15664", null);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/v2/GetState"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedPaymentInfoResponse))
                );

        PaymentInfoResponseDTO realityResponse = bankTinkoffSBPProvider.getPaymentStatus(qrId, stateOfRequest);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testRefund() throws BankCommunicationException, URISyntaxException, JsonProcessingException {
        RefundOfFullAmountDTO refundDTO = new RefundOfFullAmountDTO("37419608", "1234567");

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setOperationId("37419608")
                .setAmount(110)
                .setStatus(Status.PROCESSING)
                .setId("1234567")
                .build();

        RefundResponse expectedRefundInfoResponse = new RefundResponse("1605280872049", true, ResponseStatus.REFUNDING, 37419608L, "0", "1234567", 110L, 0L, null, null);


        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/v2/Cancel"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankTinkoffSBPProvider.refund(refundDTO);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testRefundPartOfAmount() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        RefundOfPartAmountRequestDTO refundDTO = new RefundOfPartAmountRequestDTO("37419608", "1234567", 50);

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setOperationId("37419608")
                .setAmount(50)
                .setStatus(Status.PROCESSING)
                .setId("1234567")
                .build();

        RefundResponse expectedRefundInfoResponse = new RefundResponse("1605280872049", true, ResponseStatus.REFUNDING, 37419608L, "0", "1234567", 110L, 60L, null, null);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/v2/Cancel"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankTinkoffSBPProvider.refundPartOfAmount(refundDTO);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }


    @Test
    public void testGetRefundStatus() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        String qrId = "700000138546";

        QRInfoResponse expectedPaymentInfoResponse = new QRInfoResponse("1605280872049", "1234567", true, ResponseStatus.REFUNDED, 37419608L, "0", 555L, null, null);
        StateOfRequest stateOfRequest = new StateOfRequest(sbpConfig.getDelayInSeconds(), sbpConfig.getMaxNumberOfRequest(), sbpConfig.getMaxNumberOfRetries());

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setStatus(Status.SUCCESS)
                .setOperationId("37419608")
                .build();

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/v2/GetState"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedPaymentInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankTinkoffSBPProvider.getRefundStatus(qrId, stateOfRequest);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testInitPaymentSession() throws JsonProcessingException, URISyntaxException, BankCommunicationException {
        InitResponse expectedResponse = new InitResponse(sbpConfig.getTerminalKey(), 500L, "1234567", true, "NEW",
                700000162012L, "0", null, null);

        RegistrationQRRequestDTO registrationRequest = RegistrationQRRequestDTO.builder()
                .setAmount(500)
                .setCurrency("RUB")
                .setOrderId("1234567")
                .setSbpMerchantId(sbpConfig.getSbpMerchantId())
                .build();

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(sbpConfig.getUrl() + "/v2/Init/")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedResponse))
                );

        InitResponse realityResponse = bankTinkoffSBPProvider.initPaymentSession(registrationRequest);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }
}
