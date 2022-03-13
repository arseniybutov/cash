package ru.crystals.pos.bank.goldcrownbank;


import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.exception.BankException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class GoldCrownTest {


    private static final String configName = "netcash.cfg";
    private static final String TERMINAL_ID = "J123456";
    private static final String TRANSACTION_NUMBER = "000123456789";
    private static final int PAYMENT_DIRECTION = 5;
    private Answer<Object> voidAnswer = new Answer<Object>() {@Override public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        return null;
    }};
    private Answer<Object> uniAnswer = new Answer<Object>() {@Override public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        return null;
    }};
    private Answer<AuthorizationData> uniAnswerAD = new Answer<AuthorizationData> () {@Override public AuthorizationData answer(InvocationOnMock invocation) {
        return new AuthorizationData();
    }};
    private Answer<List<String>> chequeListAnswer = new Answer<List<String>>() {@Override public List<String> answer(InvocationOnMock invocation) {
        return new ArrayList<>(successfullSlip);
    }};

    private boolean isSuccessfulAnswer = false;
    private Answer<List<String>> differentAnswer = new Answer<List<String>>() {@Override public List<String> answer(InvocationOnMock invocation) {
        isSuccessfulAnswer = !isSuccessfulAnswer;
        return new ArrayList<>(!isSuccessfulAnswer ? successfull : unsuccessfull);
    }};

    private Answer<List<String>> unsuccessfullAnswer = new Answer<List<String>>() {@Override public List<String> answer(InvocationOnMock invocation) {
        return new ArrayList<>(unsuccessfull);
    }};
    private Answer<List<String>> successfullAnswer = new Answer<List<String>>() {@Override public List<String> answer(InvocationOnMock invocation) {
        return new ArrayList<>(successfull);
    }};


    List<String> unsuccessfull = Arrays.asList(
            "10,Все плохо");
    List<String> unsuccessfull2 = Arrays.asList(
            "999,Все очень плохо");

    BankGoldCrownServiceImpl bank;
    @Before
    public void setup() throws CashException {
        bank = spy(new BankGoldCrownServiceImpl());
        doAnswer(uniAnswer).when(bank).start();
        doAnswer(uniAnswer).when(bank).runExecutable(anyListOf(String.class));
        doAnswer(chequeListAnswer).when(bank).readSlipFile();
        bank.start();
        bank.setResponseData(new GoldCrownResponseData());
        bank.setTerminalNumber(TERMINAL_ID);
        bank.setNetcashConfigName(configName);
        bank.setPaymentDirection(PAYMENT_DIRECTION);
    }

    List<String> successfullSlip = Arrays.asList(
            "Первая ",
            "Вторая",
            "Третья",
            "Четвертая",
            "Пятая",
            "Шестая",
            "Седьмая");
    List<String> successfull = Arrays.asList(
            "0,Операция выполнена успешно",
            "28,40,5008046008071110305,************1125,           3.00,810,24/09/2013 18:24:15,0.000000,12,9c2a",
            TRANSACTION_NUMBER);
    List<String> successfullDailyLog = Arrays.asList(
            "0,Операция выполнена успешно",
            "28,40,5008046008071110305,************1125,           3.00,810,24/09/2013 18:24:15,0.000000,12,9c2a",
            TRANSACTION_NUMBER);


    private ReversalData getSaleDataForRefund(){
        ReversalData sd = new ReversalData();
        sd.setMerchantId("12345");
        sd.setAmount(12345L);
        sd.setOriginalSaleTransactionAmount(sd.getAmount());
        return sd;
    }

    @Test
    public void getReversalOperationType_Reversal() throws CashException, IOException {
        ReversalData sd = getSaleDataForRefund();
        assertTrue(bank.canBeProcessedAsReversal(sd));
    }

    @Test
    public void getReversalOperationType_Refund_ifMerchantIdIsNull() throws CashException, IOException {
        ReversalData sd = getSaleDataForRefund();
        sd.setMerchantId(null);
        assertFalse(bank.canBeProcessedAsReversal(sd));
    }
    @Test
    public void getReversalOperationType_Refund_amountNotEquals() throws CashException, IOException {
        ReversalData sd = getSaleDataForRefund();
        sd.setOriginalSaleTransactionAmount(sd.getAmount() + 1);
        assertFalse(bank.canBeProcessedAsReversal(sd));
    }

    @Test
    public void saleSucessfull() throws CashException, IOException, ParseException {
        Long amount = 10000L;
        SaleData sd = new SaleData();
        sd.setAmount(amount);
        doAnswer(successfullAnswer).when(bank).readResponseFile();
        AuthorizationData ad = bank.sale(sd);
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName, "100.00", "-r", Integer.toString(PAYMENT_DIRECTION)));
        verifySuccessfullOperation(ad, BankOperationType.SALE);
    }

    @Test
    public void saleUnsuccessfull() throws CashException {
        SaleData sd = new SaleData();
        sd.setAmount(1000L);
        doAnswer(unsuccessfullAnswer).when(bank).readResponseFile();
        bank.start();
        try {
            AuthorizationData ad = bank.sale(sd);
            assertFalse("Не вернулось сообщение об ошибке", ad.isStatus());
        } catch (BankException e) {
            AuthorizationData ad = e.getAuthorizationData();
            assertNotNull(ad);
            assertEquals("Вернулось некорректное сообщение об ошибке", "Все плохо", e.getAuthorizationData().getMessage());
            verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName, "10.00", "-r", Integer.toString(PAYMENT_DIRECTION)));
        }
    }

    @Test
    public void refundSuccessful() throws CashException, IOException, ParseException {
        doReturn(new ArrayList<>(successfullSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successfull)).when(bank).readResponseFile();
        RefundData rd = new RefundData();
        rd.setAmount(10000L);
        rd.setRefNumber("12345");
        rd.setTerminalId(TERMINAL_ID.substring(1));
        rd.setOriginalSaleTransactionAmount(rd.getAmount());
        rd.setOriginalSaleTransactionDate(new SimpleDateFormat("ddMMyyyyHHmmss").parse("24092013182415"));
        AuthorizationData ad = bank.refund(rd);
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName, "100.00",
                "-refund", "-orig_sum", rd.getOriginalSaleTransactionAmount().toString(),
                "-orig_num", "12345",
                "-orig_term", TERMINAL_ID,
                "-orig_time", "24092013182415"));
        verifySuccessfullOperation(ad, BankOperationType.REFUND);
    }

    @Test
    public void refundUnsuccessfull() throws CashException, ParseException {
        RefundData rd = new RefundData();
        rd.setAmount(10000L);
        rd.setRefNumber("12345");
        rd.setTerminalId(TERMINAL_ID);
        rd.setOriginalSaleTransactionAmount(rd.getAmount());
        rd.setOriginalSaleTransactionDate(new SimpleDateFormat("ddMMyyyyHHmmss").parse("24092013182415"));
        doAnswer(unsuccessfullAnswer).when(bank).readResponseFile();
        bank.start();
        try {
            AuthorizationData ad = bank.refund(rd);
            assertFalse("Не вернулось сообщение об ошибке", ad.isStatus());
        } catch (BankException e) {
            AuthorizationData ad = e.getAuthorizationData();
            assertNotNull(ad);
            assertEquals("Вернулось некорректное сообщение об ошибке", "Все плохо", e.getAuthorizationData().getMessage());
            verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName, "100.00",
                    "-refund", "-orig_sum", rd.getOriginalSaleTransactionAmount().toString(),
                    "-orig_num", "12345",
                    "-orig_term", TERMINAL_ID,
                    "-orig_time", "24092013182415"));
        }
    }

    @Test
    public void reversalSuccessful() throws CashException, IOException, ParseException {
        doReturn(new ArrayList<>(successfullSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successfull)).when(bank).readResponseFile();
        SaleData sd = getSaleDataForRefund();

        ReversalData rd = new ReversalData();
        rd.setAmount(sd.getAmount());
        rd.setOriginalSaleTransactionAmount(rd.getAmount());
        rd.setMerchantId(TRANSACTION_NUMBER);
        AuthorizationData ad = bank.reversal(rd);
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName,"123.45","-reversal", "-tranz_id", TRANSACTION_NUMBER));
        verifySuccessfullOperation(ad, BankOperationType.REVERSAL);
    }

    @Test
    public void reversalUnsuccessfull() throws CashException, IOException {

        SaleData sd = getSaleDataForRefund();
        ReversalData rd = new ReversalData();
        rd.setAmount(sd.getAmount());
        rd.setOriginalSaleTransactionAmount(rd.getAmount());
        rd.setMerchantId(TRANSACTION_NUMBER);
        doReturn(false).when(bank).shouldBeProcessedAsRefundIfReversalFailed(any(ReversalData.class), any(BankAuthorizationException.class));
        doAnswer(unsuccessfullAnswer).when(bank).readResponseFile();
        bank.start();
        try {
            AuthorizationData ad = bank.reversal(rd);
            assertFalse("Не вернулось сообщение об ошибке", ad.isStatus());
        } catch (BankException e) {
            verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName,"123.45","-reversal", "-tranz_id", TRANSACTION_NUMBER));
            AuthorizationData ad = e.getAuthorizationData();
            assertNotNull(ad);
            assertEquals("Вернулось некорректное сообщение об ошибке", "Все плохо", e.getAuthorizationData().getMessage());
        }
    }
    @Test
    public void reversalUnsuccessfullFallbackToRefund() throws CashException, IOException, ParseException {
        SaleData sd = getSaleDataForRefund();
        ReversalData rd = new ReversalData();
        rd.setAmount(sd.getAmount());
        rd.setOriginalSaleTransactionAmount(rd.getAmount());
        rd.setOriginalSaleTransactionDate(new SimpleDateFormat("ddMMyyyyHHmmss").parse("24092013182415"));
        rd.setMerchantId(TRANSACTION_NUMBER);
        rd.setRefNumber("12345");
        rd.setTerminalId(TERMINAL_ID.substring(1));

        doReturn(true).when(bank).shouldBeProcessedAsRefundIfReversalFailed(any(ReversalData.class), any(BankAuthorizationException.class));
        isSuccessfulAnswer = false;
        doAnswer(differentAnswer).when(bank).readResponseFile();
        bank.start();
        AuthorizationData ad = bank.reversal(rd);
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName,"123.45","-reversal", "-tranz_id", TRANSACTION_NUMBER));
        verify(bank).refundIfReversalFailed(rd);
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName, "123.45",
                "-refund", "-orig_sum", rd.getOriginalSaleTransactionAmount().toString(),
                "-orig_num", "12345",
                "-orig_term", TERMINAL_ID,
                "-orig_time", "24092013182415"));
        verifySuccessfullOperation(ad, BankOperationType.REFUND);
    }


    @Test
    public void reversalFallBackToRefundSuccessful() throws CashException, IOException, ParseException {
        doReturn(new ArrayList<>(successfullSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(successfull)).when(bank).readResponseFile();
        SaleData sd = getSaleDataForRefund();

        ReversalData rd = new ReversalData();
        rd.setAmount(sd.getAmount());
        rd.setOriginalSaleTransactionAmount(rd.getAmount() + 1);
        rd.setMerchantId(TRANSACTION_NUMBER);

        rd.setAmount(12345L);
        rd.setRefNumber("12345");
        rd.setTerminalId(TERMINAL_ID);
        rd.setOriginalSaleTransactionDate(new SimpleDateFormat("ddMMyyyyHHmmss").parse("24092013182415"));

        AuthorizationData ad = bank.reversal(rd);
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(configName, "123.45",
                "-refund", "-orig_sum", rd.getOriginalSaleTransactionAmount().toString(),
                "-orig_num", "12345",
                "-orig_term", TERMINAL_ID,
                "-orig_time", "24092013182415"));
        verifySuccessfullOperation(ad, BankOperationType.REFUND);
    }




    @Test
    public void dailyLogSuccessfull() throws CashException {
        List<String> expectedSlip = new ArrayList<>(successfullDailyLog);
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readResponseFile();
        try {
            DailyLogData dld = bank.dailyLog(anyLong());
            verify(bank).runExecutableAndGetResponseData(Arrays.asList("netcash.cfg", "report"));
            assertNotNull(dld);
            assertEquals(expectedSlip, dld.getSlip());
        } catch (BankException e) {
            fail("Для успешной операции выброшено исключение");
        }
    }
    @Test
    public void dailyLogUnsuccessfull() throws CashException {
        List<String> expectedSlip = new ArrayList<>(unsuccessfull);
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readSlipFile();
        doReturn(new ArrayList<>(expectedSlip)).when(bank).readResponseFile();
        try {
            bank.dailyLog(anyLong());
            fail("Не выброшено исключение при возникновении ошибки");
        } catch (BankException e) {
            assertEquals("Текст сообщения об ошибке не совпадает с результатом", "Все плохо", e.getMessage());
            verify(bank).runExecutableAndGetResponseData(Arrays.asList("netcash.cfg", "report"));
        } catch (Exception e) {
            fail("Выброшен некорректный тип исключения " + e.getClass().getName());
        }
    }

    private void verifySuccessfullOperation(AuthorizationData ad, BankOperationType operation) throws BankException, ParseException{
        assertNotNull(ad);
        assertEquals("getResponseCode","0", ad.getResponseCode());
        assertEquals("getCard().getCardNumber()","************1125", ad.getCard().getCardNumber());
        assertEquals("getTerminalId",TERMINAL_ID.substring(1), ad.getTerminalId());
        assertEquals("getAuthCode","9c2a", ad.getAuthCode());
        assertEquals("getMessage","Операция выполнена успешно", ad.getMessage());
        assertEquals("getOperationType", operation, ad.getOperationType());
        assertTrue("isStatus", ad.isStatus());

        assertEquals("getHostTransId", TRANSACTION_NUMBER, ad.getMerchantId());
        assertEquals("getRefNumber", "28", ad.getRefNumber());
        assertEquals("getDate", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("24/09/2013 18:24:15"), ad.getDate());

        List<List<String>> fullSlip = new ArrayList<>();
        fullSlip.add(new ArrayList<>(successfullSlip));
        fullSlip.add(new ArrayList<>(successfullSlip));
        assertEquals(fullSlip, ad.getSlips());
    }

    @Test
    public void verifyTerminalConvertor(){
        assertEquals("J123456", BankGoldCrownServiceImpl.normalizeTerminalID(Long.valueOf(123456).toString()));
        assertEquals("J000001", BankGoldCrownServiceImpl.normalizeTerminalID(Long.valueOf(1).toString()));
        assertEquals("J100000", BankGoldCrownServiceImpl.normalizeTerminalID(Long.valueOf(100000).toString()));
        assertEquals("J001234", BankGoldCrownServiceImpl.normalizeTerminalID(Long.valueOf(1234).toString()));
    }


}
