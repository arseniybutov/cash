package ru.crystals.pos.bank.goldcrownbank;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldCrownResponseData implements ResponseData {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    public static final String FIELD_DELIMITER = ",";
    public static final String SUCCESSFULL_RESPONSE_CODE = "0";
    public static final String RESPONSE_TIME_OF_OPERATION_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    private List<String> responseFile;
    private Map<GoldCrownDataField, String> parsed;

    private void parseLine(int lineIndex, GoldCrownDataField... field) {
        if (lineIndex < responseFile.size()) {
            String[] splittedLine = StringUtils.trimToEmpty(responseFile.get(lineIndex)).split(FIELD_DELIMITER);
            int maxLength = Math.max(splittedLine.length, field.length);
            for (int fieldIndex = 0; fieldIndex < maxLength; fieldIndex++) {
                parsed.put(field[fieldIndex], StringUtils.trimToNull(splittedLine[fieldIndex]));
            }
        }
    }

    @Override
    public GoldCrownResponseData parseResponseFile(List<String> responseFile) {
        this.responseFile = responseFile;
        parsed = new HashMap<GoldCrownDataField, String>();

        parseLine(0, GoldCrownDataField.OPER_STATUS, GoldCrownDataField.OPER_TEXT);
        parseLine(1, GoldCrownDataField.TERM_NUM,
                GoldCrownDataField.OPER_TYPE,
                GoldCrownDataField.SELLER_ISO,
                GoldCrownDataField.BUYER_ISO,
                GoldCrownDataField.SUM,
                GoldCrownDataField.CURRENCY,
                GoldCrownDataField.TIME,
                GoldCrownDataField.PERCENT,
                GoldCrownDataField.AUTH_TYPE,
                GoldCrownDataField.AUTH_CODE);
        parseLine(2, GoldCrownDataField.TRANZ_ID);

        return this;
    }

    /**
     * Упорядоченный список полей файла ответа
     */
    public enum GoldCrownDataField {
        // поля внутри строк разделяются запятыми
        // Первая строка формируется всегда в формате
        /**
         * статус выполнения операции
         */
        OPER_STATUS,
        /**
         * текстовое описание статуса выполнения операции
         */
        OPER_TEXT,

        // Вторая строка формируется в случае, если: запрошена операция безналичного расчета и она выполнена успешно
        /**
         * порядковый номер операции в терминале (orig-term)
         */
        TERM_NUM,
        /**
         * внутренний код операции (всегда 40)
         */
        OPER_TYPE,
        /**
         * номер служебной карты терминала
         */
        SELLER_ISO,
        /**
         * номер карты покупателя
         */
        BUYER_ISO,
        /**
         * сумма операции (РУБ.КОП)
         */
        SUM,
        /**
         * код валюты операции
         */
        CURRENCY,
        /**
         * дата и время операции (ДД/ММ/ГГ ЧЧ:ММ:СС)
         */
        TIME,
        /**
         * внутренний код операции (всегда 0)
         */
        PERCENT,
        /**
         * тип авторизации
         */
        AUTH_TYPE,
        /**
         * код авторизации
         */
        AUTH_CODE,

        // Третья строка
        /**
         * идентификатор транзакции
         */
        TRANZ_ID;
    }

    @Override
    public String getResponseCode() {
        return parsed.get(GoldCrownDataField.OPER_STATUS);
    }

    @Override
    public String getMessage() {
        return parsed.get(GoldCrownDataField.OPER_TEXT);
    }

    @Override
    public String getAuthCode() {
        return parsed.get(GoldCrownDataField.AUTH_CODE);
    }

    @Override
    public String getTerminalId() {
        return null;
    }

    @Override
    public String getReferenceNumber() {
        return StringUtils.EMPTY;
    }

    @Override
    public boolean isSuccessful() {
        return SUCCESSFULL_RESPONSE_CODE.equals(getResponseCode());
    }

    @Override
    public BankCard getBankCard() {
        BankCard bc = new BankCard();
        bc.setCardNumber(BankUtils.maskCardNumber(parsed.get(GoldCrownDataField.BUYER_ISO)));
        return bc;
    }

    public Date getDate() {
        String dateOfTransaction = StringUtils.trimToNull(parsed.get(GoldCrownDataField.TIME));
        if (dateOfTransaction != null) {
            try {
                return new SimpleDateFormat(RESPONSE_TIME_OF_OPERATION_DATE_FORMAT).parse(dateOfTransaction);
            } catch (Exception e) {
                log.warn("Unable to parse date of operation (" + dateOfTransaction + ")", e);
            }
        }
        return new Date();
    }

    public Long getTerminalTransId() {
        return getLong(GoldCrownDataField.TERM_NUM, "terminal transaction id");
    }

    public String getHostTransId() {
        return parsed.get(GoldCrownDataField.TRANZ_ID);
    }

    private Long getLong(GoldCrownDataField field, String description) {
        String stringValue = parsed.get(field);
        if (StringUtils.isNotBlank(stringValue)) {
            try {
                return Long.valueOf(stringValue);
            } catch (Exception e) {
                log.error(String.format("Unable to parse {} ({})", description, stringValue), e);
            }
        }
        return null;
    }

    @Override
    public ResponseData logResponseFile() {
        String response = StringUtils.join(responseFile, '\n');
        if (StringUtils.isNotBlank(parsed.get(GoldCrownDataField.BUYER_ISO))) {
            response = response.replace(parsed.get(GoldCrownDataField.BUYER_ISO), BankUtils.maskCardNumberForLog(parsed.get(GoldCrownDataField.BUYER_ISO)));
        }
        log.info("Response file:\n" + response);
        return this;
    }
}
