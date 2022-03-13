package ru.crystals.pos.bank.opensbp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.testng.Assert;
import ru.crystals.pos.bank.commonsbpprovider.StateOfRequest;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfFullAmountDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfPartAmountRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RegistrationQRRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.PaymentInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.QRInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.RefundInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.opensbp.api.request.Amount;
import ru.crystals.pos.bank.opensbp.api.response.Data;
import ru.crystals.pos.bank.opensbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.opensbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.opensbp.api.response.ResponseStatus;
import ru.crystals.pos.bank.opensbp.api.response.TokenResponse;
import ru.crystals.pos.bank.opensbp.api.status.PaymentOperationStatus;
import ru.crystals.pos.bank.opensbp.api.status.RefundStatus;
import ru.crystals.utils.time.Timer;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(MockitoJUnitRunner.class)
public class BankOpenSBPProviderTest {

    @InjectMocks
    private BankOpenSBPProvider bankOpenSBPProvider;


    private RequestExecutor requestExecutor;

    private OpenSBPConfig sbpConfig;

    private MockRestServiceServer mockServer;
    private MockRestServiceServer mockFormDataServer;

    @Mock
    private Timer tokenTimer;

    @Before
    public void start() {
        MockitoAnnotations.initMocks(this);

        sbpConfig = new OpenSBPConfig();
        sbpConfig.setUrl("http://localhost:8080");
        sbpConfig.setQrExpiration(5L);

        sbpConfig.setSecretKey("qwe123");
        sbpConfig.setAccount("LA0000008004");
        sbpConfig.setDelayInSeconds("5");
        sbpConfig.setMaxNumberOfRequest("50");
        sbpConfig.setMaxNumberOfRetries("5");
        sbpConfig.setSbpMerchantId("MA0000086684");

        requestExecutor = new RequestExecutor(sbpConfig);

        mockServer = MockRestServiceServer
                .bindTo(requestExecutor.getRestTemplate())
                .bufferContent()
                .build();

        mockFormDataServer = MockRestServiceServer
                .bindTo(requestExecutor.getRestTemplateFormData())
                .bufferContent()
                .build();

        bankOpenSBPProvider.setConfig(sbpConfig);
        bankOpenSBPProvider.setRequestExecutor(requestExecutor);

    }

    @Test
    public void testRegistrationQr() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        QRInfoResponseDTO expectedResponse = new QRInfoResponseDTO("AD10006269TNA0JH9378574UQ65OM04Q",
                "https://qr.nspk.ru/AD10006269TNA0JH9378574UQ65OM04Q?type=02&bank=100000000015&sum=1000&cur=RUB&crc=AA34", HttpStatus.OK);

        RegistrationQRRequestDTO registrationRequest = RegistrationQRRequestDTO.builder()
                .setAmount(111)
                .setCurrency("RUB")
                .setSbpMerchantId(sbpConfig.getSbpMerchantId())
                .build();

        when(tokenTimer.isNotExpired()).thenReturn(true);
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(sbpConfig.getUrl() + "/api/merchant/v1/qrc-data/")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedResponse))
                );

        QRInfoResponseDTO realityResponse = bankOpenSBPProvider.registrationQR(registrationRequest);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);

    }


    @Test
    public void testGetPaymentStatus() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        String qrId = "AD10007RBC8067C09P1O6TOTEJSH5SP3";
        StateOfRequest stateOfRequest = new StateOfRequest(sbpConfig.getDelayInSeconds(), sbpConfig.getMaxNumberOfRequest(), sbpConfig.getMaxNumberOfRetries());

        // генерация респонса Открытия, который затем мапится на responseDTO
        Data data = new Data("AD10001TEKOA11BB9DER6NBUCEIC2IQH", "RQ00000", "Запрос обработан успешно", PaymentOperationStatus.NTST, 15664, "2021-03-23T14:12:35.0830000+03:00");
        List<Data> dataList = new ArrayList<>();
        dataList.add(data);
        PaymentInfoResponse expectedPaymentInfoResponse = new PaymentInfoResponse(dataList);

        ZonedDateTime operationTimestamp = ZonedDateTime.parse("2021-03-23T14:12:35.0830000+03:00");
        PaymentInfoResponseDTO expectedResponse = new PaymentInfoResponseDTO(Status.NOT_STARTED, "Запрос обработан успешно", "15664", operationTimestamp);

        when(tokenTimer.isNotExpired()).thenReturn(true);
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/api/merchant/v1/qrc-status"))))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedPaymentInfoResponse))
                );


        PaymentInfoResponseDTO realityResponse = bankOpenSBPProvider.getPaymentStatus(qrId, stateOfRequest);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testRefund() throws BankCommunicationException, URISyntaxException, JsonProcessingException {
        RefundOfFullAmountDTO refundDTO = new RefundOfFullAmountDTO("37419608", "1234567");

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setOperationId("1234567")
                .setAmount(110)
                .setStatus(Status.PROCESSING)
                .setId("1234567")
                .build();

        RefundInfoResponse expectedRefundInfoResponse = new RefundInfoResponse("37419608", new ResponseStatus(RefundStatus.PROCESSING, "Принята", ""),
                new Amount(110, "RUB"), "1234567");

        when(tokenTimer.isNotExpired()).thenReturn(true);
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/api/merchant/v1/operations/return"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankOpenSBPProvider.refund(refundDTO);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testRefundPartOfAmount() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        RefundOfPartAmountRequestDTO refundDTO = new RefundOfPartAmountRequestDTO("37419608", "1234567", 55);

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setOperationId("1234567")
                .setAmount(55)
                .setStatus(Status.PROCESSING)
                .setId("1234567")
                .build();

        RefundInfoResponse expectedRefundInfoResponse = new RefundInfoResponse("37419608", new ResponseStatus(RefundStatus.PROCESSING, "Принята", ""),
                new Amount(55, "RUB"), "1234567");

        when(tokenTimer.isNotExpired()).thenReturn(true);
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/api/merchant/v1/operations/return"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankOpenSBPProvider.refundPartOfAmount(refundDTO);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testGetRefundStatus() throws URISyntaxException, BankCommunicationException, JsonProcessingException {
        String refundId = "a1b74f75fed6411e8ee82a5e5c2ad0b1";
        StateOfRequest stateOfRequest = new StateOfRequest(sbpConfig.getDelayInSeconds(), sbpConfig.getMaxNumberOfRequest(), sbpConfig.getMaxNumberOfRetries());

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setOperationId("1234567")
                .setAmount(55)
                .setStatus(Status.SUCCESS)
                .setId("1234567")
                .build();

        RefundInfoResponse expectedRefundInfoResponse = new RefundInfoResponse("37419608", new ResponseStatus(RefundStatus.COMPLETED, "Принята", ""),
                new Amount(55, "RUB"), "1234567");

        when(tokenTimer.isNotExpired()).thenReturn(true);
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/api/merchant/v1/operations/return/" + refundId))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankOpenSBPProvider.getRefundStatus(refundId, stateOfRequest);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void getTokenTest() throws JsonProcessingException, URISyntaxException, BankCommunicationException {
        when(tokenTimer.isNotExpired()).thenReturn(false);

        String expectedToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkNFNTk1REU4MTEwMDdNXQiOiJ6bGxkNkJFQWY0M2c3RHR5UDNtdkt6TzBnUUkiLCJ0eXAiOiJKV1QifQ";
        TokenResponse expectedTokenResponse = new TokenResponse(expectedToken, 3600, "Bearer");


        mockFormDataServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/am/ipslegals/connect/token"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedTokenResponse))
                );

        String realityToken = bankOpenSBPProvider.getToken();

        mockFormDataServer.verify();
        Assert.assertEquals(realityToken, expectedToken);
    }

}
