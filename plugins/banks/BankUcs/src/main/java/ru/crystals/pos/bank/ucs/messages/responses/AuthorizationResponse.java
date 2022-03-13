package ru.crystals.pos.bank.ucs.messages.responses;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.BankUtils;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.ucs.ResBundleBankUcs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

public class AuthorizationResponse extends Response {
    private static final String TRANSACTION_DATE_TIME_PATTERN = "yyyyMMddHHmmss";
    private static final String EXPIRY_DATE_PATTERN = "yyMM";
    private static final char DELIMITER = '\u001B';
    private Map<AuthField, String> result = new EnumMap<>(AuthField.class);

    public AuthorizationResponse(String response) {
        super(response);
        if (getData() != null) {
            int offset = 0;
            for (AuthField field : AuthField.values()) {
                if (offset >= getData().length()) {
                    break;
                }
                if (field.getLength() > 0) {
                    result.put(field, getData().substring(offset, offset + field.getLength()));
                    offset += field.getLength();
                } else {
                    String remainData = getData().substring(offset);
                    int indexOfDelimiter = remainData.indexOf(DELIMITER);
                    if (indexOfDelimiter > 0) {
                        result.put(field, remainData.substring(0, indexOfDelimiter));
                    } else if (indexOfDelimiter == -1) {
                        result.put(field, remainData);
                        break;
                    }
                    offset += indexOfDelimiter + 1;
                }
            }
        }
    }

    public String getMerchantId() {
        return result.get(AuthField.MERCHANT_ID);
    }

    public String getUniqueReferenceNumber() {
        return result.get(AuthField.UNIQUE_REFERENCE_NUMBER);
    }

    public String getResponseCode() {
        return result.get(AuthField.RESPONSE_CODE);
    }

    public String getConfirmationCode() {
        return result.get(AuthField.CONFIRMATION_CODE);
    }

    public String getCardDetails() {
        return result.get(AuthField.CARD_DETAILS);
    }

    public String getCardType() {
        return result.get(AuthField.CARD_TYPE);
    }

    public String getMessage() {
        return result.get(AuthField.MESSAGE);
    }

    public Date getDate() {
        return parseDateWithPattern(result.get(AuthField.DATE) + result.get(AuthField.TIME), TRANSACTION_DATE_TIME_PATTERN);
    }

    public String getTransactionCurrency() {
        return result.get(AuthField.TRANSACTION_CURRENCY);
    }

    public Long getTransactionSum() {
        return parseLong(result.get(AuthField.TRANSACTION_SUM));
    }

    public String getOperationType() {
        return result.get(AuthField.OPERATION_TYPE);
    }

    public boolean isSuccessful() {
        return StringUtils.trimToEmpty(getResponseCode()).equals("00");
    }

    public enum AuthField {
        // Fixed fields

        /**
         * Operation Code of the related Request message
         */
        OPERATION_TYPE(1),
        /**
         * In minimum units (cents, kopecks) without any delimiter with leading zeros
         */
        TRANSACTION_SUM(12),
        /**
         * Number in conformity with ISO. If no value is present, 643 (Rubles) is assumed
         */
        TRANSACTION_CURRENCY(3),
        /**
         * Date of current transaction, YYYYMMDD
         */
        DATE(8),
        /**
         * Time of current transaction, HHMMSS
         */
        TIME(6),
        /**
         * Merchant ID. Is assigned by the acquirer
         */
        MERCHANT_ID(15),
        /**
         * Reference number assigned by Terminal. It must be saved by the cash register and repeated in reversals, 2-0 and 2-3 commands
         */
        UNIQUE_REFERENCE_NUMBER(12),
        /**
         * Transaction response code as received from the Payment Scheme
         */
        RESPONSE_CODE(2),

        // Flexible fields

        /**
         * Authorization code. It can be absent in answer for refund operation.
         */
        CONFIRMATION_CODE,
        /**
         * Card details in conformity with mask mode: <li>→ full cardnumber and expiry date YYMM divided by "=", <li>last 4 significant digits of
         * curdnumber, left-filled to the actual length of number by "*".
         */
        CARD_DETAILS,
        /**
         * As defined in the terminal parameters. Example: VISA, MASTER, MAESTRO.
         */
        CARD_TYPE,
        /**
         * Text description of the operation result.
         */
        MESSAGE,
        /**
         * Optional additional data.
         */
        ADDITIONAL_DATA;
        private int length;

        AuthField() {

        }

        AuthField(int length) {
            this.length = length;
        }

        public int getLength() {
            return this.length;
        }
    }

    public AuthorizationData getAuthorizationData() {
        AuthorizationData authorizationData = new AuthorizationData();
        authorizationData.setPrintNegativeSlip(true);
        authorizationData.setAmount(getTransactionSum());

        authorizationData.setCurrencyCode(getTransactionCurrency());

        authorizationData.setTerminalId(getTerminalId());
        authorizationData.setDate(getDate());
        authorizationData.setMerchantId(getMerchantId());
        authorizationData.setRefNumber(getUniqueReferenceNumber());
        authorizationData.setResponseCode(getResponseCode());
        authorizationData.setStatus(isSuccessful());
        authorizationData.setAuthCode(getConfirmationCode());
        authorizationData.setCard(getPreparedBankCard());
        if (isSuccessful()) {
            authorizationData.setMessage(getMessage());
        } else {
            authorizationData.setMessage(
                    ResBundleBankUcs.getString("PROCESSING_ANSWER") + ": " + getMessage() + " (" + getResponseCode() + ")");
        }
        return authorizationData;
    }

    private Long parseLong(String data) {
        if (data != null) {
            try {
                return Long.parseLong(data);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Date parseDateWithPattern(String data, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(data);
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    private BankCard getPreparedBankCard() {
        BankCard card = new BankCard();
        String[] splitted = StringUtils.trimToEmpty(getCardDetails()).split("=");
        card.setCardNumber(BankUtils.maskCardNumber(splitted[0]));
        if (splitted.length > 1 && splitted[1].length() == 4) {
            card.setExpiryDate(parseDateWithPattern(splitted[1], EXPIRY_DATE_PATTERN));
        }
        card.setCardType(getCardType());
        return card;
    }

    @Override
    public void setLoggableFields() {
        getLoggerUtil().add("operation type", getOperationType());
        getLoggerUtil().add("sum", getTransactionSum());
        getLoggerUtil().add("currency", getTransactionCurrency());
        getLoggerUtil().add("date", getDate());
        getLoggerUtil().add("merchantId", getMerchantId());
        getLoggerUtil().add("uniqueReferenceNumber", getUniqueReferenceNumber());
        getLoggerUtil().add("responseCode", getResponseCode());
        getLoggerUtil().add("confirmationCode", getConfirmationCode());
        getLoggerUtil().add("cardDetails", getCardDetails());
        getLoggerUtil().add("cardType", getCardType());
        getLoggerUtil().add("message", getMessage());
    }
}
