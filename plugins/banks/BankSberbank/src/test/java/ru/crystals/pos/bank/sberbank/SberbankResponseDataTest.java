package ru.crystals.pos.bank.sberbank;


import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.bank.datastruct.BankTypeEnum;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SberbankResponseDataTest {

    SberbankResponseData sberbankResponseData = new SberbankResponseData();

    private static final ThreadLocal<DateFormat> CARD_EXP_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("MM/yy"));
    private static final ThreadLocal<DateFormat> THREAD_LOCAL_DATEFORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddhhmmss"));

    private static final String RESPONSE_CODE = "0";
    private static final String MESSAGE = "ОДОБРЕHО";
    private static final String CARD_NUMBER = "546999******4617";
    private static final String CARD_EXP_DATE = "08/18";
    private static final String AUTH_CODE = "22H728";
    private static final String INTERNAL_OPERATION_NUMBER = "0030";
    private static final String CARD_TYPE = "Mastercard";
    private static final String IS_SBERBANK_CARD = "1";
    private static final String TERMINAL_ID = "00689949";
    private static final String TRANSACTION_DATE = "20180425095917";
    private static final String REFERENCE_NUMBER = "152463956160";
    private static final String CARD_HASH = "7E6AF16464E06845EE2EADF902E4EAE63D68F35A";
    private static final String LOYALTY_CODE = "3";
    private static final String REQUEST_ID = "FB98B5D4";

    @Test
    public void testParseResponseFile() throws ParseException {
        sberbankResponseData.parseResponseFile(generateResponseFile());
        Assert.assertEquals(RESPONSE_CODE, sberbankResponseData.getResponseCode());
        Assert.assertEquals(MESSAGE, sberbankResponseData.getMessage());
        Assert.assertEquals(CARD_NUMBER, sberbankResponseData.getCardNumber());
        Assert.assertEquals(CARD_EXP_DATE_FORMAT.get().parse(CARD_EXP_DATE), sberbankResponseData.getBankCard().getExpiryDate());
        Assert.assertEquals(AUTH_CODE, sberbankResponseData.getAuthCode());
        Assert.assertEquals(CARD_TYPE, sberbankResponseData.getBankCard().getCardType());
        Assert.assertEquals(BankTypeEnum.SBERBANK, sberbankResponseData.getBankCard().getCardOperator());
        Assert.assertEquals(TERMINAL_ID, sberbankResponseData.getTransactionDate());
        Assert.assertEquals(THREAD_LOCAL_DATEFORMAT.get().parse(TRANSACTION_DATE), sberbankResponseData.getDate());
        Assert.assertEquals(REFERENCE_NUMBER, sberbankResponseData.getReferenceNumber());
        Assert.assertEquals(CARD_HASH, sberbankResponseData.getBankCard().getCardNumberHash());
        Assert.assertEquals(LOYALTY_CODE, sberbankResponseData.getLoyaltyProgramCode());
        Assert.assertEquals(REQUEST_ID, sberbankResponseData.getRequestId());
    }

    @Test
    public void messageTest() {
        verifyMessage("0,ОДОБРЕHО", "0", "ОДОБРЕHО");
        verifyMessage("1234,Отказано", "1234", "Отказано (1234)");
        verifyMessage("5678,", "5678", ResBundleBankSberbank.getString("ERROR_COMMUNICATION") + " (5678)");
        verifyMessage("5678", "5678", ResBundleBankSberbank.getString("ERROR_COMMUNICATION") + " (5678)");
    }

    private void verifyMessage(String input, String expectedCode, String expectedMessage) {
        sberbankResponseData.parseResponseFile(generateResponseFile(input));
        Assert.assertEquals(expectedCode, sberbankResponseData.getResponseCode());
        Assert.assertEquals(expectedMessage, sberbankResponseData.getMessage());
    }

    private List<String> generateResponseFile() {
        return generateResponseFile(RESPONSE_CODE + "," + MESSAGE);
    }
    private List<String> generateResponseFile(String codeAndMessage) {
        List<String> responseFile = new ArrayList<>();
        responseFile.add(codeAndMessage);
        responseFile.add(CARD_NUMBER);
        responseFile.add(CARD_EXP_DATE);
        responseFile.add(AUTH_CODE);
        responseFile.add(INTERNAL_OPERATION_NUMBER);
        responseFile.add(CARD_TYPE);
        responseFile.add(IS_SBERBANK_CARD);
        responseFile.add(TERMINAL_ID);
        responseFile.add(TRANSACTION_DATE);
        responseFile.add(REFERENCE_NUMBER);
        responseFile.add(CARD_HASH);
        responseFile.add("");
        responseFile.add("0");
        responseFile.add("684444445555");
        responseFile.add("00");
        responseFile.add("00");
        responseFile.add("");
        responseFile.add(LOYALTY_CODE);
        responseFile.add("");
        responseFile.add(REQUEST_ID);
        responseFile.add("00040005");
        responseFile.add("0000000000000000000000000000000000000000000000000000000000000000902E4EAE63D68F35A");
        return responseFile;
    }
}