package ru.crystals.pos.bank.arcom;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ArcomResponseData implements ResponseData {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    protected static final String SUCCESSFULL_RESPONSE_CODE = "000";
    protected static final String SUCCESSFULL_RESPONSE_CODE_PREFIX = "00";
    protected static final String FATAL_UNSUCCESSFULL_RESPONSE_CODE_PREFIX = "99";
    protected static final String SUSPENSION_RESPONSE_CODE = "550";
    protected static final SimpleDateFormat TRACK2_DATE_FORMAT = new SimpleDateFormat("yyMM");
    protected static final String TRACK2_SEPARATOR = "=";

    private List<String> responseFile;

    @Override
    public ArcomResponseData parseResponseFile(List<String> responseFile) {
        this.responseFile = responseFile;
        return this;
    }

    public String getValue(ArcomResponseDataField field) {
        int fieldOrder = field.ordinal();
        if (responseFile != null && responseFile.size() > fieldOrder) {
            return StringUtils.trimToNull(responseFile.get(fieldOrder));
        }
        return null;
    }

    /**
     * Упорядоченный список полей файла ответа
     */
    public enum ArcomResponseDataField {
        RESPONSE_CODE,
        CARD_NUMBER,
        TERMINAL_ID,
        AUTH_CODE,
        CARD_TYPE,
        MESSAGE,
        SUM,
        RRN,
        LOYALTY_CODE
    }



    @Override
    public String getResponseCode() {
        return getValue(ArcomResponseDataField.RESPONSE_CODE);
    }

    @Override
    public String getMessage() {
        return getValue(ArcomResponseDataField.MESSAGE);
    }

    @Override
    public String getAuthCode() {
        return getValue(ArcomResponseDataField.AUTH_CODE);
    }

    @Override
    public String getTerminalId() {
        return getValue(ArcomResponseDataField.TERMINAL_ID);
    }

    @Override
    public String getReferenceNumber() {
        return getValue(ArcomResponseDataField.RRN);
    }

    @Override
    public String getLoyaltyProgramCode() {
        return getValue(ArcomResponseDataField.LOYALTY_CODE);
    }

    @Override
    public boolean isSuccessful() {
        return SUCCESSFULL_RESPONSE_CODE.equals(getValue(ArcomResponseDataField.RESPONSE_CODE));
    }

    @Override
    public BankCard getBankCard() {
        BankCard bc = new BankCard();
        String fullCardNumber = getValue(ArcomResponseDataField.CARD_NUMBER);
        if (fullCardNumber == null) {
            return bc;
        }
        String[] track2data = fullCardNumber.split(TRACK2_SEPARATOR);
        bc.setCardNumber(BankUtils.maskCardNumber(track2data[0]));
        bc.setCardType(getValue(ArcomResponseDataField.CARD_TYPE));
        if (track2data.length > 1) {
            bc.setTrack2(fullCardNumber);
            String yearAndMonth = track2data[1].substring(0, 4);
            try {
                bc.setExpiryDate(TRACK2_DATE_FORMAT.parse(yearAndMonth));
            } catch (Exception e) {
                log.warn("Unable exctract expiry date from Track2 (=" + yearAndMonth + ")", e);
            }
        }
        return bc;
    }

    @Override
    public ResponseData logResponseFile() {
        if (log.isInfoEnabled()) {
            int indexOfLineWithCardNumber = ArcomResponseDataField.CARD_NUMBER.ordinal();
            List<String> loggableResponse = new ArrayList<String>(responseFile);
            if (indexOfLineWithCardNumber < loggableResponse.size()) {
                loggableResponse.set(indexOfLineWithCardNumber, BankUtils.maskCardNumberForLog(loggableResponse.get(indexOfLineWithCardNumber)));
            }
            log.info("Response file:\n{}", StringUtils.join(loggableResponse, '\n'));
        }
        return this;
    }
}
