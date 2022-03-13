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

public class ArcomResponseExtendedData implements ResponseData {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    protected static final String SUCCESSFULL_RESPONSE_CODE = "000";
    protected static final String PIN_PAD_CONNECTION_LOST_RESPONSE_CODE = "999";
    protected static final SimpleDateFormat TRACK2_DATE_FORMAT = new SimpleDateFormat("yyMM");
    protected static final String TRACK2_SEPARATOR = "=";

    private List<String> responseFile;

    @Override
    public ArcomResponseExtendedData parseResponseFile(List<String> responseFile) {
        this.responseFile = responseFile;
        return this;
    }

    public String getExtendedValue(ArcomResponseExtendedDataField field) {
        int fieldOrder = field.getRowNumber();
        if (responseFile != null && responseFile.size() > fieldOrder) {
            return StringUtils.trimToNull(responseFile.get(fieldOrder));
        }
        return null;
    }

    /**
     * Список полей из файла с дополнительными дынными о операции
     */
    public enum ArcomResponseExtendedDataField {
        RESPONSE_CODE(0),
        CARD_NUMBER(1),
        TERMINAL_ID(6),
        CARD_TYPE(9),
        MESSAGE(18),
        LOYALTY_CODE(21);

        private final int rowNumber;

        ArcomResponseExtendedDataField(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public int getRowNumber() {
            return rowNumber;
        }
    }




    @Override
    public String getResponseCode() {
        return getExtendedValue(ArcomResponseExtendedDataField.RESPONSE_CODE);
    }

    @Override
    public String getMessage() {
        return getExtendedValue(ArcomResponseExtendedDataField.MESSAGE);
    }

    @Override
    public String getAuthCode() {
        throw new IllegalArgumentException("getAuthCode unsupported for ExtendedData");
    }

    @Override
    public String getTerminalId() {
        return getExtendedValue(ArcomResponseExtendedDataField.TERMINAL_ID);
    }

    @Override
    public String getReferenceNumber() {
        throw new IllegalArgumentException("getReferenceNumber unsupported for ExtendedData");
    }

    @Override
    public String getLoyaltyProgramCode() {
        return getExtendedValue(ArcomResponseExtendedDataField.LOYALTY_CODE);
    }

    @Override
    public boolean isSuccessful() {
        return SUCCESSFULL_RESPONSE_CODE.equals(getExtendedValue(ArcomResponseExtendedDataField.RESPONSE_CODE));
    }

    @Override
    public BankCard getBankCard() {
        BankCard bc = new BankCard();
        String fullCardNumber = getExtendedValue(ArcomResponseExtendedDataField.CARD_NUMBER);
        if (fullCardNumber == null) {
            return bc;
        }
        String[] track2data = fullCardNumber.split(TRACK2_SEPARATOR);
        bc.setCardNumber(BankUtils.maskCardNumber(track2data[0]));
        bc.setCardType(getExtendedValue(ArcomResponseExtendedDataField.CARD_TYPE));
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
            int indexOfLineWithCardNumber = ArcomResponseExtendedDataField.CARD_NUMBER.ordinal();
            List<String> loggableResponse = new ArrayList<String>(responseFile);
            if (indexOfLineWithCardNumber < loggableResponse.size()) {
                loggableResponse.set(indexOfLineWithCardNumber, BankUtils.maskCardNumberForLog(loggableResponse.get(indexOfLineWithCardNumber)));
            }
            log.info("Response file:\n{}", StringUtils.join(loggableResponse, '\n'));
        }
        return this;
    }

}
