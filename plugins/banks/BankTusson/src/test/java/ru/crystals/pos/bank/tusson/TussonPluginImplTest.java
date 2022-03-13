package ru.crystals.pos.bank.tusson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.tusson.printer.DocumentType;
import ru.crystals.pos.bank.tusson.printer.TerminalSlip;
import ru.crystals.pos.bank.tusson.printer.TussonSlipsReceiver;
import ru.crystals.pos.bank.tusson.protocol.Operation;
import ru.crystals.pos.bank.tusson.protocol.Request;
import ru.crystals.pos.bank.tusson.protocol.Response;
import ru.crystals.pos.bank.tusson.protocol.ResponseStatus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TussonPluginImplTest {

    private static final long SLIPS_AWAITING_TIMEOUT = 20L;
    private static final long TEST_AMOUNT = 12345L;
    private static final String TEST_REF_NUMBER = "1234567";
    private static final int TEST_OPERATION_AMOUNT = 12345;
    private static final String TEST_BANK_ANSWER = "TEST_BANK_ANSWER";
    private static final String ANY_STRING = "ANY_STRING";

    @Mock
    private ExecutorService operationExecutor;
    @Mock
    private TussonSlipsReceiver slipsReceiver;
    @Mock
    private TerminalConnector terminalConnector;
    @Mock
    private ExecutorService eventDailyLogExecutor;
    @Mock
    private AuthorizationData authorizationData;
    @Mock
    private Response response;
    @Mock
    private Request request;
    @Mock
    private List<TerminalSlip> slips;
    @Mock
    private SaleData saleDataMock;
    @Mock
    private ReversalData reversalDataMock;
    @Mock
    private RefundData refundDataMock;
    @Mock
    private BankCard bankCard;
    @Mock
    private List<List<String>> parsedSlips;

    @Spy
    @InjectMocks
    private TussonPluginImpl service = new TussonPluginImpl();


    @Before
    public void beforeTest() {
        doReturn(TEST_BANK_ANSWER).when(response).getBankAnswer();
        doReturn(Long.parseLong(TEST_REF_NUMBER)).when(response).getUniqueNumber();
        doReturn(TEST_AMOUNT).when(saleDataMock).getAmount();
        doReturn(TEST_AMOUNT).when(reversalDataMock).getAmount();
        doReturn(TEST_AMOUNT).when(refundDataMock).getAmount();
    }

    @Test
    public void testSale() throws Exception {
        //given

        SaleData saleData = new SaleData();
        saleData.setAmount(TEST_AMOUNT);
        doReturn(authorizationData).when(service).performOperation(any(SaleData.class), any(Request.class));
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<SaleData> saleDataCapture = ArgumentCaptor.forClass(SaleData.class);

        //when
        AuthorizationData result = service.sale(saleData);
        //then
        verify(service).performOperation(saleDataCapture.capture(), requestCaptor.capture());
        assertThat(saleData).isSameAs(saleDataCapture.getValue());
        assertThat(requestCaptor.getValue().equalsExceptUnique(Request.getRequestForSaleOperation(TEST_OPERATION_AMOUNT))).isTrue();
        assertThat(result).isSameAs(authorizationData);
    }

    @Test
    public void testSaleThrowException() throws Exception {
        //given
        SaleData saleData = new SaleData();
        saleData.setAmount(TEST_AMOUNT);
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<SaleData> saleDataCapture = ArgumentCaptor.forClass(SaleData.class);
        doThrow(new BankException()).when(service).performOperation(any(SaleData.class), any(Request.class));
        //when
        try {
            service.sale(saleData);
            fail("Expected BankException but never throws");
        } catch (BankException e) {

        }
        //then
        verify(service).performOperation(saleDataCapture.capture(), requestCaptor.capture());
        assertThat(saleData).isSameAs(saleDataCapture.getValue());
        assertThat(requestCaptor.getValue().equalsExceptUnique(Request.getRequestForSaleOperation(TEST_OPERATION_AMOUNT))).isTrue();
    }

    @Test
    public void testReversalDirect() throws Exception {
        //given

        ReversalData reversalData = new ReversalData();
        reversalData.setAmount(TEST_AMOUNT);
        reversalData.setRefNumber(TEST_REF_NUMBER);
        reversalData.setOriginalSaleTransactionAmount(TEST_AMOUNT);
        doReturn(authorizationData).when(service).performOperation(any(ReversalData.class), any(Request.class));
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<ReversalData> reversalDataArgumentCaptor = ArgumentCaptor.forClass(ReversalData.class);

        //when
        AuthorizationData result = service.reversal(reversalData);
        //then
        verify(service).performOperation(reversalDataArgumentCaptor.capture(), requestCaptor.capture());
        assertThat(reversalData).isSameAs(reversalDataArgumentCaptor.getValue());
        assertThat(requestCaptor.getValue().equalsExceptUnique(Request.getRequestForReversalOperation(Long.parseLong(TEST_REF_NUMBER), TEST_OPERATION_AMOUNT))).isTrue();
        assertThat(result).isSameAs(authorizationData);
    }

    @Test
    public void testReversalRefundActual() throws Exception {
        //given

        ReversalData reversalData = new ReversalData();
        reversalData.setAmount(TEST_AMOUNT);
        reversalData.setRefNumber(TEST_REF_NUMBER);
        reversalData.setOriginalSaleTransactionAmount(0L);
        doReturn(authorizationData).when(service).refund(any(ReversalData.class));

        //when
        AuthorizationData result = service.reversal(reversalData);
        //then
        verify(service).refund(reversalData);
        verify(service, never()).performOperation(any(SaleData.class), any(Request.class));
        assertThat(result).isSameAs(authorizationData);
    }

    @Test
    public void testReversalDirectThrowException() throws Exception {
        //given
        ReversalData reversalData = new ReversalData();
        reversalData.setAmount(TEST_AMOUNT);
        reversalData.setRefNumber(TEST_REF_NUMBER);
        reversalData.setOriginalSaleTransactionAmount(TEST_AMOUNT);
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<ReversalData> reversalDataArgumentCaptor = ArgumentCaptor.forClass(ReversalData.class);
        doThrow(new BankException()).when(service).performOperation(any(SaleData.class), any(Request.class));
        //when
        try {
            service.reversal(reversalData);
            fail("Expected BankException but never throws");
        } catch (BankException e) {

        }
        //then
        verify(service).performOperation(reversalDataArgumentCaptor.capture(), requestCaptor.capture());
        assertThat(reversalData).isSameAs(reversalDataArgumentCaptor.getValue());
        assertThat(requestCaptor.getValue().equalsExceptUnique(Request.getRequestForReversalOperation(Long.parseLong(TEST_REF_NUMBER), TEST_OPERATION_AMOUNT))).isTrue();
    }

    @Test
    public void testReversalRefundActualThrowException() throws Exception {
        //given
        ReversalData reversalData = new ReversalData();
        reversalData.setAmount(TEST_AMOUNT);
        reversalData.setRefNumber(TEST_REF_NUMBER);
        reversalData.setOriginalSaleTransactionAmount(0L);
        doThrow(new BankException()).when(service).refund(any(RefundData.class));
        //when
        try {
            service.reversal(reversalData);
            fail("Expected BankException but never throws");
        } catch (BankException e) {

        }
        //then
        verify(service).refund(reversalData);
        verify(service, never()).performOperation(any(SaleData.class), any(Request.class));
    }

    @Test
    public void testRefund() throws Exception {
        //given

        RefundData saleData = new RefundData();
        saleData.setAmount(TEST_AMOUNT);
        doReturn(authorizationData).when(service).performOperation(any(RefundData.class), any(Request.class));
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<RefundData> saleDataCapture = ArgumentCaptor.forClass(RefundData.class);

        //when
        AuthorizationData result = service.refund(saleData);
        //then
        verify(service).performOperation(saleDataCapture.capture(), requestCaptor.capture());
        assertThat(saleData).isSameAs(saleDataCapture.getValue());
        assertThat(requestCaptor.getValue().equalsExceptUnique(Request.getRequestForRefundOperation(TEST_OPERATION_AMOUNT))).isTrue();
        assertThat(result).isSameAs(authorizationData);
    }

    @Test
    public void testRefundThrowException() throws Exception {
        //given
        RefundData saleData = new RefundData();
        saleData.setAmount(TEST_AMOUNT);
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<RefundData> saleDataCapture = ArgumentCaptor.forClass(RefundData.class);
        doThrow(new BankException()).when(service).performOperation(any(RefundData.class), any(Request.class));
        //when
        try {
            service.refund(saleData);
            fail("Expected BankException but never throws");
        } catch (BankException e) {

        }
        //then
        verify(service).performOperation(saleDataCapture.capture(), requestCaptor.capture());
        assertThat(saleData).isSameAs(saleDataCapture.getValue());
        assertThat(requestCaptor.getValue().equalsExceptUnique(Request.getRequestForRefundOperation(TEST_OPERATION_AMOUNT))).isTrue();
    }


    @Test
    public void testPerformOperationSuccess() throws Exception {
        //given
        doReturn(Operation.SALE).when(request).getOperation();
        doReturn(ResponseStatus.SUCCESS).when(response).getStatus();
        doReturn(response).when(terminalConnector).processOperation(request);
        doReturn(authorizationData).when(service).makeAuth(any(SaleData.class), any(Response.class), any(Operation.class), anyList());
        doReturn(slips).when(slipsReceiver).getSlips(anyCollection(), anyLong(), any(TimeUnit.class));
        //when
        AuthorizationData result = service.performOperation(saleDataMock, request);
        //then
        verify(operationExecutor).execute(slipsReceiver);
        verify(terminalConnector).processOperation(request);
        verify(response, times(2)).getStatus();
        verify(request, times(2)).getOperation();
        verify(slipsReceiver).getSlips(Operation.SUCCESS_DOCUMENTS, SLIPS_AWAITING_TIMEOUT, TimeUnit.SECONDS);
        verify(service).makeAuth(saleDataMock, response, request.getOperation(), slips);
        assertThat(result).isSameAs(authorizationData);
    }

    @Test
    public void testPerformOperationFailed() throws Exception {
        //given
        doReturn(Operation.SALE).when(request).getOperation();
        doReturn(ResponseStatus.CANCELED).when(response).getStatus();
        doReturn(response).when(terminalConnector).processOperation(request);
        doReturn(authorizationData).when(service).makeAuth(any(SaleData.class), any(Response.class), any(Operation.class), anyList());
        doReturn(slips).when(slipsReceiver).getSlips(anyCollection(), anyLong(), any(TimeUnit.class));
        //when
        AuthorizationData result = service.performOperation(saleDataMock, request);
        //then
        verify(operationExecutor).execute(slipsReceiver);
        verify(terminalConnector).processOperation(request);
        verify(response, times(2)).getStatus();
        verify(request, times(2)).getOperation();
        verify(slipsReceiver).getSlips(Operation.FAILED_DOCUMENTS, SLIPS_AWAITING_TIMEOUT, TimeUnit.SECONDS);
        verify(service).makeAuth(saleDataMock, response, request.getOperation(), slips);
        assertThat(result).isSameAs(authorizationData);
    }

    @Test
    public void testPerformOperationFailedCauseReversalOperationDenied() throws Exception {
        //given
        doReturn(Operation.REVERSAL).when(request).getOperation();
        doReturn(ResponseStatus.SUCCESS).doReturn(ResponseStatus.CANCELED).doReturn(ResponseStatus.CANCELED).when(response).getStatus();
        doReturn(response).when(terminalConnector).processOperation(request);
        doReturn(false).when(response).isTransactionCanceled();
        doReturn(authorizationData).when(service).makeAuth(any(SaleData.class), any(Response.class), any(Operation.class), anyList());
        doReturn(slips).when(slipsReceiver).getSlips(anyCollection(), anyLong(), any(TimeUnit.class));
        //when
        AuthorizationData result = service.performOperation(saleDataMock, request);
        //then
        verify(operationExecutor).execute(slipsReceiver);
        verify(terminalConnector).processOperation(request);
        verify(response, times(2)).getStatus();
        verify(request, times(2)).getOperation();
        verify(response).setStatus(ResponseStatus.CANCELED);
        verify(slipsReceiver).getSlips(Operation.FAILED_DOCUMENTS, SLIPS_AWAITING_TIMEOUT, TimeUnit.SECONDS);
        verify(service).makeAuth(saleDataMock, response, request.getOperation(), slips);
        assertThat(result).isSameAs(authorizationData);
    }

    @Test
    public void testPerformOperationSuccessThrowsExceptionOnMakeAuth() throws Exception {
        //given
        doReturn(Operation.SALE).when(request).getOperation();
        doReturn(ResponseStatus.SUCCESS).when(response).getStatus();
        doReturn(response).when(terminalConnector).processOperation(request);
        doThrow(new BankAuthorizationException()).when(service).makeAuth(any(SaleData.class), any(Response.class), any(Operation.class), anyList());
        doReturn(slips).when(slipsReceiver).getSlips(anyCollection(), anyLong(), any(TimeUnit.class));
        //when
        try {
            service.performOperation(saleDataMock, request);
            fail("Expected BankAuthorizationException but never throws");
        } catch (BankAuthorizationException e) {

        }
        //then
        verify(operationExecutor).execute(slipsReceiver);
        verify(terminalConnector).processOperation(request);
        verify(response, times(2)).getStatus();
        verify(request, times(2)).getOperation();
        verify(slipsReceiver).getSlips(Operation.SUCCESS_DOCUMENTS, SLIPS_AWAITING_TIMEOUT, TimeUnit.SECONDS);
        verify(service).makeAuth(saleDataMock, response, request.getOperation(), slips);
    }

    @Test
    public void testPerformOperationThrowsExceptionStatusBusy() throws Exception {
        //given

        doReturn(Operation.SALE).when(request).getOperation();
        doReturn(ResponseStatus.BUSY).when(response).getStatus();
        doReturn(response).when(terminalConnector).processOperation(request);

        //when
        try {
            service.performOperation(saleDataMock, request);
            fail("Expected BankCommunicationException but never throws");
        } catch (BankCommunicationException e) {

        }
        //then
        verify(operationExecutor).execute(slipsReceiver);
        verify(terminalConnector).processOperation(request);
        verify(response).getStatus();
        verify(request, never()).getOperation();
        verify(slipsReceiver, never()).getSlips(anyCollection(), anyLong(), any(TimeUnit.class));
        verify(service, never()).makeAuth(any(SaleData.class), any(Response.class), any(Operation.class), anyList());
    }

    @Test
    public void testPerformOperationThrowsExceptionStatusInProgress() throws Exception {
        //given

        doReturn(Operation.SALE).when(request).getOperation();
        doReturn(ResponseStatus.IN_PROGRESS).when(response).getStatus();
        doReturn(response).when(terminalConnector).processOperation(request);

        //when
        try {
            service.performOperation(saleDataMock, request);
            fail("Expected BankCommunicationException but never throws");
        } catch (BankCommunicationException e) {

        }
        //then
        verify(operationExecutor).execute(slipsReceiver);
        verify(terminalConnector).processOperation(request);
        verify(response).getStatus();
        verify(request, never()).getOperation();
        verify(slipsReceiver, never()).getSlips(anyCollection(), anyLong(), any(TimeUnit.class));
        verify(service, never()).makeAuth(any(SaleData.class), any(Response.class), any(Operation.class), anyList());
    }

    //makeAuth test
    @Test
    public void testMakeAuthForSale() throws Exception {
        testMakeAuth(ResponseStatus.SUCCESS, saleDataMock, 1, true, false, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForSaleOperationFromResponse() throws Exception {
        testMakeAuth(ResponseStatus.SUCCESS, saleDataMock, 2, true, true, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForReversal() throws Exception {
        testMakeAuth(ResponseStatus.SUCCESS, reversalDataMock, 1, true, false, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForReversalOperationFromResponse() throws Exception {
        testMakeAuth(ResponseStatus.SUCCESS, reversalDataMock, 2, true, true, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForRefund() throws Exception {
        testMakeAuth(ResponseStatus.SUCCESS, refundDataMock, 1, true, false, Operation.REFUND, BankOperationType.REFUND);
    }

    @Test
    public void testMakeAuthForRefundOperationFromResponse() throws Exception {
        testMakeAuth(ResponseStatus.SUCCESS, refundDataMock, 2, true, true, Operation.REFUND, BankOperationType.REFUND);

    }
    //makeAuth test exception

    @Test
    public void testMakeAuthForSaleThrowsExceptionStatusCanceled() throws Exception {
        testMakeAuth(ResponseStatus.CANCELED, saleDataMock, 1, false, false, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForSaleThrowsExceptionStatusBusy() throws Exception {
        testMakeAuth(ResponseStatus.BUSY, saleDataMock, 1, false, false, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForSaleThrowsExceptionStatusInProgress() throws Exception {
        testMakeAuth(ResponseStatus.IN_PROGRESS, saleDataMock, 1, false, false, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForSaleOperationFromResponseThrowsExceptionStatusCanceled() throws Exception {
        testMakeAuth(ResponseStatus.CANCELED, saleDataMock, 2, false, true, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForSaleFromResponseThrowsExceptionStatusBusy() throws Exception {
        testMakeAuth(ResponseStatus.BUSY, saleDataMock, 2, false, true, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForSaleThrowsFromResponseExceptionStatusInProgress() throws Exception {
        testMakeAuth(ResponseStatus.IN_PROGRESS, saleDataMock, 2, false, true, Operation.SALE, BankOperationType.SALE);
    }

    @Test
    public void testMakeAuthForReversalThrowsExceptionStatusCanceled() throws Exception {
        testMakeAuth(ResponseStatus.CANCELED, reversalDataMock, 1, false, false, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForReversalThrowsExceptionStatusBusy() throws Exception {
        testMakeAuth(ResponseStatus.BUSY, reversalDataMock, 1, false, false, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForReversalThrowsExceptionStatusInProgress() throws Exception {
        testMakeAuth(ResponseStatus.IN_PROGRESS, reversalDataMock, 1, false, false, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForReversalOperationFromResponseThrowsExceptionStatusCanceled() throws Exception {
        testMakeAuth(ResponseStatus.CANCELED, reversalDataMock, 2, false, true, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForReversalFromResponseThrowsExceptionStatusBusy() throws Exception {
        testMakeAuth(ResponseStatus.BUSY, reversalDataMock, 2, false, true, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForReversalThrowsFromResponseExceptionStatusInProgress() throws Exception {
        testMakeAuth(ResponseStatus.IN_PROGRESS, reversalDataMock, 2, false, true, Operation.REVERSAL, BankOperationType.REVERSAL);
    }

    @Test
    public void testMakeAuthForRefundThrowsExceptionStatusCanceled() throws Exception {
        testMakeAuth(ResponseStatus.CANCELED, refundDataMock, 1, false, false, Operation.REFUND, BankOperationType.REFUND);
    }

    @Test
    public void testMakeAuthForRefundThrowsExceptionStatusBusy() throws Exception {
        testMakeAuth(ResponseStatus.BUSY, refundDataMock, 1, false, false, Operation.REFUND, BankOperationType.REFUND);
    }

    @Test
    public void testMakeAuthForRefundThrowsExceptionStatusInProgress() throws Exception {
        testMakeAuth(ResponseStatus.IN_PROGRESS, refundDataMock, 1, false, false, Operation.REFUND, BankOperationType.REFUND);
    }

    @Test
    public void testMakeAuthForRefundOperationFromResponseThrowsExceptionStatusCanceled() throws Exception {
        testMakeAuth(ResponseStatus.CANCELED, refundDataMock, 2, false, true, Operation.REFUND, BankOperationType.REFUND);
    }

    @Test
    public void testMakeAuthForRefundFromResponseThrowsExceptionStatusBusy() throws Exception {
        testMakeAuth(ResponseStatus.BUSY, refundDataMock, 2, false, true, Operation.REFUND, BankOperationType.REFUND);
    }

    @Test
    public void testMakeAuthForRefundThrowsFromResponseExceptionStatusInProgress() throws Exception {
        testMakeAuth(ResponseStatus.IN_PROGRESS, refundDataMock, 2, false, true, Operation.REFUND, BankOperationType.REFUND);
    }

    private void testMakeAuth(ResponseStatus responseStatus, SaleData saleData, int timesForGetOperation, boolean operationSuccess, boolean operationFromResponse,
                              Operation operation, BankOperationType bankOperationType) throws Exception {
        //given
        doReturn(bankCard).when(service).makeCard(any(Response.class));
        doReturn(responseStatus).when(response).getStatus();
        doReturn(parsedSlips).when(service).getSlips(anyCollection(), anyList());
        doReturn(ANY_STRING).when(service).getMessage(any(DocumentType.class), anyList(), anyBoolean());
        doReturn(operationFromResponse ? operation : null).when(response).getOperation();
        //when
        AuthorizationData result = null;
        try {
            result = service.makeAuth(saleData, response, operation, slips);
            if (!operationSuccess) {
                fail("Expected BankAuthorizationException but never throws");
            }
        } catch (BankAuthorizationException e) {
            result = e.getAuthorizationData();
            if (operationSuccess) {
                throw e;
            }
        }
        verifyMakeAuth(result, saleData, timesForGetOperation, operationSuccess, operation, bankOperationType);
    }

    private void verifyMakeAuth(AuthorizationData result, SaleData saleData, int timesForGetOperation, boolean operationSuccess, Operation operation,
                                BankOperationType bankOperationType) {
        verify(saleData).getAmount();
        assertThat(result.getAmount()).isEqualTo(TEST_AMOUNT);
        verify(response, times(timesForGetOperation)).getOperation();
        assertThat(result.getOperationType()).isEqualTo(bankOperationType);
        assertThat(result.getDate()).isNotNull();
        verify(response).getBankAnswer();
        assertThat(result.getAuthCode()).isEqualTo(TEST_BANK_ANSWER);
        verify(service).isPrintNegativeSlip();
        assertThat(result.isPrintNegativeSlip()).isEqualTo(service.isPrintNegativeSlip());
        verify(response).getUniqueNumber();
        assertThat(result.getRefNumber()).isEqualTo(TEST_REF_NUMBER);
        assertThat(result.getOperationCode()).isEqualTo((long) operation.getOperationCode());
        verify(response).getStatus();
        assertThat(result.isStatus()).isEqualTo(operationSuccess);
        //Проверка выполнения fillOperationStatusDependentFields
        assertThat(result.getSlips()).isSameAs(parsedSlips);
        if (operationSuccess) {
            verify(service).fillOperationStatusDependentFields(result, true, slips);
            verify(service).getSlips(Operation.SUCCESS_DOCUMENTS, slips);
            verify(service).getMessage(null, slips, true);
        } else {
            verify(service).fillOperationStatusDependentFields(result, false, slips);
            verify(service).getSlips(Operation.FAILED_DOCUMENTS, slips);
            verify(service).getMessage(DocumentType.CASHIER_NOTIFY, slips, false);
        }
        assertThat(result.getMessage()).isEqualTo(ANY_STRING);
    }
}
