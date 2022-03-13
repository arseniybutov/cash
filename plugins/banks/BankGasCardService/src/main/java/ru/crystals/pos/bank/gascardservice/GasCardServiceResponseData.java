package ru.crystals.pos.bank.gascardservice;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GasCardServiceResponseData implements ResponseData {
    private static final Logger log = LoggerFactory.getLogger(GasCardServiceResponseData.class);

    private static final String PARAMS_DELIMITER = ",";
    private static final String CARD_EXPIRY_DATE_PATTERN = "MMyy";
    private static final String GCS_DATE_PATTERN = "ddMMyyHHmmss";
    private static final String LOCAL_DATE_PATTERN = "yyMMddHHmmss";

    /**
     * Регулярка, что бы распарсить файл ответа от банка.
     * Надо вытащить число(первая скобка) - код ответа по операции, и текст(вторая скобка) - расшифровка ответа.
     * Терминал может вернуть текст без двойных кавычек, и сплитить по символу просто не получится.
     * А так же из-за таймаута или ошибок доступа нужного файла может не быть вообще, и первым может прийти второй файл.
     */
    private static final String REGEX_FOR_FIRST_FILE_SEPARATE = "(^\\d+)\\s*\"?(.+[^\"])\"?$";

    private Collection<String> okResponses = new ArrayList<>();
    private EnumMap<GasCardServiceDataField, String> parsed = new EnumMap<>(GasCardServiceDataField.class);
    private List<String> responseFiles;
    private String firstFileResponseCode;
    private String message;
    private GasCardServiceDataField[] dataFields = GasCardServiceDataField.values();

    private enum GasCardServiceDataField {
        TERMINAL_ID,
        MERCHANT_ID,
        ECR_NUMBER,
        ECR_RECEIPT_NUMBER,
        BANK_CARD_NUMBER,
        CARD_EXPIRY_DATE,
        TRANSACTION_AMOUNT,
        TRANSACTION_FEE,
        TOTAL_AMOUNT,
        TRANSACTION_DATE,
        TRANSACTION_TIME,
        INVOICE_NUMBER,
        ISSUER_NAME,
        CURRENCY,
        RESPONSE_CODE,
        VISUAL_RESPONSE_CODE,
        AUTHORIZATION_ID,
        RETRIEVAL_REFERENCE_NUMBER,
        APPLICATION_ID,
        TRANSACTION_CERTIFICATE,
        APP_LAB,
        TRANSACTION_DATE_WITH_YEAR,
        TRANSACTION_DATE_WITH_YEAR_TIME,
        RES_CODE_GCS,
        VRES_CODE_GCS,
        AUTH_RESULT,
        VIS_HOST_RES,
        APPROVE_RES,
        FLAGS_RES,
        SYSTEM_TRACE_AUDIT_NUMBER,
        LOCAL_TRANSACTION_DATE,
        REQUEST_ID,
        CRYPTOGRAM
    }

    public GasCardServiceResponseData() {
        okResponses.add("0");
        okResponses.add("000");
        okResponses.add("003");
        okResponses.add("020");
        okResponses.add("959");
    }

    @Override
    public GasCardServiceResponseData parseResponseFile(List<String> responseFiles) {
        this.responseFiles = responseFiles;
        clearResponseData();

        if (!responseFiles.isEmpty()) {
            String firstFile = responseFiles.get(0);
            Matcher m = Pattern.compile(REGEX_FOR_FIRST_FILE_SEPARATE).matcher(firstFile);
            if (m.matches()) {
                firstFileResponseCode = m.group(1);
                message = m.group(2);
            }
        }
        if (responseFiles.size() > 1) {
            parseResponseParamsFile(1);
        }
        return this;
    }

    private void clearResponseData() {
        parsed.clear();
        firstFileResponseCode = null;
        message = null;
    }

    private void parseResponseParamsFile(int fileIndex) {
        if (fileIndex < responseFiles.size()) {
            String[] splittedLine = StringUtils.trimToEmpty(responseFiles.get(fileIndex)).split(PARAMS_DELIMITER);
            if (splittedLine.length >= dataFields.length - 1) {
                for (int fieldIndex = GasCardServiceDataField.TERMINAL_ID.ordinal(); fieldIndex < dataFields.length - 1; fieldIndex++) {
                    parsed.put(dataFields[fieldIndex],
                            StringUtils.trimToNull(splittedLine[fieldIndex - GasCardServiceDataField.TERMINAL_ID.ordinal()]));
                }
            }
            if (containsCryptogram(splittedLine)) {
                parsed.put(dataFields[dataFields.length - 1], parseCryptogram(splittedLine));
            }
        }
    }

    private String parseCryptogram(String... splittedLine) {
        StringBuilder cryptogram = new StringBuilder();
        for (int i = dataFields.length - 1; i < splittedLine.length - 1; i++) {
            cryptogram.append(splittedLine[i]).append(",");
        }
        cryptogram.append(splittedLine[splittedLine.length - 1]);
        return cryptogram.toString();
    }

    private boolean containsCryptogram(String... splittedLine) {
        return splittedLine.length > dataFields.length - 1;
    }

    private Date parseDate(String rawDate, String pattern) {
        Date date = null;
        if (rawDate != null && !rawDate.isEmpty()) {
            try {
                date = new SimpleDateFormat(pattern).parse(rawDate);
            } catch (ParseException e) {
                log.debug("Unable to parse date", e);
            }
        }
        return date;
    }

    @Override
    public String getMessage() {
        return message == null ? ResBundleBankGasCardService.getString("TERMINAL_IS_NOT_AVAILABLE") : message + " (" + getResponseCode() + ")";
    }

    @Override
    public String getResponseCode() {
        return firstFileResponseCode;
    }

    @Override
    public String getAuthCode() {
        return parsed.get(GasCardServiceDataField.AUTHORIZATION_ID);
    }

    @Override
    public String getTerminalId() {
        return parsed.get(GasCardServiceDataField.TERMINAL_ID);
    }

    @Override
    public String getReferenceNumber() {
        return parsed.get(GasCardServiceDataField.RETRIEVAL_REFERENCE_NUMBER);
    }

    @Override
    public BankCard getBankCard() {
        BankCard bankCard = new BankCard();
        bankCard.setCardNumber(parsed.get(GasCardServiceDataField.BANK_CARD_NUMBER));
        bankCard.setExpiryDate(parseDate(parsed.get(GasCardServiceDataField.CARD_EXPIRY_DATE), CARD_EXPIRY_DATE_PATTERN));
        return bankCard;
    }

    @Override
    public boolean isSuccessful() {
        return firstFileResponseCode != null && okResponses.contains(firstFileResponseCode);
    }

    @Override
    public ResponseData logResponseFile() {
        for (String responseFile : responseFiles) {
            log.info(responseFile);
        }
        return this;
    }

    public String getResCodeGCS() {
        return parsed.get(GasCardServiceDataField.RES_CODE_GCS);
    }

    public String getMerchantId() {
        return parsed.get(GasCardServiceDataField.MERCHANT_ID);
    }

    public String getECRNumber() {
        return parsed.get(GasCardServiceDataField.ECR_NUMBER);
    }

    public String getECRReceiptNumber() {
        return parsed.get(GasCardServiceDataField.ECR_RECEIPT_NUMBER);
    }

    public String getTransactionAmount() {
        return parsed.get(GasCardServiceDataField.TRANSACTION_AMOUNT);
    }

    public String getTransactionFee() {
        return parsed.get(GasCardServiceDataField.TRANSACTION_FEE);
    }

    public String getTotalAmount() {
        return parsed.get(GasCardServiceDataField.TOTAL_AMOUNT);
    }

    public Date getTransactionDate() {
        return parseDate(parsed.get(GasCardServiceDataField.TRANSACTION_DATE) + parsed.get(GasCardServiceDataField.TRANSACTION_TIME), "ddMMHHmm");
    }

    public Long getInvoiceNumber() {
        String invoiceNumber = parsed.get(GasCardServiceDataField.INVOICE_NUMBER);
        return StringUtils.trimToNull(invoiceNumber) != null ? Long.parseLong(invoiceNumber) : null;
    }

    public String getIssuerName() {
        return parsed.get(GasCardServiceDataField.ISSUER_NAME);
    }

    public String getCurrency() {
        return parsed.get(GasCardServiceDataField.CURRENCY);
    }

    public String getVisualResponseCode() {
        return parsed.get(GasCardServiceDataField.VISUAL_RESPONSE_CODE);
    }

    public String getRRN() {
        return parsed.get(GasCardServiceDataField.RETRIEVAL_REFERENCE_NUMBER);
    }

    public String getTransactionCertificate() {
        return parsed.get(GasCardServiceDataField.TRANSACTION_CERTIFICATE);
    }

    public String getApplicationId() {
        return parsed.get(GasCardServiceDataField.APPLICATION_ID);
    }

    public String getAppLab() {
        return parsed.get(GasCardServiceDataField.APP_LAB);
    }

    public Date getGCSTransactionDate() {
        return parseDate(
                parsed.get(GasCardServiceDataField.TRANSACTION_DATE_WITH_YEAR) + parsed.get(GasCardServiceDataField.TRANSACTION_DATE_WITH_YEAR_TIME),
                GCS_DATE_PATTERN);
    }

    public String getVResCodeGCS() {
        return parsed.get(GasCardServiceDataField.VRES_CODE_GCS);
    }

    public String getAuthResult() {
        return parsed.get(GasCardServiceDataField.AUTH_RESULT);
    }

    public String getVisHostRes() {
        return parsed.get(GasCardServiceDataField.VIS_HOST_RES);
    }

    public String getApproveRes() {
        return parsed.get(GasCardServiceDataField.APPROVE_RES);
    }

    public String getFlagsRes() {
        return parsed.get(GasCardServiceDataField.FLAGS_RES);
    }

    public String getSystemTraceAuditNumber() {
        return parsed.get(GasCardServiceDataField.SYSTEM_TRACE_AUDIT_NUMBER);
    }

    public Date getLocalTransactionDate() {
        return parseDate(parsed.get(GasCardServiceDataField.LOCAL_TRANSACTION_DATE), LOCAL_DATE_PATTERN);
    }

    public String getCryptogram() {
        return parsed.get(GasCardServiceDataField.CRYPTOGRAM);
    }

    public String getReqID() {
        return parsed.get(GasCardServiceDataField.REQUEST_ID);
    }
}
