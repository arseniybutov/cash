package ru.crystals.pos.bank.raiffeisensbp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.BankQRPlugin;
import ru.crystals.pos.bank.BankQRProcessingCallback;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.raiffeisensbp.api.request.QRType;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RefundRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.request.RegistrationQRRequest;
import ru.crystals.pos.bank.raiffeisensbp.api.response.PaymentInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.QRInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.RefundInfoResponse;
import ru.crystals.pos.bank.raiffeisensbp.api.response.ResponseStatusCode;
import ru.crystals.pos.bank.raiffeisensbp.api.status.PaymentStatus;
import ru.crystals.pos.bank.raiffeisensbp.api.status.RefundStatus;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.pos.property.Properties;
import ru.crystals.utils.time.DateConverters;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankRaiffeisenSBPTest {

    private final String QR_ID = "AD10000LIELKGQDK9TSAUC4MOQHTLRPU";
    private final String QR_URL = "https://test.ecom.raiffeisen.ru/api/sbp/v1/qr/AD10000LIELKGQDK9TSAUC4MOQHTLRPU/image";
    private final String PAYLOAD = "https://qr.nspk.ru/AD10000LIELKGQDK9TSAUC4MOQHTLRPU?type=02&bank=raiffei1test&sum=111000&cur=RUB&crc=277B";

    @Mock
    private FastPaymentSystem fastPaymentSystem;

    @Mock
    private SBPTimeSupplier sbpTimeSupplier;

    @Mock
    private Properties properties;

    private SBPConfig config = new SBPConfig();

    @InjectMocks
    private BankRaiffeisenSBPImpl bankRaiffeisenSBP;

    private BankQRProcessingCallback callback = Mockito.mock(BankQRProcessingCallback.class);

    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private static final String PROVIDER = "raiffeisen_sbp_sp";
    private OffsetDateTime now;
    private LocalDateTime nowLocal;
    private String orderId;


    @Before
    public void start() {
        bankRaiffeisenSBP = new BankRaiffeisenSBPImpl(properties, config, sbpTimeSupplier, fastPaymentSystem);

        config.setUrl("http://localhost:8080");
        config.setSecretKey("test_secret_key");
        config.setAccount("40702810200001448172");
        config.setSbpMerchantId("MA0000002371");
        config.setQrExpiration(5L);

        stopped.set(false);

        nowLocal = LocalDateTime.parse("2021-04-15T13:12:12");
        now = OffsetDateTime.now().with(nowLocal);
        orderId = UUID.randomUUID().toString();
        when(sbpTimeSupplier.getNowTime()).thenReturn(now);
        when(sbpTimeSupplier.getIdForOperation()).thenReturn(orderId);

        when(properties.getCashNumber()).thenReturn(4L);
        when(properties.getShopIndex()).thenReturn(1032L);
    }

    @Test
    public void saleByQRWithSuccessTest() throws BankException {
        SaleData saleData = createSaleData();
        QRInfoResponse expectedQRResponse = new QRInfoResponse(ResponseStatusCode.SUCCESS, null, QR_ID, PAYLOAD, QR_URL);
        when(fastPaymentSystem.registrationQR(any(RegistrationQRRequest.class))).thenReturn(expectedQRResponse);
        when(fastPaymentSystem.checkPaymentStatus(QR_ID))
                .thenReturn(createPaymentInfoResponse())
                .thenReturn(createPaymentInfoResponse(PaymentStatus.IN_PROGRESS))
                .thenReturn(createPaymentInfoResponse(PaymentStatus.SUCCESS));

        AuthorizationData data = bankRaiffeisenSBP.saleByQR(saleData, callback);

        verifyEventShowQR(saleData);
        verify(callback).eventHideQRCode();

        assertEquals(saleData.getAmount(), data.getAmount());
        assertEquals(saleData.getCurrencyCode(), data.getCurrencyCode());
        assertEquals(saleData.getBankId(), data.getBankid());
        assertEquals(saleData.getCashTransId(), data.getCashTransId());
        assertEquals(Date.from(now.toInstant()), data.getDate());
        assertTrue(data.isStatus());
        assertNotNull(data.getExtendedData().get(BankQRPlugin.ORDER_ID_PROP_NAME));

        final ArgumentCaptor<RegistrationQRRequest> captor = ArgumentCaptor.forClass(RegistrationQRRequest.class);
        verify(fastPaymentSystem).registrationQR(captor.capture());
        final RegistrationQRRequest actualRequest = captor.getValue();
        RegistrationQRRequest expected = RegistrationQRRequest.builder()
                .setAccount("40702810200001448172")
                .setAmount(new BigDecimal("10.00"))
                .setCreateDate(now)
                .setCurrency("RUB")
                .setOrder(orderId)
                .setQrType(QRType.QR_DYNAMIC)
                .setQrExpirationDate(now.plusMinutes(5L))
                .setSbpMerchantId("MA0000002371")
                .build();

        assertEquals(expected, actualRequest);
    }

    @Test
    public void saleByQRWithPaymentDetailsSuccessTest() throws BankException {
        config.setAdditionalInfo("%SHOP_INDEX%.%CASH_NUMBER%.%DATE_TIME%;доп. информация для комментариев в реестре");
        config.setPaymentDetails("%SHOP_INDEX%.%CASH_NUMBER%.%DATE_TIME%;доп. информация для комментариев в выписке");

        SaleData saleData = createSaleData();
        QRInfoResponse expectedQRResponse = new QRInfoResponse(ResponseStatusCode.SUCCESS, null, QR_ID, PAYLOAD, QR_URL);
        when(fastPaymentSystem.registrationQR(any(RegistrationQRRequest.class))).thenReturn(expectedQRResponse);
        when(fastPaymentSystem.checkPaymentStatus(QR_ID))
                .thenReturn(createPaymentInfoResponse())
                .thenReturn(createPaymentInfoResponse(PaymentStatus.IN_PROGRESS))
                .thenReturn(createPaymentInfoResponse(PaymentStatus.SUCCESS));

        AuthorizationData data = bankRaiffeisenSBP.saleByQR(saleData, callback);

        verifyEventShowQR(saleData);
        verify(callback).eventHideQRCode();

        assertEquals(saleData.getAmount(), data.getAmount());
        assertEquals(saleData.getCurrencyCode(), data.getCurrencyCode());
        assertEquals(DateConverters.toDate(now.toLocalDateTime()), data.getDate());
        assertTrue(data.isStatus());
        assertNotNull(data.getExtendedData().get(BankQRPlugin.ORDER_ID_PROP_NAME));

        final ArgumentCaptor<RegistrationQRRequest> captor = ArgumentCaptor.forClass(RegistrationQRRequest.class);
        verify(fastPaymentSystem).registrationQR(captor.capture());
        final RegistrationQRRequest actualRequest = captor.getValue();
        RegistrationQRRequest expected = RegistrationQRRequest.builder()
                .setAccount("40702810200001448172")
                .setAmount(new BigDecimal("10.00"))
                .setCreateDate(now)
                .setCurrency("RUB")
                .setOrder(orderId)
                .setQrType(QRType.QR_DYNAMIC)
                .setQrExpirationDate(now.plusMinutes(5L))
                .setSbpMerchantId("MA0000002371")
                .setPaymentDetails("1032.4.2021-04-15T13:12:12;доп. информация для комментариев в выписке")
                .setAdditionalInfo("1032.4.2021-04-15T13:12:12;доп. информация для комментариев в реестре")
                .build();

        assertEquals(expected, actualRequest);
    }

    private void verifyEventShowQR(SaleData saleData) {
        verify(callback).eventShowQRCode(PAYLOAD, PictureId.QR_PAY_RAIFFEISEN_SBP, saleData.getAmount(), this.nowLocal, this.nowLocal.plusMinutes(5L), PROVIDER);
    }

    @Test
    public void saleByQRWithDeclinedTest() throws BankException {
        SaleData saleData = createSaleData();
        QRInfoResponse expectedQRResponse = new QRInfoResponse(ResponseStatusCode.SUCCESS, null, QR_ID, PAYLOAD, QR_URL);
        when(fastPaymentSystem.registrationQR(any(RegistrationQRRequest.class))).thenReturn(expectedQRResponse);
        when(fastPaymentSystem.checkPaymentStatus(QR_ID))
                .thenReturn(createPaymentInfoResponse())
                .thenReturn(createPaymentInfoResponse(PaymentStatus.IN_PROGRESS))
                .thenReturn(createPaymentInfoResponse(PaymentStatus.DECLINED, "Отказано в операции"));

        AuthorizationData data = bankRaiffeisenSBP.saleByQR(saleData, callback);
        verifyEventShowQR(saleData);
        verify(callback).eventHideQRCode();

        assertEquals(saleData.getAmount(), data.getAmount());
        assertEquals(saleData.getCurrencyCode(), data.getCurrencyCode());
        assertEquals(saleData.getBankId(), data.getBankid());
        assertEquals(saleData.getCashTransId(), data.getCashTransId());
        assertEquals(Date.from(now.toInstant()), data.getDate());
        Assert.assertFalse(data.isStatus());
        assertEquals(ResBundleBankRaiffeisenSBP.getString("STATUS_DECLINED") + ": Отказано в операции", data.getMessage());
    }

    @Test
    public void saleByQRTryStopWithSuccessTest() throws BankException {
        SaleData saleData = createSaleData();
        QRInfoResponse expectedQRResponse = new QRInfoResponse(ResponseStatusCode.SUCCESS, null, QR_ID, PAYLOAD, QR_URL);
        PaymentInfoResponse expectedPaymentResponse = createPaymentInfoResponse();
        when(fastPaymentSystem.registrationQR(any(RegistrationQRRequest.class))).thenReturn(expectedQRResponse);
        when(fastPaymentSystem.checkPaymentStatus(QR_ID))
                .thenReturn(expectedPaymentResponse)
                .thenReturn(createPaymentInfoResponse(PaymentStatus.IN_PROGRESS))
                .thenReturn(createPaymentInfoResponse(PaymentStatus.SUCCESS));
        when(callback.isStopped()).thenReturn(false);

        AuthorizationData data = bankRaiffeisenSBP.saleByQR(saleData, callback);
        verifyEventShowQR(saleData);
        verify(callback).eventHideQRCode();

        assertEquals(saleData.getAmount(), data.getAmount());
        assertEquals(saleData.getCurrencyCode(), data.getCurrencyCode());
        assertEquals(saleData.getBankId(), data.getBankid());
        assertEquals(saleData.getCashTransId(), data.getCashTransId());
        assertEquals(Date.from(now.toInstant()), data.getDate());
        assertTrue(data.isStatus());
        assertNotNull(data.getExtendedData().get(BankQRPlugin.ORDER_ID_PROP_NAME));
    }

    @Test(expected = BankException.class)
    public void saleByQRTryStopWithFailTest() throws BankException {
        SaleData saleData = createSaleData();
        QRInfoResponse expectedQRResponse = new QRInfoResponse(ResponseStatusCode.SUCCESS, null, QR_ID, PAYLOAD, QR_URL);
        PaymentInfoResponse expectedPaymentResponse = createPaymentInfoResponse();
        when(fastPaymentSystem.registrationQR(any(RegistrationQRRequest.class))).thenReturn(expectedQRResponse);
        when(fastPaymentSystem.checkPaymentStatus(QR_ID)).thenReturn(expectedPaymentResponse);
        when(callback.isStopped()).thenReturn(false, true);
        bankRaiffeisenSBP.saleByQR(saleData, callback);
    }

    @Test
    public void refundByQRWithSuccessTest() throws BankException {
        config.setPaymentDetails("%SHOP_INDEX%.%CASH_NUMBER%.%DATE_TIME%;доп. информация для комментариев в выписке");
        final String orderId = UUID.randomUUID().toString();
        final String refundId = this.orderId;

        final Date currentDate = new Date();
        RefundData refundData = createRefundData(orderId, currentDate);
        RefundInfoResponse expectedRefundResponse = createRefundInfoResponse();
        when(fastPaymentSystem.refund(any(RefundRequest.class))).thenReturn(expectedRefundResponse);
        when(fastPaymentSystem.checkRefundStatus(anyString()))
                .thenReturn(expectedRefundResponse)
                .thenReturn(createRefundInfoResponse(RefundStatus.COMPLETED));
        AuthorizationData data = bankRaiffeisenSBP.refundByQR(refundData);
        assertEquals(refundData.getAmount(), data.getAmount());
        assertEquals(refundData.getCurrencyCode(), data.getCurrencyCode());
        assertEquals(refundData.getBankId(), data.getBankid());
        assertEquals(refundData.getCashTransId(), data.getCashTransId());
        assertEquals(currentDate, data.getDate());
        assertTrue(data.isStatus());
        assertNotNull(data.getExtendedData().get(BankQRPlugin.ORDER_ID_PROP_NAME));

        final ArgumentCaptor<RefundRequest> captor = ArgumentCaptor.forClass(RefundRequest.class);
        verify(fastPaymentSystem).refund(captor.capture());
        final RefundRequest actualRequest = captor.getValue();
        RefundRequest expected = new RefundRequest(BigDecimal.valueOf(10.00), orderId, refundId,
                "1032.4.2021-04-15T13:12:12;доп. информация для комментариев в выписке");

        assertEquals(expected, actualRequest);
    }

    @Test
    public void refundByQRWithDeclinedTest() throws BankException {
        final UUID orderId = UUID.randomUUID();
        final Date currentDate = new Date();
        RefundData refundData = createRefundData(orderId.toString(), currentDate);
        RefundInfoResponse expectedRefundResponse = createRefundInfoResponse();
        when(fastPaymentSystem.refund(any(RefundRequest.class))).thenReturn(expectedRefundResponse);
        when(fastPaymentSystem.checkRefundStatus(anyString()))
                .thenReturn(expectedRefundResponse)
                .thenReturn(createRefundInfoResponse(RefundStatus.DECLINED));
        AuthorizationData data = bankRaiffeisenSBP.refundByQR(refundData);
        assertEquals(refundData.getAmount(), data.getAmount());
        assertEquals(refundData.getCurrencyCode(), data.getCurrencyCode());
        assertEquals(refundData.getBankId(), data.getBankid());
        assertEquals(refundData.getCashTransId(), data.getCashTransId());
        assertEquals(currentDate, data.getDate());
        Assert.assertFalse(data.isStatus());
        assertEquals(ResBundleBankRaiffeisenSBP.getString("STATUS_DECLINED"), data.getMessage());
    }

    private RefundData createRefundData(String orderId, Date date) {
        RefundData refundData = new RefundData();
        refundData.setAmount(1000L);
        refundData.setCurrencyCode("RUB");
        refundData.setCashTransId(1009051920L);
        refundData.setBankId("raiffeisensbp");
        refundData.getExtendedData().put(BankQRPlugin.ORDER_ID_PROP_NAME, orderId);
        refundData.setOriginalSaleTransactionDate(date);
        return refundData;
    }

    private SaleData createSaleData() {
        SaleData saleData = new SaleData();
        saleData.setAmount(1000L);
        saleData.setCurrencyCode("RUB");
        saleData.setCashTransId(1009051920L);
        saleData.setBankId("raiffeisensbp");
        return saleData;
    }

    private PaymentInfoResponse createPaymentInfoResponse() {
        return createPaymentInfoResponse(PaymentStatus.NO_INFO);
    }

    private PaymentInfoResponse createPaymentInfoResponse(PaymentStatus paymentStatus, String message) {
        return new PaymentInfoResponse(null, BigDecimal.valueOf(1110),
                ResponseStatusCode.SUCCESS, message, OffsetDateTime.now(),
                "RUB", 0L,
                "1-22-333", paymentStatus,
                "AD10000LIELKGQDK9TSAUC4MOQHTLRPU", "MA0000002371",
                OffsetDateTime.now().plusDays(1), 2087);
    }

    private PaymentInfoResponse createPaymentInfoResponse(PaymentStatus paymentStatus) {
        return new PaymentInfoResponse(null, BigDecimal.valueOf(1110),
                ResponseStatusCode.SUCCESS, null, OffsetDateTime.now(),
                "RUB", 0L,
                "1-22-333", paymentStatus,
                "AD10000LIELKGQDK9TSAUC4MOQHTLRPU", "MA0000002371",
                OffsetDateTime.now().plusDays(1), 2087);
    }

    private RefundInfoResponse createRefundInfoResponse() {
        return createRefundInfoResponse(RefundStatus.IN_PROGRESS);
    }

    private RefundInfoResponse createRefundInfoResponse(RefundStatus refundStatus) {
        return new RefundInfoResponse(ResponseStatusCode.SUCCESS, null, BigDecimal.valueOf(1100), refundStatus);
    }
}
