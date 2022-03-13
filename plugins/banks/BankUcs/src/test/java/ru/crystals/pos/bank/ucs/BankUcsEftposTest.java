package ru.crystals.pos.bank.ucs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.BankEvent;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.messages.requests.CreditRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.bank.ucs.messages.requests.SaleRequest;
import ru.crystals.pos.bank.ucs.serviceoperations.GetFullReportOperation;
import ru.crystals.pos.bank.ucs.serviceoperations.GetShortReportOperation;
import ru.crystals.pos.bank.ucs.serviceoperations.GetSlipCopyOperation;
import ru.crystals.pos.bank.ucs.serviceoperations.UCSServiceOperation;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankUcsEftposTest {
    private static final long TEST_CASH_TRANS_ID = 123L;
    private static final long TEST_SALE_AMOUNT = 10000L;
    @Mock
    private EftposConnector connector;
    @Mock
    private RequestManager requestManager;
    @Mock
    private TerminalConfiguration terminalConfiguration;
    @Mock
    private BankUcsEftposMessageListener terminalMessageListener;
    @Mock
    private UCSServiceOperation operation;
    @Mock
    private Request request;
    @Mock
    private LastOperation lastOperation;
    @Mock
    private DailyLogData dailyLogData;
    @InjectMocks @Spy
    private BankUcsEftpos bank = new BankUcsEftpos();

    @Before
    public void setUp() throws Exception {
        when(requestManager.makeTransaction(any(Request.class))).thenReturn(new AuthorizationData());
        doNothing().when(terminalMessageListener).addAll(anyCollectionOf(BankEvent.class));
        when(lastOperation.getLastTransactionID()).thenReturn("1");
    }

    @Test
    public void testStart() throws Exception {

        //when
        bank.start();

        //then
        verify(connector).init();
        verify(connector).setTerminalConfiguration(any(TerminalConfiguration.class));
        verify(requestManager).setTerminalMessageListener(any(TerminalMessageListener.class));
        verify(requestManager).setConnector(connector);
    }

    @Test
    public void testStartWithTerminalIDAutoDetection() throws BankException {

        //given
        when(terminalConfiguration.isUseTerminalIDAutoDetection()).thenReturn(true);
        bank.setTerminalID("1234567890");
        bank.setTerminalConfiguration(terminalConfiguration);

        //when
        bank.start();

        //then
        assertThat(bank.getTerminalID()).isNull();
        verify(connector).init();
        verify(connector).setTerminalConfiguration(any(TerminalConfiguration.class));
        verify(requestManager).setTerminalMessageListener(any(TerminalMessageListener.class));
        verify(requestManager).setConnector(connector);
    }

    @Test
    public void testStop() throws Exception {
        bank.stop();
        verify(connector).close();
    }

    @Test
    public void testSale() throws Exception {

        //given
        SaleData saleData = new SaleData();
        saleData.setAmount(TEST_SALE_AMOUNT);
        saleData.setCashTransId(TEST_CASH_TRANS_ID);
        saleData.setCurrencyCode("RUB");

        //when
        AuthorizationData authorizationData = bank.sale(saleData);

        //then
        verify(requestManager).makeTransaction(new SaleRequest(saleData));
        assertThat(authorizationData.getOperationType()).isEqualTo(BankOperationType.SALE);
        assertThat(authorizationData.getCashTransId()).isEqualTo(saleData.getCashTransId());
    }

    @Test
    public void testRefund() throws Exception {

        //given
        RefundData refundData = new RefundData();
        refundData.setAmount(TEST_SALE_AMOUNT);
        refundData.setCashTransId(TEST_CASH_TRANS_ID);
        refundData.setCurrencyCode("RUB");

        //when
        AuthorizationData authorizationData = bank.refund(refundData);

        //then
        verify(requestManager).makeTransaction(new CreditRequest(refundData));
        assertThat(authorizationData.getOperationType()).isEqualTo(BankOperationType.REFUND);
        assertThat(authorizationData.getCashTransId()).isEqualTo(refundData.getCashTransId());
    }

    @Test
    public void testGetAvailableOperations() {

        // given
        bank.start();

        // when
        List<UCSServiceOperation> availableOperations = bank.getAvailableServiceOperations();

        // then

        List<Class> operationClasses = new ArrayList<>();
        for (UCSServiceOperation availableOperation : availableOperations) {
            operationClasses.add(availableOperation.getClass());
        }
        assertThat(operationClasses).contains(GetFullReportOperation.class);
        assertThat(operationClasses).contains(GetShortReportOperation.class);
        assertThat(operationClasses).contains(GetSlipCopyOperation.class);
        verify(lastOperation).getLastTransactionID();
    }

    @Test
    public void testDailyLog() throws BankException {

        // given
        when(requestManager.dailyLog()).thenReturn(dailyLogData);

        // when
        DailyLogData result = bank.dailyLog(TEST_CASH_TRANS_ID);

        // then
        assertThat(result).isSameAs(dailyLogData);
        verify(lastOperation).clear();
    }
}
