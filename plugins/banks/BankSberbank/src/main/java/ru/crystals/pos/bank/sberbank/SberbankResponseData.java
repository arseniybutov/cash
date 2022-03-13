package ru.crystals.pos.bank.sberbank;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.BankTypeEnum;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;

public class SberbankResponseData implements ResponseData {

    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    private static final String RESPONSE_CODE_AND_MESSAGE_DELIMITER = ",";
    static final String SUCCESSFUL_RESPONSE_CODE = "0";
    private static final ThreadLocal<DateFormat> CARD_EXP_DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("MM/yy"));
    private static final ThreadLocal<DateFormat> THREAD_LOCAL_DATEFORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddhhmmss"));
    private static final String EMITTED_BY_SBERBANK = "1";
    private static final String LOYALTY_NOT_FOUND_CODE = "0";
    /**
     * Код прерывания банковской операции для обработки «Программы лояльности»
     */
    static final String SUSPENSION_CODE = "4353";

    private List<String> responseFile = new ArrayList<>();
    private EnumMap<SberbankDataField, String> parsed = new EnumMap<>(SberbankDataField.class);

    @Override
    public SberbankResponseData parseResponseFile(List<String> responseFile) {
        this.responseFile = responseFile;
        parsed.clear();

        String responseAndMessage = getValueByField(SberbankDataField.RESPONSE_CODE);
        if (responseAndMessage != null) {
            String[] responseAndMessageSplitted = responseAndMessage.split(RESPONSE_CODE_AND_MESSAGE_DELIMITER);
            if (responseAndMessageSplitted.length > 0) {
                final String code = responseAndMessageSplitted[0];
                parsed.put(SberbankDataField.RESPONSE_CODE, code);
                final String message = (responseAndMessageSplitted.length > 1 ? responseAndMessageSplitted[1] : null);
                parsed.put(SberbankDataField.MESSAGE, ErrorHandler.getErrorMessageByCode(message, code));
            }
        }
        // Заполнение остальных полей
        for (SberbankDataField field : SberbankDataField.values()) {
            if (field.ordinal() >= SberbankDataField.CARD_NUMBER.ordinal() && field.getRowNumber() < responseFile.size()) {
                String value = getValueByField(field);
                if (value != null) {
                    parsed.put(field, value);
                }
            }
        }
        return this;
    }

    public String getCardNumber() {
        return parsed.get(SberbankDataField.CARD_NUMBER);
    }

    /**
     * Упорядоченный список полей файла ответа
     */
    public enum SberbankDataField {
        // 1 строка: Код результата и текст сообщения (разделены запятой без пробелов)
        /**
         * Код результата
         */
        RESPONSE_CODE(0),
        /**
         * текст сообщения
         */
        MESSAGE(0),
        // 2 строка
        /**
         * Номер карты (маскированный)
         */
        CARD_NUMBER(1),
        /**
         * Срок действия карты в формате ММ/ГГ
         */
        CARD_EXP_DATE(2),
        /**
         * Код авторизации
         */
        AUTH_CODE(3),
        /**
         * Внутренний номер операции
         */
        INTERNAL_OPERATION_NUMBER(4),
        /**
         * Название типа карты
         */
        CARD_TYPE(5),
        /**
         * Признак карты Сбербанка (1)
         */
        IS_SBERBANK_CARD(6),
        /**
         * Номер терминала
         */
        TERMINAL_ID(7),
        /**
         * Дата-время операции (ГГГГММДДччммсс)
         */
        TRANSACTION_DATE(8),
        /**
         * Ссылочный номер операции (может быть пустым)
         */
        REFERENCE_NUMBER(9),
        /**
         * Хеш от номера карты
         */
        CARD_HASH(10),
        /**
         * Номер программы лояльности, если карта не входит ни в одну, возвращается 0
         */
        LOYALTY_CODE(17),
        /**
         * Идентификатор транзакции
         */
        REQUEST_ID(19);

        /**
         * номер строки в файле
         */
        private int rowNumber;

        SberbankDataField(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public int getRowNumber() {
            return rowNumber;
        }
    }

    @Override
    public String getResponseCode() {
        return parsed.get(SberbankDataField.RESPONSE_CODE);
    }

    @Override
    public String getMessage() {
        return parsed.get(SberbankDataField.MESSAGE);
    }

    public void putMessage(String message) {
        parsed.put(SberbankDataField.MESSAGE, message);
    }

    @Override
    public String getAuthCode() {
        return parsed.get(SberbankDataField.AUTH_CODE);
    }

    @Override
    public String getTerminalId() {
        return parsed.get(SberbankDataField.TERMINAL_ID);
    }

    @Override
    public String getReferenceNumber() {
        return parsed.get(SberbankDataField.REFERENCE_NUMBER);
    }

    public String getRequestId() {
        return parsed.get(SberbankDataField.REQUEST_ID);
    }

    @Override
    public String getLoyaltyProgramCode() {
        String loyaltyProgramCode = parsed.get(SberbankDataField.LOYALTY_CODE);
        return LOYALTY_NOT_FOUND_CODE.equals(loyaltyProgramCode) ? null : loyaltyProgramCode;
    }

    @Override
    public boolean isSuccessful() {
        return SUCCESSFUL_RESPONSE_CODE.equals(getResponseCode());
    }

    @Override
    public BankCard getBankCard() {
        BankCard bc = new BankCard();
        bc.setCardNumber(BankUtils.maskCardNumber(parsed.get(SberbankDataField.CARD_NUMBER)));
        bc.setCardType(parsed.get(SberbankDataField.CARD_TYPE));
        bc.setCardNumberHash(parsed.get(SberbankDataField.CARD_HASH));
        if (EMITTED_BY_SBERBANK.equals(parsed.get(SberbankDataField.IS_SBERBANK_CARD))) {
            bc.setCardOperator(BankTypeEnum.SBERBANK);
        }
        String expDate = StringUtils.trimToNull(parsed.get(SberbankDataField.CARD_EXP_DATE));
        if (expDate != null) {
            try {
                bc.setExpiryDate(CARD_EXP_DATE_FORMAT.get().parse(expDate));
            } catch (Exception e) {
                log.warn("Unable to parse expiry date (" + expDate + ")", e);
            }
        }
        return bc;
    }

    public String getTransactionDate() {
        return parsed.get(SberbankDataField.TERMINAL_ID);
    }

    public Date getDate() {
        String dateOfTransaction = StringUtils.trimToNull(parsed.get(SberbankDataField.TRANSACTION_DATE));
        if (dateOfTransaction != null) {
            try {
                return THREAD_LOCAL_DATEFORMAT.get().parse(dateOfTransaction);
            } catch (Exception e) {
                log.warn("Unable to parse date of operation (" + dateOfTransaction + ")", e);
            }
        }
        return new Date();
    }

    private String getValueByField(SberbankDataField field) {
        if (responseFile.size() > field.getRowNumber()) {
            return responseFile.get(field.getRowNumber());
        }
        return null;
    }

    @Override
    public ResponseData logResponseFile() {
        if (log.isInfoEnabled()) {
            int indexOfLineWithCardNumber = SberbankDataField.CARD_NUMBER.getRowNumber();
            List<String> loggableResponse = new ArrayList<>(responseFile);
            if (indexOfLineWithCardNumber < loggableResponse.size()) {
                loggableResponse.set(indexOfLineWithCardNumber, BankUtils.maskCardNumberForLog(loggableResponse.get(indexOfLineWithCardNumber)));
            }
            log.info("Response file:\n{}", StringUtils.join(loggableResponse, '\n'));
        }
        return this;
    }
}
