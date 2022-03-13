package ru.crystals.pos.bank.arcom;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.arcom.operations.CashierMenuOperation;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankConfigException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.crystals.pos.bank.BankPlugin.LOYALTY_PROGRAM_CODE_NAME;

public class BankArcomServiceImplTest {

    private List<String> successfulSlip = asList(
            "Первая ",
            "Вторая",
            "Третья",
            "Четвертая",
            "Пятая",
            "Шестая",
            "Седьмая");

    private List<String> successful = asList(
            "000",
            "4475209900082012",
            "R0000009",
            "582172",
            "VISA",
            "УСПЕШНО",
            "04.00",
            "123456789012");

    private List<String> successfulDailyLog = asList(
            "000",
            "",
            "R0000009",
            "582172",
            "",
            "УСПЕШНО",
            "");

    private List<String> unsuccessful = asList(
            "999",
            "",
            "R0000009",
            "",
            "",
            "Пинпад не отвечает",
            "");

    private Answer<Object> uniAnswer = inv -> null;
    private Answer<List<String>> chequeListAnswer = inv -> new ArrayList<>(successfulSlip);
    private Answer<List<String>> unsuccessfullAnswer = inv -> new ArrayList<>(unsuccessful);
    private Answer<List<String>> successfullAnswer = inv -> new ArrayList<>(successful);

    private BankArcomServiceImpl bank;

    private ResponseData responseData = new ArcomResponseData();

    @Before
    public void setup() throws CashException {
        bank = spy(new BankArcomServiceImpl());
        doAnswer(uniAnswer).when(bank).start();
        doAnswer(uniAnswer).when(bank).runExecutable(anyListOf(String.class));
        doAnswer(uniAnswer).when(bank).clearLastSale();
        doAnswer(chequeListAnswer).when(bank).readSlipFile();
        bank.start();
        bank.setResponseData(responseData);
        bank.setListeners(Collections.emptyList());
        doReturn("643").when(bank).getCurrencyCodeByName("RUB");
    }

    private LastSale getLastSale() throws IOException {
        LastSale lastSale = spy(new LastSale());
        doAnswer(uniAnswer).when(lastSale).writeProperties();
        doAnswer(uniAnswer).when(lastSale).loadProperties();
        Properties lastSalePeoperties = new Properties();
        lastSalePeoperties.setProperty(LastSale.CASH_TRANS_ID, "12345");
        lastSalePeoperties.setProperty(LastSale.AMOUNT, "10000");
        lastSalePeoperties.setProperty(LastSale.TIME_STAMP, Long.toString(System.currentTimeMillis()));
        lastSalePeoperties.setProperty(LastSale.LAST_OPERATION, Boolean.TRUE.toString());
        lastSale.setProperties(lastSalePeoperties);
        bank.setLastSale(lastSale);
        return lastSale;
    }

    private ReversalData getSaleDataForRefund() {
        ReversalData sd = new ReversalData();
        sd.setCashTransId(12345L);
        sd.setAmount(10000L);
        sd.setRefNumber("000123456789");
        return sd;
    }

    @Test
    public void getReversalOperationType_Reversal() throws IOException {
        getLastSale();
        ReversalData sd = getSaleDataForRefund();
        assertTrue(bank.canBeProcessedAsReversal(sd));
    }

    @Test
    public void getReversalOperationType_Refund_ifCashTransIdNotEquals() throws IOException {
        getLastSale();
        ReversalData sd = getSaleDataForRefund();
        sd.setCashTransId(sd.getCashTransId() + 1);
        assertFalse(bank.canBeProcessedAsReversal(sd));
    }

    @Test
    public void getReversalOperationType_Refund_amountNotEquals() throws IOException {
        getLastSale();
        ReversalData sd = getSaleDataForRefund();
        sd.setAmount(sd.getAmount() + 1);
        assertFalse(bank.canBeProcessedAsReversal(sd));
    }

    @Test
    public void getReversalOperationType_Refund_oldOperation() throws IOException {
        LastSale lastSale = getLastSale();
        ReversalData sd = getSaleDataForRefund();
        lastSale.getProperties().put(LastSale.TIME_STAMP,
                Long.toString(System.currentTimeMillis() - LastSale.LAST_OPERATION_REVERSAL_TIMEOUT - 1));
        assertFalse(bank.canBeProcessedAsReversal(sd));
    }

    @Test
    public void getReversalOperationType_Refund_notLastOperation() throws IOException {
        LastSale lastSale = getLastSale();
        ReversalData sd = getSaleDataForRefund();
        lastSale.getProperties().put(LastSale.LAST_OPERATION, Boolean.FALSE.toString());

        assertFalse(bank.canBeProcessedAsReversal(sd));
    }

    @Test
    public void saleSuccessful() throws CashException {
        Long amount = 10000L;
        SaleData sd = new SaleData();
        sd.setAmount(amount);
        sd.setCurrencyCode("RUB");
        doAnswer(uniAnswer).when(bank).saveLastSale(responseData, sd);
        doAnswer(successfullAnswer).when(bank).readResponseFile();
        AuthorizationData ad = bank.sale(sd);
        verify(bank).runExecutableAndGetResponseData(asList("/o1", "/a" + amount, "/c643"));
        verifySuccessfulOperation(ad, BankOperationType.SALE, 1L);
        verify(bank).saveLastSale(responseData, sd);
    }

    @Test
    public void saleSuccessfulOneSlip() {
        AuthorizationData ad = new AuthorizationData();

        final int slipCount = 1;
        bank.setInnerSlipCount(slipCount);
        bank.makeSlip(ad, new ArcomResponseData().parseResponseFile(singletonList("000")), successfulSlip, BankOperationType.SALE);
        Assert.assertEquals(slipCount, ad.getSlips().size());

        bank.setInnerSlipCount(2);
    }

    @Test
    public void saleSuccessfulTwoSlip() {
        AuthorizationData ad = new AuthorizationData();

        final int slipCount = 2;
        bank.setInnerSlipCount(slipCount);
        bank.makeSlip(ad, new ArcomResponseData().parseResponseFile(singletonList("000")), successfulSlip, BankOperationType.SALE);
        Assert.assertEquals(slipCount, ad.getSlips().size());
    }

    @Test
    public void saleSuccessfulManySlip() {
        AuthorizationData ad = new AuthorizationData();

        final int slipCount = 10;
        bank.setInnerSlipCount(slipCount);
        bank.makeSlip(ad, new ArcomResponseData().parseResponseFile(singletonList("000")), successfulSlip, BankOperationType.SALE);
        Assert.assertEquals(slipCount, ad.getSlips().size());

        bank.setInnerSlipCount(2);
    }

    @Test
    public void saleSuccessfulWithTerminalID() throws CashException {
        Long amount = 10000L;
        SaleData sd = new SaleData();
        sd.setAmount(amount);
        sd.setCurrencyCode("RUB");
        sd.setFirstFiscalPrinter(true);
        doAnswer(uniAnswer).when(bank).saveLastSale(responseData, sd);
        doAnswer(successfullAnswer).when(bank).readResponseFile();
        bank.setFirstFiscalPrinterTerminalID("1");
        bank.setSecondFiscalPrinterTerminalID("2");
        AuthorizationData ad = bank.sale(sd);
        verify(bank).runExecutableAndGetResponseData(asList("/o1", "/a" + amount, "/c643,1"));
        verifySuccessfulOperation(ad, BankOperationType.SALE, 1L);
        verify(bank).saveLastSale(responseData, sd);
    }

    @Test
    public void saleUnsuccessful() throws CashException {
        SaleData sd = new SaleData();
        sd.setAmount(1000L);
        doAnswer(unsuccessfullAnswer).when(bank).readResponseFile();
        doAnswer(uniAnswer).when(bank).saveLastSale(null, sd);
        bank.start();
        try {
            AuthorizationData ad = bank.sale(sd);
            assertFalse("Не вернулось сообщение об ошибке", ad.isStatus());
        } catch (BankException e) {
            AuthorizationData ad = e.getAuthorizationData();
            assertNotNull(ad);
            assertEquals("Вернулось некорректное сообщение об ошибке", "Пинпад не отвечает", e.getAuthorizationData().getMessage());
            verify(bank).runExecutableAndGetResponseData(asList("/o1", "/a" + sd.getAmount()));
            verify(bank, never()).saveLastSale(null, sd);
            verify(bank, never()).clearLastSale();
        }
    }

    @Test
    public void refundSuccessful() throws CashException {
        doReturn(new ArrayList<>(successfulSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successful)).when(bank).readResponseFile();
        doAnswer(uniAnswer).when(bank).clearLastSale();
        RefundData rd = new RefundData();
        rd.setAmount(10000L);
        rd.setCurrencyCode("RUB");
        AuthorizationData ad = bank.refund(rd);
        verify(bank).runExecutableAndGetResponseData(asList("/o3", "/a" + rd.getAmount(), "/c" + "643"));
        verify(bank).clearLastSale();
        verifySuccessfulOperation(ad, BankOperationType.REFUND, 3L);
    }

    @Test
    public void refundUnsuccessful() throws CashException {
        RefundData rd = new RefundData();
        rd.setAmount(10000L);
        doAnswer(unsuccessfullAnswer).when(bank).readResponseFile();
        doAnswer(uniAnswer).when(bank).saveLastSale(null, rd);
        bank.start();
        try {
            AuthorizationData ad = bank.refund(rd);
            assertFalse("Не вернулось сообщение об ошибке", ad.isStatus());
        } catch (BankException e) {
            AuthorizationData ad = e.getAuthorizationData();
            assertNotNull(ad);
            assertEquals("Вернулось некорректное сообщение об ошибке", "Пинпад не отвечает", e.getAuthorizationData().getMessage());
            verify(bank).runExecutableAndGetResponseData(asList("/o3", "/a" + rd.getAmount()));
            verify(bank, never()).clearLastSale();
        }
    }

    @Test
    public void reversalSuccessful() throws CashException, IOException {
        doReturn(new ArrayList<>(successfulSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successful)).when(bank).readResponseFile();
        getLastSale();
        ReversalData rd = getSaleDataForRefund();
        doAnswer(uniAnswer).when(bank).clearLastSale();

        AuthorizationData ad = bank.reversal(rd);
        verify(bank).runExecutableAndGetResponseData(singletonList("/o2"));
        verify(bank).clearLastSale();
        verifySuccessfulOperation(ad, BankOperationType.REVERSAL, 2L);
    }

    @Test
    public void reversalUnsuccessful() throws CashException, IOException {
        ReversalData rd = getSaleDataForRefund();
        getLastSale();
        doAnswer(unsuccessfullAnswer).when(bank).readResponseFile();
        doAnswer(uniAnswer).when(bank).saveLastSale(null, rd);
        doAnswer(uniAnswer).when(bank).clearLastSale();
        bank.start();
        try {
            AuthorizationData ad = bank.reversal(rd);
            assertFalse("Не вернулось сообщение об ошибке", ad.isStatus());
        } catch (BankException e) {
            verify(bank).runExecutableAndGetResponseData(singletonList("/o2"));
            AuthorizationData ad = e.getAuthorizationData();
            assertNotNull(ad);
            assertEquals("Вернулось некорректное сообщение об ошибке", "Пинпад не отвечает", e.getAuthorizationData().getMessage());
            verify(bank).clearLastSale();
        }
    }

    @Test
    public void reversalFallBackToRefundSuccessful() throws CashException, IOException {
        doReturn(new ArrayList<>(successfulSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successful)).when(bank).readResponseFile();
        LastSale lastSale = getLastSale();
        lastSale.getProperties().put(LastSale.LAST_OPERATION, Boolean.FALSE.toString());
        lastSale.getProperties().put(LastSale.TIME_STAMP,
                Long.toString(System.currentTimeMillis() - LastSale.LAST_OPERATION_REVERSAL_TIMEOUT - 1));
        ReversalData rd = getSaleDataForRefund();
        doAnswer(uniAnswer).when(bank).clearLastSale();

        AuthorizationData ad = bank.reversal(rd);
        verify(bank).runExecutableAndGetResponseData(asList("/o3", "/a" + rd.getAmount(), "/r" + rd.getRefNumber()));
        verify(bank).clearLastSale();
        verifySuccessfulOperation(ad, BankOperationType.REFUND, 3L);
    }

    @Test
    public void dailyLogSuccessful() throws CashException {
        List<String> expectedSlip = new ArrayList<>(successfulDailyLog);
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readResponseFile();
        try {
            DailyLogData dld = bank.dailyLog(anyLong());
            verify(bank).runExecutableAndGetResponseData(singletonList("/o12"));
            assertNotNull(dld);
            assertEquals(expectedSlip, dld.getSlip());
        } catch (BankException e) {
            fail("Для успешной операции выброшено исключение");
        }
    }

    @Test
    public void dailyLogUnsuccessful() throws CashException {
        List<String> expectedSlip = new ArrayList<>(unsuccessful);
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readResponseFile();
        try {
            bank.dailyLog(anyLong());
            fail("Не выброшено исключение при возникновении ошибки");
        } catch (BankException e) {
            assertEquals("Текст сообщения об ошибке не совпадает с результатом", "Пинпад не отвечает", e.getMessage());
        } catch (Exception e) {
            fail("Выброшен некорректный тип исключения " + e.getClass().getName());
        }
    }

    @Test
    public void cashierMenu() throws CashException {
        bank.processServiceOperation(new CashierMenuOperation());
        verify(bank).runExecutable(singletonList("/o98"));
    }

    private void verifySuccessfulOperation(AuthorizationData ad, BankOperationType operation, Long operationCode) {
        assertNotNull(ad);
        assertEquals("getResponseCode", "000", ad.getResponseCode());
        assertEquals("getCard().getCardNumber()", BankUtils.maskCardNumber("4475209900082012"), ad.getCard().getCardNumber());
        assertEquals("getTerminalId", "R0000009", ad.getTerminalId());
        assertEquals("getAuthCode", "582172", ad.getAuthCode());
        assertEquals("getCard().getCardType()", "VISA", ad.getCard().getCardType());
        assertEquals("getMessage", "УСПЕШНО", ad.getMessage());
        assertEquals("getOperationCode", operationCode, ad.getOperationCode());
        assertEquals("getOperationType", operation, ad.getOperationType());
        assertEquals("getRefNumber", "123456789012", ad.getRefNumber());
        assertTrue("isStatus", ad.isStatus());
        List<List<String>> fullSlip = new ArrayList<>();
        fullSlip.add(new ArrayList<>(successfulSlip));
        fullSlip.add(new ArrayList<>(successfulSlip));
        assertEquals(fullSlip, ad.getSlips());
    }

    @Test(expected = BankConfigException.class)
    public void assumeFileNotFound() throws BankConfigException {
        new BankArcomServiceImpl().getProperties("notExistedFile.ini", "utf-8");
    }

    @Test
    public void assumeGetProperty() throws BankConfigException, IOException {
        File arcomConfigFile = new File("ArcomTest.ini");
        String keyName = "Key";
        String keyValue = "Значение";
        List<String> config = new ArrayList<>();
        config.add(keyName + " = " + keyValue);
        try {
            FileUtils.writeLines(arcomConfigFile, BankArcomServiceImpl.INI_FILE_CHARSET, config);
            assertEquals(keyValue, new BankArcomServiceImpl().getProperties(arcomConfigFile.getName(), "utf-8").getProperty(keyName));
        } finally {
            arcomConfigFile.delete();
        }
    }

    @Test
    public void cancelByBankSuccessful() throws CashException {
        doReturn(new ArrayList<>(successfulSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successful)).when(bank).readResponseFile();
        doAnswer(uniAnswer).when(bank).clearLastSale();

        AuthorizationData ad = bank.cancelAtBank();

        verify(bank).runExecutableAndGetResponseData(singletonList("/o5"));
        verify(bank).clearLastSale();
        verifySuccessfulOperation(ad, BankOperationType.CANCEL_AT_BANK, 5L);
    }

    @Test
    public void saleWithShowMessage() throws BankException {
        Long amount = 10000L;
        SaleData sd = new SaleData();
        sd.setAmount(amount);
        sd.setCurrencyCode("RUB");
        doAnswer(uniAnswer).when(bank).saveLastSale(responseData, sd);
        doAnswer(successfullAnswer).when(bank).readResponseFile();
        when(bank.isShowStatusMessage()).thenReturn(true);
        AuthorizationData ad = bank.sale(sd);
        verify(bank).runExecutableAndGetResponseData(asList("/o1", "/a" + amount, "/c643", "/console"));
        verifySuccessfulOperation(ad, BankOperationType.SALE, 1L);
        verify(bank).saveLastSale(responseData, sd);
    }

    @Test
    public void refundSuccessfulWithShowMessage() throws CashException {
        doReturn(new ArrayList<>(successfulSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successful)).when(bank).readResponseFile();
        doAnswer(uniAnswer).when(bank).clearLastSale();
        when(bank.isShowStatusMessage()).thenReturn(true);
        RefundData rd = new RefundData();
        rd.setAmount(10000L);
        rd.setCurrencyCode("RUB");
        AuthorizationData ad = bank.refund(rd);
        verify(bank).runExecutableAndGetResponseData(asList("/o3", "/a" + rd.getAmount(), "/c" + "643", "/console"));
        verify(bank).clearLastSale();
        verifySuccessfulOperation(ad, BankOperationType.REFUND, 3L);
    }

    @Test
    public void cancelByBankSuccessfulWithShowMessage() throws CashException {
        doReturn(new ArrayList<>(successfulSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successful)).when(bank).readResponseFile();
        doAnswer(uniAnswer).when(bank).clearLastSale();
        when(bank.isShowStatusMessage()).thenReturn(true);

        AuthorizationData ad = bank.cancelAtBank();

        verify(bank).runExecutableAndGetResponseData(singletonList("/o5"));
        verify(bank).clearLastSale();
        verifySuccessfulOperation(ad, BankOperationType.CANCEL_AT_BANK, 5L);
    }

}
