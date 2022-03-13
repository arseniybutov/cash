package ru.crystals.pos.bank.gazpromsbp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
import ru.crystals.pos.bank.gazpromsbp.api.response.InitRefundResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.PaymentInfoData;
import ru.crystals.pos.bank.gazpromsbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.PaymentStatus;
import ru.crystals.pos.bank.gazpromsbp.api.response.ProcessRefundResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.QRData;
import ru.crystals.pos.bank.gazpromsbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.RefundStatus;
import ru.crystals.pos.bank.gazpromsbp.api.response.RegistrationQRResponse;
import ru.crystals.pos.bank.gazpromsbp.api.response.RegistrationQRStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(MockitoJUnitRunner.class)
public class BankGazpromSBPProviderTest {

    @InjectMocks
    BankGazpromSBPProvider bankGazpromSBPProvider;

    private RequestExecutor requestExecutor;

    private GazpromSBPConfig sbpConfig;

    private MockRestServiceServer mockServer;


    @Before
    public void start() {
        sbpConfig = new GazpromSBPConfig();

        sbpConfig.setUrl("http://localhost:8080");
        sbpConfig.setQrExpiration(5L);

        sbpConfig.setPassword("R2K9Im3ywR5D2m");
        sbpConfig.setSbpMerchantId("MA0000084118");
        sbpConfig.setAccount("40702810293001099999");
        sbpConfig.setBrandName("brandName");
        sbpConfig.setLegalId("LA0000006784");

        requestExecutor = new RequestExecutor(sbpConfig);

        mockServer = MockRestServiceServer
                .bindTo(requestExecutor.getRestTemplate())
                .bufferContent()
                .build();

        bankGazpromSBPProvider.setConfig(sbpConfig);
        bankGazpromSBPProvider.setRequestExecutor(requestExecutor);
    }

    @Test
    public void testRegistrationQr() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        QRInfoResponseDTO expectedResponse = new QRInfoResponseDTO("AD10001246OADML79D5OP9279JLPI2V6",
                "https://qr.nspk.ru/AD10001246OADML79D5OP9279JLPI2V6?type=02&bank=100000000001&sum=253&cur=RUB&crc=D959", Status.SUCCESS);


        QRData data = new QRData("AD10001246OADML79D5OP9279JLPI2V6", "https://qr.nspk.ru/AD10001246OADML79D5OP9279JLPI2V6?type=02&bank=100000000001&sum=253&cur=RUB&crc" +
                "=D959", RegistrationQRStatus.CREATED);
        RegistrationQRResponse gazpromResponse = new RegistrationQRResponse("000", "Запрос обработан успешно", "000000069055",
                data);

        RegistrationQRRequestDTO registrationRequest = RegistrationQRRequestDTO.builder()
                .setAmount(500)
                .setCurrency("RUB")
                .setSbpMerchantId(sbpConfig.getSbpMerchantId())
                .build();

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(sbpConfig.getUrl() + "/merchant/qrc-data")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(gazpromResponse))
                );

        QRInfoResponseDTO realityResponse = bankGazpromSBPProvider.registrationQR(registrationRequest);
        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testGetPaymentStatus() throws JsonProcessingException, URISyntaxException, BankCommunicationException {
        String qrId = "AD10001246OADML79D5OP9279JLPI2V6";
        List<PaymentInfoData> data = new ArrayList<>();
        data.add(new PaymentInfoData("AD10001246OADML79D5OP9279JLPI2V6", "RQ00000", "Запрос обработан успешно", PaymentStatus.ACCEPTED, "000000069057"));

        PaymentInfoResponse expectedPaymentInfoResponse = new PaymentInfoResponse("000", "Запрос обработан успешно", "000000069059", data);
        StateOfRequest stateOfRequest = new StateOfRequest(sbpConfig.getDelayInSeconds(), sbpConfig.getMaxNumberOfRequest(), sbpConfig.getMaxNumberOfRetries());

        PaymentInfoResponseDTO expectedResponse = new PaymentInfoResponseDTO(Status.SUCCESS, "Запрос обработан успешно", "000000069057", null);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/payment/v1/qrc-status"))))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedPaymentInfoResponse))
                );

        PaymentInfoResponseDTO realityResponse = bankGazpromSBPProvider.getPaymentStatus(qrId, stateOfRequest);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testInitRefund() throws JsonProcessingException, URISyntaxException, BankCommunicationException {
        String operationId = "000000069060";
        long amount = 110;

        InitRefundResponse expectedInitRefundResponse = new InitRefundResponse("000", "Запрос обработан успешно", "000000069060");
        String expectedResponse = "000000069060";

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/merchant/transfer/return/prepare"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedInitRefundResponse))
                );

        String realityResponse = bankGazpromSBPProvider.initRefund(operationId, amount);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testRefund() throws BankCommunicationException, URISyntaxException, JsonProcessingException {

        RefundOfFullAmountDTO refundDTO = new RefundOfFullAmountDTO("000000069060", "1234567", 110);

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setOperationId("000000069060")
                .setAmount(110)
                .setStatus(Status.SUCCESS)
                .setId("1234567")
                .setOperationTimestamp()
                .build();

        InitRefundResponse expectedInitRefundResponse = new InitRefundResponse("000", "Запрос обработан успешно", "000000069060");
        ProcessRefundResponse expectedRefundInfoResponse = new ProcessRefundResponse("000", "Запрос обработан успешно", "000000069060");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/merchant/transfer/return/prepare"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedInitRefundResponse))
                );

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/merchant/transfer/return/confirm"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankGazpromSBPProvider.refund(refundDTO);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }

    @Test
    public void testRefundPartOfAmount() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        RefundOfPartAmountRequestDTO refundDTO = new RefundOfPartAmountRequestDTO("000000069060", "1234567", 50);

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setOperationId("000000069060")
                .setAmount(50)
                .setStatus(Status.SUCCESS)
                .setId("1234567")
                .setOperationTimestamp()
                .build();

        InitRefundResponse expectedInitRefundResponse = new InitRefundResponse("000", "Запрос обработан успешно", "000000069060");
        ProcessRefundResponse expectedRefundInfoResponse = new ProcessRefundResponse("000", "Запрос обработан успешно", "000000069060");

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/merchant/transfer/return/prepare"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedInitRefundResponse))
                );

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/merchant/transfer/return/confirm"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankGazpromSBPProvider.refundPartOfAmount(refundDTO);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }


    @Test
    public void testGetRefundStatus() throws URISyntaxException, JsonProcessingException, BankCommunicationException {
        String qrId = "AD10001246OADML79D5OP9279JLPI2V6";

        RefundInfoResponse expectedRefundInfoResponse = new RefundInfoResponse("000", "Запрос обработан успешно", "000000069060", "QR_RETURNING", RefundStatus.PERFORMED);
        StateOfRequest stateOfRequest = new StateOfRequest(sbpConfig.getDelayInSeconds(), sbpConfig.getMaxNumberOfRequest(), sbpConfig.getMaxNumberOfRetries());

        RefundInfoResponseDTO expectedResponse = RefundInfoResponseDTO.builder()
                .setStatus(Status.SUCCESS)
                .setOperationId("000000069060")
                .build();

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI((sbpConfig.getUrl() + "/operation/info"))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestExecutor.getObjectMapper().writeValueAsString(expectedRefundInfoResponse))
                );

        RefundInfoResponseDTO realityResponse = bankGazpromSBPProvider.getRefundStatus(qrId, stateOfRequest);

        mockServer.verify();
        Assert.assertEquals(realityResponse, expectedResponse);
    }
}
