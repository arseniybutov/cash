package ru.crystals.pos.bank.gascardservice;

import org.hamcrest.core.AnyOf;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GasCardServiceResponseDataTest {
    private static final String OK_RESPONSE_CODE = "000";
    private static final String OK_MESSAGE = "Без ошибок!";
    private static final String EXPECTED_TERMINAL_ID = "00600123";
    private static final String EXPECTED_MERCHANT_ID = "700000";
    private static final String EXPECTED_ECR_NUMBER = "098";
    private static final String EXPECTED_ECR_RECEIPT_NUMBER = "0004";
    private static final String TEST_E_FILE = OK_RESPONSE_CODE + " \"" + OK_MESSAGE + "\"";
    private static final String EXPECTED_PRIMARY_ACCOUNT_NUMBER = "67645420****0012";
    public static final String CARD_EXPIRY_DATE_FORMAT = "MMyy";
    public static final String RAW_CARD_EXPIRY_DATE = "1212";
    private static final Date EXPECTED_EXPIRY_DATE = parseDate(RAW_CARD_EXPIRY_DATE, CARD_EXPIRY_DATE_FORMAT);
    private static final String EXPECTED_TRANSACTION_AMOUNT = "10000";
    public static final String EXPECTED_TRANSACTION_FEE = "0";
    public static final String EXPECTED_TOTAL_AMOUNT = "10000";
    private static final String TRANSACTION_DATE_PATTERN = "ddMMHHmm";
    public static final String RAW_TRANSACTION_DATE = "1110";
    public static final String RAW_TRANSACTION_TIME = "1011";
    private static final Date TRANSACTION_DATE = parseDate(RAW_TRANSACTION_DATE + RAW_TRANSACTION_TIME, TRANSACTION_DATE_PATTERN);
    public static final Long EXPECTED_INVOICE_NUMBER = 60L;
    public static final String EXPECTED_ISSUER_NAME = "CIRRUS/MAESTRO";
    public static final String EXPECTED_CURRENCY = "643";
    public static final String EXPECTED_RESPONSE_CODE = "0";
    public static final String EXPECTED_VISUAL_RESPONSE_CODE = "0";
    public static final String EXPECTED_AUTHORIZATION_ID = "484804";
    public static final String EXPECTED_RRN = "1002484804";
    public static final String EXPECTED_APPLICATION_ID = "a0000000043060";
    private static final String EXPECTED_TRANSACTION_CERTIFICATE = "140110148";
    public static final String EXPECTED_APP_LAB = "MAESTRO";
    private static final String GCS_DATE_PATTERN = "ddMMyyHHmmss";
    public static final String RAW_GCS_DATE = "111012";
    public static final String RAW_GCS_TIME = "101024";
    private static final Date EXPECTED_GCS_DATE = parseDate(RAW_GCS_DATE + RAW_GCS_TIME, GCS_DATE_PATTERN);
    private static final String EXPECTED_VIS_RESPONSE_CODE_GCS = "OK";
    public static final String EXPECTED_AUTH_RESULT = "OK";
    public static final String EXPECTED_FLAGS_RES = "\\x80\\x80\\x81";
    private static final String EXPECTED_VISUAL_HOST_RESULT = "";
    private static final String EXPECTED_APPROVE_RESULT = "";
    private static final String EXPECTED_STAN = "";
    private static final String EXPECTED_REQUEST_ID = "1";
    private static final String LOCAL_TRANSACTION_DATE = "";
    private static final String EXPECTED_CRYPTOGRAM = "123,ек";
    private static final String TEST_RESULT_DATA_FILE = EXPECTED_TERMINAL_ID + "," +
            EXPECTED_MERCHANT_ID + "," +
            EXPECTED_ECR_NUMBER + "," +
            EXPECTED_ECR_RECEIPT_NUMBER + "," +
            EXPECTED_PRIMARY_ACCOUNT_NUMBER + "," +
            RAW_CARD_EXPIRY_DATE + "," +
            EXPECTED_TRANSACTION_AMOUNT + "," +
            EXPECTED_TRANSACTION_FEE + "," +
            EXPECTED_TOTAL_AMOUNT + "," +
            RAW_TRANSACTION_DATE + "," +
            RAW_TRANSACTION_TIME + "," +
            EXPECTED_INVOICE_NUMBER + "," +
            EXPECTED_ISSUER_NAME + "," +
            EXPECTED_CURRENCY + "," +
            EXPECTED_RESPONSE_CODE + "," +
            EXPECTED_VISUAL_RESPONSE_CODE + "," +
            EXPECTED_AUTHORIZATION_ID + "," +
            EXPECTED_RRN + "," +
            EXPECTED_APPLICATION_ID + "," +
            EXPECTED_TRANSACTION_CERTIFICATE + "," +
            EXPECTED_APP_LAB + "," +
            RAW_GCS_DATE + "," +
            RAW_GCS_TIME + "," +
            OK_RESPONSE_CODE + "," +
            EXPECTED_VIS_RESPONSE_CODE_GCS + "," +
            EXPECTED_AUTH_RESULT + "," +
            EXPECTED_VISUAL_HOST_RESULT + "," +
            EXPECTED_APPROVE_RESULT + "," +
            EXPECTED_FLAGS_RES + "," +
            EXPECTED_STAN + "," +
            LOCAL_TRANSACTION_DATE + "," +
            EXPECTED_REQUEST_ID + "," +
            EXPECTED_CRYPTOGRAM;

    private static Date parseDate(String rawDate, String pattern) {
        Date date = null;
        try {
            date = new SimpleDateFormat(pattern).parse(rawDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private GasCardServiceResponseData responseData = new GasCardServiceResponseData();

    @Test
    public void testParseResponseFile() {
        // given
        List<String> responseFile = new ArrayList<>();
        responseFile.add(TEST_E_FILE);
        responseFile.add(TEST_RESULT_DATA_FILE);

        // when
        GasCardServiceResponseData resultResponseData = responseData.parseResponseFile(responseFile);

        // then
        Assert.assertEquals(OK_RESPONSE_CODE, resultResponseData.getResCodeGCS());
        Assert.assertEquals(OK_MESSAGE + " (" + OK_RESPONSE_CODE + ")", resultResponseData.getMessage());
        Assert.assertEquals(EXPECTED_TERMINAL_ID, resultResponseData.getTerminalId());
        Assert.assertEquals(EXPECTED_MERCHANT_ID, resultResponseData.getMerchantId());
        Assert.assertEquals(EXPECTED_ECR_NUMBER, resultResponseData.getECRNumber());
        Assert.assertEquals(EXPECTED_ECR_RECEIPT_NUMBER, resultResponseData.getECRReceiptNumber());
        Assert.assertEquals(EXPECTED_PRIMARY_ACCOUNT_NUMBER, resultResponseData.getBankCard().getCardNumber());
        Assert.assertEquals(EXPECTED_EXPIRY_DATE, resultResponseData.getBankCard().getExpiryDate());
        Assert.assertEquals(EXPECTED_TRANSACTION_AMOUNT, resultResponseData.getTransactionAmount());
        Assert.assertEquals(EXPECTED_TRANSACTION_FEE, resultResponseData.getTransactionFee());
        Assert.assertEquals(EXPECTED_TOTAL_AMOUNT, resultResponseData.getTotalAmount());
        Assert.assertEquals(TRANSACTION_DATE, resultResponseData.getTransactionDate());
        Assert.assertEquals(EXPECTED_INVOICE_NUMBER, resultResponseData.getInvoiceNumber());
        Assert.assertEquals(EXPECTED_ISSUER_NAME, resultResponseData.getIssuerName());
        Assert.assertEquals(EXPECTED_CURRENCY, resultResponseData.getCurrency());
        Assert.assertEquals(OK_RESPONSE_CODE, resultResponseData.getResponseCode());
        Assert.assertEquals(EXPECTED_VISUAL_RESPONSE_CODE, resultResponseData.getVisualResponseCode());
        Assert.assertEquals(EXPECTED_AUTHORIZATION_ID, resultResponseData.getAuthCode());
        Assert.assertEquals(EXPECTED_RRN, resultResponseData.getRRN());
        Assert.assertEquals(EXPECTED_APPLICATION_ID, resultResponseData.getApplicationId());
        Assert.assertEquals(EXPECTED_TRANSACTION_CERTIFICATE, resultResponseData.getTransactionCertificate());
        Assert.assertEquals(EXPECTED_APP_LAB, resultResponseData.getAppLab());
        Assert.assertEquals(EXPECTED_GCS_DATE, resultResponseData.getGCSTransactionDate());
        Assert.assertEquals(OK_RESPONSE_CODE, resultResponseData.getResCodeGCS());
        Assert.assertEquals(EXPECTED_VIS_RESPONSE_CODE_GCS, resultResponseData.getVResCodeGCS());
        Assert.assertEquals(EXPECTED_AUTH_RESULT, resultResponseData.getAuthResult());
        Assert.assertNull(resultResponseData.getVisHostRes());
        Assert.assertNull(resultResponseData.getApproveRes());
        Assert.assertEquals(EXPECTED_FLAGS_RES, resultResponseData.getFlagsRes());
        Assert.assertNull(resultResponseData.getSystemTraceAuditNumber());
        Assert.assertNull(resultResponseData.getLocalTransactionDate());
        Assert.assertEquals(EXPECTED_REQUEST_ID, resultResponseData.getReqID());
        Assert.assertEquals(EXPECTED_CRYPTOGRAM, resultResponseData.getCryptogram());
        Assert.assertTrue(resultResponseData.isSuccessful());
    }

    /**
     * Была ошибка, что результат операции не обнулялся, и принимали оплату, при отказе терминала.
     */
    @Test
    public void testResetResultAfterSuccessSale() {
        testParseResponseFile();

        List<String> responseFile = new ArrayList<>();
        GasCardServiceResponseData resultResponseData = responseData.parseResponseFile(responseFile);

        Assert.assertNull(resultResponseData.getResCodeGCS());
        Assert.assertThat(resultResponseData.getMessage(),
                AnyOf.anyOf(IsEqual.equalTo("Терминал не доступен. Проверьте подключение."),
                        IsEqual.equalTo("The terminal is not available. Check the connection.")));
        Assert.assertNull(resultResponseData.getTerminalId());
        Assert.assertNull(resultResponseData.getMerchantId());
        Assert.assertNull(resultResponseData.getECRNumber());
        Assert.assertNull(resultResponseData.getECRReceiptNumber());
        Assert.assertNull(resultResponseData.getBankCard().getCardNumber());
        Assert.assertNull(resultResponseData.getBankCard().getExpiryDate());
        Assert.assertNull(resultResponseData.getTransactionAmount());
        Assert.assertNull(resultResponseData.getTransactionFee());
        Assert.assertNull(resultResponseData.getTotalAmount());
        Assert.assertNull(resultResponseData.getTransactionDate());
        Assert.assertNull(resultResponseData.getInvoiceNumber());
        Assert.assertNull(resultResponseData.getIssuerName());
        Assert.assertNull(resultResponseData.getCurrency());
        Assert.assertNull(resultResponseData.getResponseCode());
        Assert.assertNull(resultResponseData.getVisualResponseCode());
        Assert.assertNull(resultResponseData.getAuthCode());
        Assert.assertNull(resultResponseData.getRRN());
        Assert.assertNull(resultResponseData.getApplicationId());
        Assert.assertNull(resultResponseData.getTransactionCertificate());
        Assert.assertNull(resultResponseData.getAppLab());
        Assert.assertNull(resultResponseData.getGCSTransactionDate());
        Assert.assertNull(resultResponseData.getResCodeGCS());
        Assert.assertNull(resultResponseData.getVResCodeGCS());
        Assert.assertNull(resultResponseData.getAuthResult());
        Assert.assertNull(resultResponseData.getVisHostRes());
        Assert.assertNull(resultResponseData.getApproveRes());
        Assert.assertNull(resultResponseData.getFlagsRes());
        Assert.assertNull(resultResponseData.getSystemTraceAuditNumber());
        Assert.assertNull(resultResponseData.getLocalTransactionDate());
        Assert.assertNull(resultResponseData.getReqID());
        Assert.assertNull(resultResponseData.getCryptogram());
        Assert.assertFalse(resultResponseData.isSuccessful());
    }
}
