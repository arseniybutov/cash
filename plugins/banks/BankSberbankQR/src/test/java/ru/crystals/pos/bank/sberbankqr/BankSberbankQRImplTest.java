package ru.crystals.pos.bank.sberbankqr;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.crystals.pos.bank.BankQRProcessingCallback;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.sberbankqr.api.core.SberbankApi;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderOperationParamType;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderState;
import ru.crystals.pos.bank.sberbankqr.api.dto.cancel.OrderCancelQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.creation.OrderCreationQrRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.pay.PayRusClientQRRs;
import ru.crystals.pos.bank.sberbankqr.api.dto.status.OrderStatusRequestQrRs;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.utils.time.DateConverters;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.crystals.pos.bank.BankQRPlugin.MERCHANT_ID_PROP_NAME;
import static ru.crystals.pos.bank.BankQRPlugin.OPERATION_ID_PROP_NAME;
import static ru.crystals.pos.bank.BankQRPlugin.ORDER_ID_PROP_NAME;
import static ru.crystals.pos.bank.sberbankqr.BankSberbankQRImpl.SBERBANK_ID_QR_PROP_NAME;

public class BankSberbankQRImplTest {

    private final SberbankApi sberbankApi = mock(SberbankApi.class);

    private BankSberbankQRImpl sberbankQrPlugin;

    private static final ZonedDateTime RESP_TIME = ZonedDateTime.parse("2020-06-22T16:50:00.000+03:00[Europe/Moscow]");
    private static final ZonedDateTime OPERATION_TIME = ZonedDateTime.parse("2020-06-22T16:51:00.000+03:00[Europe/Moscow]");
    private static final Date EXPECTED_AUTH_DATE = DateConverters.toDate(OPERATION_TIME);
    private final DateTimeFormatter slipDateFormat = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    private static final String MERCHANT_ID = "merchant_id";
    private static final String TERMINAL_ID = "terminal_id";
    private static final String AUTH_CODE = "55000";
    private static final String RRN = "rrn_value";
    private static final String OPERATION_ID = "operation_id";
    private static final Long SALE_AMOUNT = 123L;

    @Before
    public void setUp() {
        sberbankQrPlugin = new BankSberbankQRImpl();
        sberbankQrPlugin.setSberbankApi(sberbankApi);
        sberbankQrPlugin.setIdQR("id_qr");
        sberbankQrPlugin.setTerminalId(TERMINAL_ID);
        sberbankQrPlugin.setMemberId("member_id");
    }

    @Test
    public void testSaleByCustomerQr() throws BankException {
        PayRusClientQRRs.Status status = new PayRusClientQRRs.Status();
        status.setRqTm(RESP_TIME);
        status.setOrderId("order_id");
        OrderOperationParamType param = new OrderOperationParamType();
        param.setAuthCode(AUTH_CODE);
        param.setOperationId(OPERATION_ID);
        param.setResponseCode("00");
        param.setRrn(RRN);
        param.setOperationDateTime(OPERATION_TIME);
        status.setOrderOperationParam(param);
        status.setErrorCode("000000");
        status.setMerchantId(MERCHANT_ID);
        when(sberbankApi.pay(anyString(), anyInt())).thenReturn(status);

        SaleData saleData = new SaleData();
        saleData.setCustomerQR("someQR");
        saleData.setAmount(SALE_AMOUNT);

        AuthorizationData authData = sberbankQrPlugin.saleByCustomerQR(saleData);

        AuthorizationData expectedAuthData = new AuthorizationData();
        expectedAuthData.setAmount(SALE_AMOUNT);
        expectedAuthData.setDate(EXPECTED_AUTH_DATE);
        expectedAuthData.setRefNumber(RRN);
        expectedAuthData.setAuthCode(AUTH_CODE);
        expectedAuthData.getExtendedData().put(ORDER_ID_PROP_NAME, "order_id");
        expectedAuthData.getExtendedData().put(OPERATION_ID_PROP_NAME, OPERATION_ID);
        expectedAuthData.getExtendedData().put(MERCHANT_ID_PROP_NAME, MERCHANT_ID);
        expectedAuthData.getExtendedData().put(SBERBANK_ID_QR_PROP_NAME, "id_qr");
        expectedAuthData.setOperationType(BankOperationType.SALE);
        expectedAuthData.setStatus(true);
        expectedAuthData.setTerminalId(TERMINAL_ID);
        addSlip(expectedAuthData, "Оплата");
        assertEquals(expectedAuthData, authData);

        verify(sberbankApi).pay("someQR", SALE_AMOUNT.intValue());
    }

    @Test
    public void testRefundByQr() throws BankException {
        OrderCancelQrRs.Status status = new OrderCancelQrRs.Status();
        status.setRqTm(RESP_TIME);
        status.setOrderId("order_id");
        status.setAuthCode("55000");
        status.setOperationId("operation_id");
        status.setErrorCode("000000");
        status.setRrn(RRN);
        status.setOperationDateTime(OPERATION_TIME);
        when(sberbankApi.cancel(anyString(), anyString(), anyString(), anyInt(), anyString())).thenReturn(status);

        RefundData refundData = new RefundData();
        refundData.setRefNumber(RRN);
        refundData.setAuthCode(AUTH_CODE);
        refundData.setAmount(SALE_AMOUNT);
        refundData.getExtendedData().put(ORDER_ID_PROP_NAME, "order_id");
        refundData.getExtendedData().put(OPERATION_ID_PROP_NAME, OPERATION_ID);
        refundData.getExtendedData().put(SBERBANK_ID_QR_PROP_NAME, "orig_id_qr");
        refundData.getExtendedData().put(MERCHANT_ID_PROP_NAME, MERCHANT_ID);

        AuthorizationData authData = sberbankQrPlugin.refundByQR(refundData);

        AuthorizationData expectedAuthData = new AuthorizationData();
        expectedAuthData.setAmount(SALE_AMOUNT);
        expectedAuthData.setDate(EXPECTED_AUTH_DATE);
        expectedAuthData.setRefNumber(RRN);
        expectedAuthData.getExtendedData().put(ORDER_ID_PROP_NAME, "order_id");
        expectedAuthData.setAuthCode(AUTH_CODE);
        expectedAuthData.getExtendedData().put(OPERATION_ID_PROP_NAME, OPERATION_ID);
        expectedAuthData.getExtendedData().put(MERCHANT_ID_PROP_NAME, MERCHANT_ID);
        expectedAuthData.getExtendedData().put(SBERBANK_ID_QR_PROP_NAME, "id_qr");
        expectedAuthData.setOperationType(BankOperationType.REFUND);
        expectedAuthData.setStatus(true);
        expectedAuthData.setTerminalId(TERMINAL_ID);
        addSlip(expectedAuthData, "Возврат");
        assertEquals(expectedAuthData, authData);

        verify(sberbankApi).cancel("order_id", OPERATION_ID, "55000", 123, "orig_id_qr");
    }

    @Test
    public void testSaleByQR() throws BankException {
        OrderCreationQrRs.Status creationStatus = new OrderCreationQrRs.Status();
        creationStatus.setRqTm(RESP_TIME);
        creationStatus.setOrderId("order_id");

        when(sberbankApi.creation(SALE_AMOUNT.intValue())).thenReturn(creationStatus);

        OrderStatusRequestQrRs.Status status = new OrderStatusRequestQrRs.Status();
        status.setRqTm(RESP_TIME);
        status.setOrderId("order_id");
        status.setOrderState(OrderState.PAID);
        status.setErrorCode("000000");
        status.setMerchantId(MERCHANT_ID);
        OrderOperationParamType param = new OrderOperationParamType();
        param.setAuthCode(AUTH_CODE);
        param.setOperationId(OPERATION_ID);
        param.setResponseCode("00");
        param.setOperationSum(SALE_AMOUNT.intValue());
        param.setRrn(RRN);
        param.setOperationDateTime(OPERATION_TIME);
        status.setOrderOperationParams(Collections.singletonList(param));

        OrderStatusRequestQrRs response = new OrderStatusRequestQrRs();
        response.setStatus(status);
        ResponseEntity<OrderStatusRequestQrRs> statusResponse = new ResponseEntity<>(response, HttpStatus.OK);

        when(sberbankApi.statusWithRetries("order_id")).thenReturn(statusResponse);

        SaleData saleData = new SaleData();
        saleData.setCustomerQR("someQR");
        saleData.setAmount(SALE_AMOUNT);

        BankQRProcessingCallback callback = mock(BankQRProcessingCallback.class);
        when(callback.isStopped()).thenReturn(false);
        AuthorizationData authData = sberbankQrPlugin.saleByQR(saleData, callback);

        AuthorizationData expectedAuthData = new AuthorizationData();
        expectedAuthData.setAmount(SALE_AMOUNT);
        expectedAuthData.setDate(EXPECTED_AUTH_DATE);
        expectedAuthData.setRefNumber(RRN);
        expectedAuthData.getExtendedData().put(ORDER_ID_PROP_NAME, "order_id");
        expectedAuthData.setAuthCode(AUTH_CODE);
        expectedAuthData.getExtendedData().put(OPERATION_ID_PROP_NAME, OPERATION_ID);
        expectedAuthData.getExtendedData().put(SBERBANK_ID_QR_PROP_NAME, "id_qr");
        expectedAuthData.getExtendedData().put(MERCHANT_ID_PROP_NAME, MERCHANT_ID);
        expectedAuthData.setOperationType(BankOperationType.SALE);
        expectedAuthData.setStatus(true);
        expectedAuthData.setTerminalId(TERMINAL_ID);
        addSlip(expectedAuthData, "Оплата");
        assertEquals(expectedAuthData, authData);

        verify(sberbankApi).creation(123);
        verify(sberbankApi, times(1)).statusWithRetries("order_id");
    }

    private void addSlip(AuthorizationData authData, String operationTypeName) {
        List<List<String>> slips = new ArrayList<>();
        slips.add(new ArrayList<>(Arrays.asList(
                "Оплата по QR-коду",
                slipDateFormat.format(DateConverters.toLocalDateTime(EXPECTED_AUTH_DATE)),
                operationTypeName,
                "Терминал: " + TERMINAL_ID,
                "Сумма (Руб): " + CurrencyUtil.convertMoneyToText(SALE_AMOUNT),
                "Комиссия за операцию - 0 Руб",
                "RRN операции: " + RRN,
                "Код авторизации: " + AUTH_CODE,
                "Идентификатор операции: " + OPERATION_ID
        )));
        authData.setSlips(slips);
    }
}