package ru.crystals.pos.bank.bpc;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.bank.BankDialogEvent;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.bpc.exceptions.BankBPCAutoReversalException;
import ru.crystals.pos.bank.bpc.exceptions.BankTimeoutException;
import ru.crystals.pos.bank.bpc.exceptions.DailyLogExpectedException;
import ru.crystals.pos.bank.bpc.serviceoperations.BPCServiceOperation;
import ru.crystals.pos.bank.bpc.serviceoperations.TestHostOperation;
import ru.crystals.pos.bank.bpc.serviceoperations.TestPinpadOperation;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankBPCServiceTest {
    private static final String TEST_CARD_NUMBER = "1234123412341234";
    private static final String ACCEPTED_STRING = "ОДОБРЕНО";
    private static final long TEST_AMOUNT = 12345L;
    private static final long TEST_AMOUNT_FOR_PARTIAL_REVERSAL = 10L;
    private static final String BYR_CURRENCY_CODE = "BYN";
    private static final String ANY_STRING = "ANY";
    private static final String TEST_REF_NUMBER = "123";
    private static final String TEST_RRN_NUMBER = "321";
    public static final String B5_RESPONSE_CODE = "B5";
    public static final String BB_RESPONSE_CODE = "BB";
    @Mock
    private TCPPortAdapter portAdapter;
    @Spy
    private BPCProperties bpcProperties;
    @Mock
    private BPCConnector connector;
    @Mock
    private DialogListener dialogListener;
    @Mock
    private BPCServiceOperation bpcServiceOperation;
    @Mock
    private ExecutorService dialogListenerExecutor;
    @Spy
    @InjectMocks
    private BankBPCServiceImpl service = new BankBPCServiceImpl();

    @Before
    public void before() throws IOException {
        doNothing().when(bpcProperties).load();
        doNothing().when(bpcProperties).writeProperties();
    }

    @Test
    public void testStart() throws Exception {

        // given
        doNothing().when(bpcProperties).load();

        // when
        service.start();

        // then
        verify(bpcProperties).load();
        verify(connector).connect();
        verify(dialogListener).start();
        verify(dialogListenerExecutor).execute(dialogListener);

        List<BPCServiceOperation> serviceOperations = service.getAvailableServiceOperations();
        List<Class> operationClasses = new ArrayList<>();
        for (BPCServiceOperation availableOperation : serviceOperations) {
            operationClasses.add(availableOperation.getClass());
        }
        assertThat(serviceOperations.size()).isEqualTo(3);
        assertThat(operationClasses).contains(TestHostOperation.class);
        assertThat(operationClasses).contains(TestPinpadOperation.class);
    }

    @Test(expected = BankConfigException.class)
    public void testStartFailOnLoadLastSale() throws Exception {

        // given
        doThrow(new IOException()).when(bpcProperties).load();

        // when
        service.start();

        // then
        verify(bpcProperties).load();
        verify(connector, never()).connect();
    }

    @Test
    public void shouldReversalCompleteWithSuccessfulApproveTest() throws BankException {
        //given
        ReversalData data = getReversalData();
        doReturn(
                getAnswer(ANY_STRING, BankBPCServiceImpl.APPROVE_SUCCESS, BankBPCServiceImpl.CORRECT_RESPONSE_VALUE, TEST_CARD_NUMBER, ACCEPTED_STRING,
                        " ", TEST_REF_NUMBER)).when(connector).makeTransaction(any(Request.class));
        List<BankEvent> listeners = mock(List.class);
        doReturn(listeners).when(service).getListeners();
        //when
        AuthorizationData result = service.reversal(data);

        //then
        verify(service, never()).processJRNOperation();
        verify(bpcProperties, never()).increaseERN();
        verify(dialogListener).addListeners(listeners);
        verify(dialogListener).removeServiceOperationListener();
        verify(dialogListener).removeBankListeners();
        assertThat(result.getAmount()).isEqualTo(TEST_AMOUNT);
        assertThat(result.getCard().getCardNumber()).isEqualTo(TEST_CARD_NUMBER);
        assertThat(result.isStatus()).isTrue();
        assertThat(result.getMessage()).isEqualTo(ACCEPTED_STRING);
        assertThat(result.getOperationType()).isEqualTo(BankOperationType.REVERSAL);
    }

    @Test(expected = DailyLogExpectedException.class)
    public void testReversalWithTransactionsProhibition() throws BankException {
        //given
        when(bpcProperties.isDailyLogExpected()).thenReturn(true);
        ReversalData data = getReversalData();
        //when
        service.reversal(data);
    }

    @Test
    public void shouldReversalCompleteWithSuccessfulJRNOperationTest() throws BankException {
        //given
        ReversalData data = getReversalData();
        doReturn(getAnswer(ANY_STRING, ANY_STRING, BankBPCServiceImpl.B_4_RESPONSE_CODE, TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER))
                .when(connector).makeTransaction(any(Request.class));
        doReturn(true).when(service).processJRNOperation();

        //when
        AuthorizationData result = service.reversal(data);

        //then
        verify(service).processJRNOperation();
        verify(bpcProperties, never()).increaseERN();
        assertThat(result.getAmount()).isEqualTo(TEST_AMOUNT);
        assertThat(result.getCard().getCardNumber()).isEqualTo(TEST_CARD_NUMBER);
        assertThat(result.isStatus()).isTrue();
        assertThat(result.getMessage()).isEqualTo(ACCEPTED_STRING);
        assertThat(result.getOperationType()).isEqualTo(BankOperationType.REVERSAL);
    }

    @Test
    public void shouldReversalFailedJRNOperationFailedTest() throws BankException {
        //given
        ReversalData data = getReversalData();
        doReturn(getAnswer(ANY_STRING, ANY_STRING, BankBPCServiceImpl.B_4_RESPONSE_CODE, TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER))
                .when(connector).makeTransaction(any(Request.class));
        doReturn(false).when(service).processJRNOperation();
        //when
        AuthorizationData result = service.reversal(data);
        //then
        assertThat(result.isStatus()).isFalse();
        verify(service).processJRNOperation();
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void shouldReversalFailedWithIncorrectResponseCode() throws BankException {
        //given
        ReversalData data = getReversalData();
        doReturn(getAnswer(ANY_STRING, ANY_STRING, ANY_STRING, TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));
        //when
        AuthorizationData result = service.reversal(data);
        //then
        assertThat(result.isStatus()).isFalse();
        verify(service, never()).processJRNOperation();
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void shouldSaleCompleteTest() throws BankException {
        //given
        SaleData data = getSaleData();
        doReturn(new AuthorizationData()).when(service).innerSale(data);
        List<BankEvent> listeners = mock(List.class);
        doReturn(listeners).when(service).getListeners();
        AuthorizationData authorizationData = mock(AuthorizationData.class);
        doReturn(authorizationData).when(service).innerSale(data);

        //when
        AuthorizationData result = service.sale(data);

        //then
        verify(dialogListener).addListeners(listeners);
        verify(dialogListener).removeServiceOperationListener();
        verify(dialogListener).removeBankListeners();
        verify(service).innerSale(data);
        assertThat(result).isEqualTo(authorizationData);
    }

    @Test(expected = DailyLogExpectedException.class)
    public void testSaleWithTransactionsProhibition() throws BankException {
        //given
        when(bpcProperties.isDailyLogExpected()).thenReturn(true);
        SaleData data = getSaleData();
        //when
        service.sale(data);
    }

    @Test(expected = BankException.class)
    public void shouldSaleThrowExceptionTest() throws BankException {
        //given
        SaleData data = getSaleData();
        doThrow(new BankException()).when(service).innerSale(data);
        //when
        service.sale(data);
        //then
        verify(service).innerSale(any(SaleData.class));
    }

    @Test(expected = BankException.class)
    public void shouldSaleThrowExceptionOnProcessResponseTest() throws BankException {

        //given
        SaleData data = getSaleData();
        Map<Integer, DataByte> testMap = new HashMap<>();
        doThrow(new BankException()).when(connector).makeTransaction(any(Request.class));
        doReturn(new AuthorizationData()).when(service).processSaleRefundResponse(data, testMap);
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);

        //when
        service.sale(data);

        //then
        verify(bpcProperties, never()).increaseERN();
        verify(service).sale(any(SaleData.class));
    }

    @Test
    public void shouldSaleOperationSuccessAfterReversalTest() throws BankException {
        //given
        SaleData data = getSaleData();
        doNothing().when(service).processAutoReversalOperation(anyString());
        doThrow(new BankBPCAutoReversalException(TEST_REF_NUMBER)).doReturn(new AuthorizationData()).when(service).innerSale(data);
        //when
        service.sale(data);
        //then
        verify(service, times(2)).innerSale(any(SaleData.class));
        verify(service).processAutoReversalOperation(TEST_REF_NUMBER);
    }

    @Test
    public void shouldInnerSaleComplete() throws BankException {
        //given
        SaleData data = getSaleData();
        Map<Integer, DataByte> testMap = mock(HashMap.class);
        when(testMap.isEmpty()).thenReturn(false);
        doReturn(testMap).when(connector).makeTransaction(any(Request.class));
        AuthorizationData authorizationData = new AuthorizationData();
        doReturn(authorizationData).when(service).processSaleRefundResponse(data, testMap);
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        AuthorizationData result = service.innerSale(data);
        //then
        verify(bpcProperties).increaseERN();
        assertThat(result).isEqualTo(authorizationData);
        assertThat(result.getOperationType()).isEqualTo(BankOperationType.SALE);
    }

    @Test
    public void testInnerSaleIncrementRrn() throws BankException, IOException {
        //given
        SaleData data = getSaleData();
        Map<Integer, DataByte> testMap = mock(HashMap.class);
        when(testMap.isEmpty()).thenReturn(false);
        doReturn(testMap).when(connector).makeTransaction(any(Request.class));
        AuthorizationData authorizationData = new AuthorizationData();
        doReturn(authorizationData).when(service).processSaleRefundResponse(data, testMap);
        Mockito.doCallRealMethod().when(bpcProperties).increaseERN();
        when(bpcProperties.getERN()).thenCallRealMethod();
        //when
        AuthorizationData result = service.innerSale(data);
        //then
        Assert.assertEquals("2", bpcProperties.getERN());
        assertThat(result).isEqualTo(authorizationData);
        assertThat(result.getOperationType()).isEqualTo(BankOperationType.SALE);
    }

    @Test(expected = BankException.class)
    public void shouldInnerSaleMakeTransactionException() throws BankException {
        //given
        SaleData data = getSaleData();
        doThrow(new BankException()).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.innerSale(data);
        //then
        verify(bpcProperties, never()).increaseERN();
    }

    @Test(expected = BankException.class)
    public void shouldInnerSaleProcessResponseException() throws BankException {
        //given
        SaleData data = getSaleData();
        Map<Integer, DataByte> testMap = mock(HashMap.class);
        when(testMap.isEmpty()).thenReturn(false);
        doReturn(testMap).when(connector).makeTransaction(any(Request.class));
        doThrow(new BankException()).when(service).processSaleRefundResponse(data, testMap);
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.innerSale(data);
        //then
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void testInnerSaleWithEmptyResponse() throws BankException {
        //given
        doReturn(Collections.emptyMap()).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        String exceptionMessage = null;
        SaleData data = getSaleData();

        //when
        try {
            service.innerSale(data);
        } catch (BankTimeoutException e) {
            exceptionMessage = e.getMessage();
        }

        //then
        verify(dialogListener).closeDialog();
        assertThat(exceptionMessage).isEqualTo(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
    }

    @Test
    public void shouldRefundCompleteTest() throws BankException {
        //given
        RefundData data = getRefundData();
        AuthorizationData authorizationData = new AuthorizationData();
        doReturn(authorizationData).when(service).innerRefund(data);
        List<BankEvent> listeners = mock(List.class);
        doReturn(listeners).when(service).getListeners();
        //when
        AuthorizationData result = service.refund(data);
        //then
        verify(service).innerRefund(any(RefundData.class));
        InOrder inOrder = inOrder(dialogListener);
        inOrder.verify(dialogListener).removeServiceOperationListener();
        inOrder.verify(dialogListener).addListeners(listeners);
        inOrder.verify(dialogListener).removeBankListeners();
        assertThat(result).isEqualTo(authorizationData);
    }

    @Test(expected = DailyLogExpectedException.class)
    public void testRefundWithTransactionsProhibition() throws BankException {
        //given
        when(bpcProperties.isDailyLogExpected()).thenReturn(true);
        RefundData data = getRefundData();
        //when
        service.refund(data);
    }

    @Test(expected = BankException.class)
    public void shouldRefundThrowExceptionTest() throws BankException {
        //given
        RefundData data = getRefundData();
        doThrow(new BankException()).when(service).innerRefund(data);
        //when
        service.refund(data);

        verify(service).innerRefund(any(RefundData.class));
    }

    @Test(expected = BankException.class)
    public void shouldRefundThrowExceptionOnProcessResponseTest() throws BankException {

        //given
        RefundData data = getRefundData();
        Map<Integer, DataByte> testMap = new HashMap<>();
        doThrow(new BankException()).when(connector).makeTransaction(any(Request.class));
        doReturn(new AuthorizationData()).when(service).processSaleRefundResponse(data, testMap);
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.refund(data);
        //then
        verify(bpcProperties, never()).increaseERN();
        verify(service).refund(any(RefundData.class));
    }

    @Test
    public void shouldRefundOperationSuccessAfterReversalTest() throws BankException {
        //given
        RefundData data = getRefundData();
        doThrow(new BankBPCAutoReversalException(TEST_REF_NUMBER)).doReturn(new AuthorizationData()).when(service).innerRefund(data);
        doNothing().when(service).processAutoReversalOperation(anyString());
        //when
        service.refund(data);
        //then
        verify(service, times(2)).innerRefund(any(RefundData.class));
        verify(service).processAutoReversalOperation(TEST_REF_NUMBER);
    }

    @Test
    public void shouldInnerRefundComplete() throws BankException {
        //given
        RefundData data = getRefundData();
        Map<Integer, DataByte> testMap = mock(HashMap.class);
        when(testMap.isEmpty()).thenReturn(false);
        doReturn(testMap).when(connector).makeTransaction(any(Request.class));
        AuthorizationData authorizationData = new AuthorizationData();
        doReturn(authorizationData).when(service).processSaleRefundResponse(data, testMap);
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        AuthorizationData result = service.innerRefund(data);
        //then
        verify(bpcProperties).increaseERN();
        assertThat(result).isEqualTo(authorizationData);
        assertThat(result.getOperationType()).isEqualTo(BankOperationType.REFUND);
    }

    @Test
    public void testInnerRefundWithEmptyResponse() throws BankException {
        //given
        doReturn(Collections.emptyMap()).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        String exceptionMessage = null;
        RefundData data = getRefundData();

        //when
        try {
            service.innerRefund(data);
        } catch (BankTimeoutException e) {
            exceptionMessage = e.getMessage();
        }

        //then
        verify(dialogListener).closeDialog();
        assertThat(exceptionMessage).isEqualTo(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
    }

    @Test(expected = BankException.class)
    public void shouldInnerRefundMakeTransactionException() throws BankException {
        //given
        RefundData data = getRefundData();
        doThrow(new BankException()).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.innerRefund(data);
        //then
        verify(bpcProperties, never()).increaseERN();
    }

    @Test(expected = BankException.class)
    public void shouldInnerRefundProcessResponseException() throws BankException {
        //given
        RefundData data = getRefundData();
        Map<Integer, DataByte> testMap = mock(HashMap.class);
        when(testMap.isEmpty()).thenReturn(false);
        doReturn(testMap).when(connector).makeTransaction(any(Request.class));
        doThrow(new BankException()).when(service).processSaleRefundResponse(data, testMap);
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.innerRefund(data);
        //then
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void shouldProcessAutoReversalOperationCompletedTest() throws BankException {
        //given
        ReversalData data = getReversalData();
        doReturn(
                getAnswer(ANY_STRING, BankBPCServiceImpl.APPROVE_SUCCESS, BankBPCServiceImpl.CORRECT_RESPONSE_VALUE, TEST_CARD_NUMBER, ACCEPTED_STRING,
                        " ", TEST_REF_NUMBER)).when(connector).makeTransaction(any(Request.class));
        //when
        service.processAutoReversalOperation(data.getRefNumber());
        //then
        verify(bpcProperties).increaseERN();
    }

    @Test
    public void shouldProcessAutoReversalOperationCompletedB4Test() throws BankException {
        //given
        ReversalData data = getReversalData();
        doReturn(
                getAnswer(ANY_STRING, BankBPCServiceImpl.APPROVE_SUCCESS, BankBPCServiceImpl.B_4_RESPONSE_CODE, TEST_CARD_NUMBER, ACCEPTED_STRING, " ",
                        TEST_REF_NUMBER)).when(connector).makeTransaction(any(Request.class));
        //when
        service.processAutoReversalOperation(data.getRefNumber());
        //then
        verify(bpcProperties).increaseERN();
    }

    @Test(expected = BankException.class)
    public void shouldProcessAutoReversalOperationFailedTest() throws BankException {
        //given
        ReversalData data = getReversalData();
        doReturn(getAnswer(ANY_STRING, BankBPCServiceImpl.APPROVE_SUCCESS, "XYNTA", TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER))
                .when(connector).makeTransaction(any(Request.class));
        //when
        service.processAutoReversalOperation(data.getRefNumber());
        //then
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void shouldProcessJRNOperationReturnsTrueTest() throws BankException {

        //given
        doReturn(
                getAnswer(BankBPCServiceImpl.REVERSAL_OPERATION_MESSAGE_ID, BankBPCServiceImpl.APPROVE_SUCCESS, BankBPCServiceImpl.CORRECT_RESPONSE_VALUE,
                        TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER)).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        boolean result = service.processJRNOperation();
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldProcessJRNOperationReturnsFalseMessageIDMismatchTest() throws BankException {

        //given
        doReturn(
                getAnswer(ANY_STRING, BankBPCServiceImpl.APPROVE_SUCCESS, BankBPCServiceImpl.CORRECT_RESPONSE_VALUE, TEST_CARD_NUMBER, ACCEPTED_STRING,
                        " ", TEST_REF_NUMBER)).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        boolean result = service.processJRNOperation();
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldProcessJRNOperationReturnsFalseResponseCodeMismatchTest() throws BankException {

        //given
        doReturn(getAnswer(BankBPCServiceImpl.REVERSAL_OPERATION_MESSAGE_ID, BankBPCServiceImpl.APPROVE_SUCCESS, ANY_STRING, TEST_CARD_NUMBER,
                ACCEPTED_STRING, " ", TEST_REF_NUMBER)).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        boolean result = service.processJRNOperation();
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldProcessJRNOperationReturnsFalseMessageIDAndResponseCodeMismatchTest() throws BankException {

        //given
        doReturn(getAnswer(ANY_STRING, BankBPCServiceImpl.APPROVE_SUCCESS, ANY_STRING, TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER))
                .when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        boolean result = service.processJRNOperation();
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldProcessSaleRefundResponseSuccess() throws BankException {
        //given
        SaleData data = getSaleData();
        BankCard testCard = new BankCard();
        data.setCard(testCard);
        //when
        AuthorizationData auth = service.processSaleRefundResponse(data,
                getAnswer(ANY_STRING, BankBPCServiceImpl.APPROVE_SUCCESS, ANY_STRING, TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER, TEST_RRN_NUMBER));
        //then
        assertThat(auth.isStatus()).isTrue();
        assertThat(auth.getRefNumber()).isEqualTo(TEST_REF_NUMBER);
        assertThat(auth.getExtendedData().get(RequestFactory.RRN)).isEqualTo(TEST_RRN_NUMBER);
        assertThat(auth.getResponseCode()).isEqualTo(ANY_STRING);
        assertThat(auth.isPrintNegativeSlip()).isTrue();
        assertThat(auth.getCard()).isEqualTo(testCard);
        assertThat(auth.getCard().getCardNumber()).isEqualTo(TEST_CARD_NUMBER);
        assertThat(auth.getSlips()).isEqualTo(Parser.parseSlips(" "));
        assertThat(new Date().getTime() - auth.getDate().getTime()).isLessThanOrEqualTo(DateUtils.MILLIS_PER_MINUTE);
    }

    @Test
    public void shouldProcessSaleRefundResponseApproveFailed() throws BankException {
        //given
        SaleData data = getSaleData();
        //when
        AuthorizationData auth = service
                .processSaleRefundResponse(data, getAnswer(ANY_STRING, ANY_STRING, ANY_STRING, TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER));
        assertThat(auth.isStatus()).isFalse();
    }

    @Test(expected = BankException.class)
    public void shouldProcessSaleRefundResponseIncorrectResponseDetected() throws BankException {
        //given
        SaleData data = getSaleData();
        //when
        service.processSaleRefundResponse(data, getAnswer(ANY_STRING, ANY_STRING, "RT", TEST_CARD_NUMBER, ACCEPTED_STRING, " ", TEST_REF_NUMBER));
    }

    @Test
    public void testDailyLog() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, BankBPCServiceImpl.CORRECT_RESPONSE_VALUE, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER))
                .when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        DailyLogData result = service.dailyLog(1L);
        //then
        assertThat(result).isNotNull();
        assertThat(result.getSlip()).isEqualTo(Arrays.asList(ANY_STRING));
        verify(bpcProperties).setDailyLogExpected(false);
        verify(dialogListener).removeServiceOperationListener();
    }

    @Test(expected = BankAuthorizationException.class)
    public void testDailyLogIncorrectResponseCode() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.dailyLog(1L);
    }

    @Test(expected = BankException.class)
    public void testDailyLogNoResponseCode() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, null, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.dailyLog(1L);
        verify(bpcProperties).setDailyLogExpected(true);
    }

    @Test(expected = BankException.class)
    public void testDailyLogNoVisualHostResponse() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, null, ANY_STRING, TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when
        service.dailyLog(1L);
        verify(bpcProperties).setDailyLogExpected(true);
    }

    @Test
    public void testProcessServiceOperation() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, BankBPCServiceImpl.CORRECT_RESPONSE_VALUE, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER))
                .when(connector).makeTransaction(any(Request.class));
        //when
        List<List<String>> slips = service.processServiceOperation(bpcServiceOperation);
        //then
        assertThat(slips.get(0)).isEqualTo(Arrays.asList(ANY_STRING));
    }

    @Test
    public void testProcessServiceOperationIncorrectResponseCode() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));

        //when
        String exceptionMessage = null;
        try {
            service.processServiceOperation(bpcServiceOperation);
        } catch (BankAuthorizationException e) {
            exceptionMessage = e.getMessage();
        }

        //then
        assertThat(exceptionMessage).isEqualTo(ANY_STRING);
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void testProcessServiceOperationWithB5Response() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, B5_RESPONSE_CODE, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));
        //when
        BankAuthorizationException exception = null;
        try {
            service.processServiceOperation(bpcServiceOperation);
        } catch (BankAuthorizationException e) {
            exception = e;
        }
        //then
        assert exception != null;
        assertThat(exception.getMessage()).isEqualTo(ANY_STRING);
        assertThat(exception.getErrorType()).isEqualTo(CashErrorType.AUTO_FIXE);
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void testProcessServiceOperationWithBBResponse() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, BB_RESPONSE_CODE, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));
        //when
        BankAuthorizationException exception = null;
        try {
            service.processServiceOperation(bpcServiceOperation);
        } catch (BankAuthorizationException e) {
            exception = e;
        }
        //then
        assert exception != null;
        assertThat(exception.getMessage()).isEqualTo(ANY_STRING);
        assertThat(exception.getErrorType()).isEqualTo(CashErrorType.AUTO_FIXE);
        verify(bpcProperties, never()).increaseERN();
    }

    @Test(expected = BankException.class)
    public void testProcessServiceOperationNoResponseCode() throws BankException {
        //given
        doReturn(getAnswer(ANY_STRING, ANY_STRING, null, ANY_STRING, ANY_STRING, ANY_STRING, TEST_REF_NUMBER)).when(connector)
                .makeTransaction(any(Request.class));
        //when
        service.processServiceOperation(bpcServiceOperation);

        //then
        verify(bpcProperties, never()).increaseERN();
    }

    @Test
    public void testProcessServiceWithEmptyResponse() throws BankException {
        //given
        doReturn(Collections.emptyMap()).when(connector).makeTransaction(any(Request.class));

        //when
        BankTimeoutException receivedException = null;
        try {
            service.processServiceOperation(bpcServiceOperation);
        } catch (BankTimeoutException e) {
            receivedException = e;
        }

        //then
        assert receivedException != null;
        assertThat(receivedException.getErrorType()).isEqualTo(CashErrorType.NOT_CRITICAL_ERROR_WITHOUT_REPEAT);
        assertThat(receivedException.getMessage()).isEqualTo(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
        verify(dialogListener).closeDialog();
    }

    @Test
    public void testIsHostOnline() throws BankException {
        //given
        doReturn(null).when(service).processServiceOperation(any(TestHostOperation.class));
        service.setUseAutomaticHostTest(true);

        //when
        boolean result = service.isHostOnline();
        //then
        assertThat(result).isTrue();
        verify(dialogListener).removeBankListeners();
        verify(dialogListener).removeServiceOperationListener();
    }

    @Test
    public void testIsHostOnlineReturnFalse() throws BankException {
        //given
        service.setUseAutomaticHostTest(true);
        doThrow(new BankException()).when(service).processServiceOperation(any(TestHostOperation.class));
        //when
        boolean result = service.isHostOnline();
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void testIsHostOnlineWithoutUseAutomaticHost() throws BankException {
        //given
        service.setUseAutomaticHostTest(false);
        //when
        boolean result = service.isHostOnline();
        //then
        assertThat(result).isTrue();
    }

    private Map<Integer, DataByte> getAnswer(String messageId, String approve, String responseCode, String pan, String visualHostResponse,
                                             String receipt, String ern, String rrn) {
        Map<Integer, DataByte> result = new HashMap<>();
        addToMap(result, Tag.Input.MESSAGE_ID, messageId);
        addToMap(result, Tag.Output.APPROVE, approve);
        addToMap(result, Tag.Output.RESPONSE_CODE, responseCode);
        addToMap(result, Tag.Output.PAN, pan);
        addToMap(result, Tag.Output.VISUAL_HOST_RESPONSE, visualHostResponse);
        addToMap(result, Tag.Output.RECEIPT, receipt);
        addToMap(result, Tag.Output.ERN, ern);
        addToMap(result, Tag.Output.RRN, rrn);
        return result;
    }

    private Map<Integer, DataByte> getAnswer(String messageId, String approve, String responseCode, String pan, String visualHostResponse,
                                             String receipt, String ern) {
        return getAnswer(messageId, approve, responseCode, pan, visualHostResponse, receipt, ern, null);
    }

    private void addToMap(Map<Integer, DataByte> map, int tag, String value) {
        if (value != null) {
            map.put(tag, new DataByte(getCP866BytesFromString(value)));
        }
    }

    private byte[] getCP866BytesFromString(String str) {
        try {
            return str.getBytes("cp866");
        } catch (UnsupportedEncodingException ignore) {
            return str.getBytes();
        }
    }

    private SaleData getSaleData() {
        SaleData data = new SaleData();
        data.setAmount(TEST_AMOUNT);
        data.setCurrencyCode(BYR_CURRENCY_CODE);
        return data;
    }

    private RefundData getRefundData() {
        RefundData data = new RefundData();
        data.setAmount(TEST_AMOUNT);
        data.setCurrencyCode(BYR_CURRENCY_CODE);
        data.setRefNumber(TEST_REF_NUMBER);
        data.setExtendedData(new HashMap<String, String>() {{
            put(RequestFactory.RRN, TEST_RRN_NUMBER);
        }});
        return data;
    }

    private ReversalData getReversalData() {
        ReversalData data = new ReversalData();
        data.setOriginalSaleTransactionAmount(TEST_AMOUNT);
        data.setAmount(TEST_AMOUNT);
        data.setRefNumber("1");
        data.setCurrencyCode(BYR_CURRENCY_CODE);
        return data;
    }

    @Test
    public void testAddDialogListener() throws Exception {

        //given
        BankDialogEvent listener = mock(BankDialogEvent.class);

        //when
        service.addDialogListener(listener);

        //then
        verify(dialogListener).addServiceOperationListener(listener);
    }

    @Test
    public void testSendDialogResponse() throws Exception {
        //when
        service.sendDialogResponse(any(BankDialogType.class), anyString());

        //then
        verify(dialogListener).answer(any(BankDialogType.class), anyString());
        verify(dialogListener).answer(any(BankDialogType.class), anyString());
    }

    @Test
    public void testSendDialogResponseThrowsException() throws Exception {

        //given
        doThrow(new IOException()).when(dialogListener).answer(any(BankDialogType.class), anyString());
        doThrow(new IOException()).when(dialogListener).answer(any(BankDialogType.class), anyString());

        //when
        service.sendDialogResponse(any(BankDialogType.class), anyString());

        //then
        verify(dialogListener).answer(any(BankDialogType.class), anyString());
        verify(dialogListener).answer(any(BankDialogType.class), anyString());
    }

    @Test
    public void testCloseDialog() throws Exception {

        //when
        service.closeDialog();

        //then
        verify(dialogListener).closeDialog();
    }

    @Test
    public void testPartialReversal() throws Exception {

        //given
        ReversalData reversalData = getReversalData();
        reversalData.setAmount(TEST_AMOUNT_FOR_PARTIAL_REVERSAL);
        AuthorizationData mock = mock(AuthorizationData.class);
        doReturn(mock).when(service).refund(reversalData);

        //when
        AuthorizationData result = service.reversal(reversalData);

        //then
        assertThat(result).isSameAs(mock);
        verify(service).refund(reversalData);
    }

    @Test
    public void testReversalWithEmptyResponse() throws Exception {
        //given
        ReversalData data = getReversalData();
        doReturn(Collections.emptyMap()).when(connector).makeTransaction(any(Request.class));
        //when

        try {
            service.reversal(data);
        } catch (BankTimeoutException ignore) {
        }

        //then
        verify(dialogListener).closeDialog();
    }

    @Test
    public void testProcessAutoReversalWithEmptyResponse() throws Exception {
        //given
        doReturn(Collections.emptyMap()).when(connector).makeTransaction(any(Request.class));
        //when

        try {
            service.processAutoReversalOperation(TEST_REF_NUMBER);
        } catch (BankTimeoutException ignore) {
        }

        //then
        verify(dialogListener).closeDialog();
    }

    @Test
    public void testProcessJRNOperation() throws Exception {
        //given
        doReturn(Collections.emptyMap()).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        //when

        try {
            service.processJRNOperation();
        } catch (BankTimeoutException ignore) {
        }

        //then
        verify(dialogListener).closeDialog();
    }

    @Test
    public void testDailyLogWithEmptyResponse() throws Exception {
        //given
        doReturn(Collections.emptyMap()).when(connector).makeTransaction(any(Request.class));
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);
        String exceptionMessage = null;

        //when
        try {
            service.dailyLog(123L);
        } catch (BankTimeoutException e) {
            exceptionMessage = e.getMessage();
        }

        //then
        verify(dialogListener).closeDialog();
        assertThat(exceptionMessage).isEqualTo(ResBundleBankBPC.getString("TIMEOUT_EXPIRED"));
        verify(bpcProperties).setDailyLogExpected(true);
    }

    @Test
    public void testDailyLogAutoReversalNeeded() throws Exception {
        //given
        doReturn(
                getAnswer(ANY_STRING, ANY_STRING, BankBPCServiceImpl.AUTO_REVERSAL_OPERATION_NEEDED_RESPONSE_CODE, ANY_STRING, ANY_STRING, ANY_STRING,
                        TEST_REF_NUMBER)).when(connector).makeTransaction(any(Request.class));
        doNothing().when(service).processAutoReversalOperation(TEST_REF_NUMBER);
        when(bpcProperties.getERN()).thenReturn(TEST_REF_NUMBER);

        //when
        BankBPCAutoReversalException receivedException = null;
        try {
            service.dailyLog(123L);
        } catch (BankBPCAutoReversalException e) {
            receivedException = e;
        }

        //then
        verify(service).processAutoReversalOperation(TEST_REF_NUMBER);
        assert receivedException != null;
        assertThat(receivedException.getRefNumber()).isEqualTo(TEST_REF_NUMBER);
        verify(bpcProperties, times(2)).setDailyLogExpected(true);
    }

    @Test
    public void testSetProcessingCatalog() throws Exception {

        //when
        service.setProcessing(anyString());

        //then
        verify(connector).setProcessingCatalog(anyString());
    }

    @Test
    public void testSetTcpAddress() throws Exception {

        //when
        service.setTcpAddress(anyString());

        //then
        verify(connector).setTcpAddress(anyString());
    }

    @Test
    public void testSetTcpPort() throws Exception {

        //when
        service.setTcpPort(anyInt());

        //then
        verify(connector).setTcpPort(anyInt());
    }
}
