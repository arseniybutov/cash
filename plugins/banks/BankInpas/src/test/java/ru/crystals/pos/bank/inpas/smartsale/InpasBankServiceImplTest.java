package ru.crystals.pos.bank.inpas.smartsale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.InpasConstants;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankOpenPortException;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InpasBankServiceImplTest {
    private String port = "COM1";
    private String baudRate = "9600";
    private String dataBits = "8";
    private String stopBits = "1";
    private String parity = "NONE";
    @Mock
    private InpasConnector connector;
    @Mock
    private TCPConnector tcpConnector;
    @Spy
    @InjectMocks
    private InpasBankServiceImpl service = new InpasBankServiceImpl();

    @Test
    public void testStart() throws Exception {
        doNothing().when(connector).open(port, baudRate, dataBits, stopBits, parity);

        service.start();

        verify(connector).open(port, baudRate, dataBits, stopBits, parity);
    }

    @Test(expected = BankOpenPortException.class)
    public void testStartFail() throws Exception {
        doThrow(new Exception()).when(connector).open(port, baudRate, dataBits, stopBits, parity);

        service.start();
    }

    @Test
    public void testRequestTerminalStateIfOffline() throws Exception {
        FieldCollection f = new FieldCollection();
        doReturn(f).when(service).executeCommand(InpasConstants.CHECK_STATE);
        doReturn(new FieldCollection()).when(service).processTerminalResponse(f);

        boolean b = service.requestTerminalStateIfOffline();

        verify(service).createNewData();
        verify(service).executeCommand(InpasConstants.CHECK_STATE);
        assertTrue(b);
    }

    @Test
    public void testRequestTerminalStateIfOfflineFalse() throws Exception {
        doThrow(new BankException()).when(service).executeCommand(anyLong());

        boolean b = service.requestTerminalStateIfOffline();

        assertFalse(b);
    }

    @Test
    public void testExecuteCommand() throws Exception {
        service.data = mock(FieldCollection.class);

        service.executeCommand(InpasConstants.CHECK_STATE);

        verify(service.data).setCashDateTime(any(LocalDateTime.class));
        verify(service.data).setTerminalId(anyString());
        verify(service.data).setOperationCode(InpasConstants.CHECK_STATE);
        verify(connector).sendPacket(service.data);
        verify(connector).readPacket();
    }

    @Test(expected = Exception.class)
    public void testExecuteCommandException() throws Exception {
        service.executeCommand(1);

        verify(connector, never()).sendPacket(any(FieldCollection.class));
        verify(connector, never()).readPacket();
    }

    @Test
    public void testSale() throws Exception {
        SaleData saleData = new SaleData();
        saleData.setAmount(250L);
        saleData.setCurrencyCode("RUB");
        saleData.setCashTransId(110005L);
        when(connector.readPacket()).thenReturn(new FieldCollection());
        doReturn(new FieldCollection()).when(service).processTerminalResponse(any(FieldCollection.class));
        doNothing().when(service).setAuthorizationDataMessage(any(AuthorizationData.class));
        doNothing().when(service).setAthorizationDataSlips(any(AuthorizationData.class), any(FieldCollection.class));

        service.sale(saleData);

        verify(service).createNewData();
        assertThat(service.data.getAmount()).isEqualTo(saleData.getAmount());
        assertThat(service.data.getCurrencyCode()).isEqualTo(643);
        assertThat(service.data.getCashTransId()).isEqualTo(saleData.getCashTransId());
        verify(service).processBankOperation(InpasConstants.SALE);
    }

    @Test
    public void testProcessCommand1() throws Exception {
        AuthorizationData ad = proccessCommand(1);
        assertThat(ad.isStatus()).isTrue();
    }

    @Test
    public void testProcessCommand17() throws Exception {
        AuthorizationData ad = proccessCommand(17);
        assertThat(ad.isStatus()).isTrue();
    }

    @Test
    public void testProcessCommand2() throws Exception {
        AuthorizationData ad = proccessCommand(2);
        assertThat(ad.isStatus()).isFalse();
    }

    private AuthorizationData proccessCommand(long resultCode) throws Exception {
        FieldCollection collection = new FieldCollection();
        doReturn(collection).when(service).executeCommand(eq(InpasConstants.SALE), any(InpasConnector.class));

        AuthorizationData authorizationData = new AuthorizationData();
        authorizationData.setResultCode(resultCode);
        doReturn(authorizationData).when(service).processTerminalResponseAuthorizationData(collection);
        doNothing().when(service).setAuthorizationDataMessage(authorizationData);
        doNothing().when(service).setAthorizationDataSlips(authorizationData, collection);

        service.processBankOperation(InpasConstants.SALE);

        verify(service).executeCommand(eq(InpasConstants.SALE), any(InpasConnector.class));
        verify(service).processTerminalResponseAuthorizationData(collection);
        verify(service).setAuthorizationDataMessage(authorizationData);
        verify(service).setAthorizationDataSlips(authorizationData, collection);
        return authorizationData;
    }

    @Test(expected = BankException.class)
    public void testProcessCommandException() throws Exception {
        doThrow(new BankException()).when(service).executeCommand(eq(InpasConstants.SALE), any(InpasConnector.class));

        service.processBankOperation(InpasConstants.SALE);
    }

    @Test(expected = BankException.class)
    public void testReversalFail() throws Exception {
        service.setCancelPossible(false);
        service.reversal(new ReversalData());
        verify(service, never()).processBankOperation(anyLong());
    }

    @Test
    public void testReversal() throws Exception {
        ReversalData reversalData = new ReversalData();
        reversalData.setOriginalSaleTransactionAmount(1L);
        testReversal(reversalData);
        verify(service).processBankOperation(InpasConstants.REVERSAL);
    }

    @Test
    public void testReversalRefund() throws Exception {
        ReversalData reversalData = new ReversalData();
        reversalData.setOriginalSaleTransactionAmount(2L);
        testReversal(reversalData);
        verify(service).processBankOperation(InpasConstants.REFUND);
    }

    @Test
    public void testReversalPartial() throws Exception {
        ReversalData reversalData = new ReversalData();
        reversalData.setOriginalSaleTransactionAmount(2L);
        service.setUsePartialReversal(true);
        testReversal(reversalData);
        assertThat(service.data.getAdditionalAmount()).isEqualTo(reversalData.getOriginalSaleTransactionAmount());
        verify(service).processBankOperation(InpasConstants.REVERSAL);
    }

    public void testReversal(ReversalData reversalData) throws Exception {
        reversalData.setAmount(1L);
        reversalData.setCurrencyCode("RUB");
        reversalData.setAuthCode("SSS");
        reversalData.setRefNumber("000000000001");
        reversalData.setCashTransId(1L);
        doReturn(new AuthorizationData()).when(service).processBankOperation(anyLong());

        service.reversal(reversalData);

        assertThat(service.data.getAmount()).isEqualTo(reversalData.getAmount());
        assertThat(service.data.getCurrencyCode()).isEqualTo(643);
        assertThat(service.data.getAuthCode()).isEqualTo(reversalData.getAuthCode());
        assertThat(service.data.getRefNumber()).isEqualTo(reversalData.getRefNumber());
        assertThat(service.data.getRefNumber()).isEqualTo("000000000001");
        assertThat(service.data.getCashTransId()).isEqualTo(reversalData.getCashTransId());
    }

    @Test(expected = BankException.class)
    public void testRefundFail() throws Exception {
        service.setCancelPossible(false);
        service.refund(new ReversalData());
        verify(service, never()).processBankOperation(anyLong());
    }

    @Test
    public void testRefund() throws Exception {
        RefundData refundData = new RefundData();
        refundData.setAmount(1L);
        refundData.setCurrencyCode("RUB");
        refundData.setAuthCode("SSS");
        refundData.setRefNumber("000000000001");
        refundData.setCashTransId(1L);
        doReturn(new AuthorizationData()).when(service).processBankOperation(anyLong());

        service.refund(refundData);

        assertThat(service.data.getAmount()).isEqualTo(refundData.getAmount());
        assertThat(service.data.getCurrencyCode()).isEqualTo(643);
        assertThat(service.data.getAuthCode()).isEqualTo(refundData.getAuthCode());
        assertThat(service.data.getRefNumber()).isEqualTo(refundData.getRefNumber());
        assertThat(service.data.getRefNumber()).isEqualTo("000000000001");
        assertThat(service.data.getCashTransId()).isEqualTo(refundData.getCashTransId());
        verify(service).processBankOperation(InpasConstants.REFUND);
    }

    @Test
    public void testRefundWithoutRRN() throws Exception {
        RefundData refundData = new RefundData();
        refundData.setAmount(1L);
        refundData.setCurrencyCode("RUB");
        refundData.setAuthCode("SSS");
        refundData.setCashTransId(1L);
        doReturn(new AuthorizationData()).when(service).processBankOperation(anyLong());

        service.refund(refundData);

        assertThat(service.data.getAmount()).isEqualTo(refundData.getAmount());
        assertThat(service.data.getCurrencyCode()).isEqualTo(643);
        assertThat(service.data.getAuthCode()).isEqualTo(refundData.getAuthCode());
        assertThat(service.data.getRefNumber()).isNull();
        assertThat(service.data.getCashTransId()).isEqualTo(refundData.getCashTransId());
        verify(service).processBankOperation(InpasConstants.REFUND);
    }

    @Test
    public void testDailyLog() throws Exception {
        Long transId = 1L;
        FieldCollection fc = new FieldCollection();
        doReturn(fc).when(service).executeCommand(eq(InpasConstants.DAILY_LOG), any(InpasConnector.class));
        doReturn(fc).when(service).processTerminalResponse(any(FieldCollection.class));
        doReturn(new ArrayList<String>()).when(service).generateBankSlip(any(FieldCollection.class));
        service.dailyLog(transId);

        verify(service).createNewData();
        assertThat(service.data.getCashTransId()).isEqualTo(transId);
        verify(service).executeCommand(eq(InpasConstants.DAILY_LOG), any(InpasConnector.class));
        verify(service).processTerminalResponse(any(FieldCollection.class));
        verify(service).generateBankSlip(fc);
    }

    @Test(expected = BankCommunicationException.class)
    public void testDailyLogException() throws Exception {
        doThrow(new BankException()).when(service).executeCommand(eq(InpasConstants.DAILY_LOG), any(InpasConnector.class));

        service.dailyLog(1L);
    }

    @Test
    public void testProcessTerminalResponse() throws Exception {
        FieldCollection fc = new FieldCollection();
        fc.setOperationCode(InpasConstants.EXECUTE_USER_COMMAND);
        final FieldCollection customFC = new FieldCollection();
        customFC.setOperationCode(InpasConstants.SALE);
        doReturn(customFC).when(service).executeCommand(InpasConstants.EXECUTE_USER_COMMAND);

        FieldCollection answer = service.processTerminalResponse(fc);

        verify(tcpConnector).fillData(fc, service.data);
        verify(service).executeCommand(InpasConstants.EXECUTE_USER_COMMAND);
        assertThat(answer).isEqualTo(customFC);
    }
}
