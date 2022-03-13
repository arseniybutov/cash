package ru.crystals.pos.bank.demir.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static ru.crystals.pos.bank.demir.response.Response.ResponseField.AUTH_CODE;
import static ru.crystals.pos.bank.demir.response.Response.ResponseField.DATE_TIME;
import static ru.crystals.pos.bank.demir.response.Response.ResponseField.MESSAGE;
import static ru.crystals.pos.bank.demir.response.Response.ResponseField.RESPONSE_CODE;
import static ru.crystals.pos.bank.demir.response.Response.ResponseField.RRN;

public class Response {

    private static final String OK = "000";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmm");

    private String responseCode;
    private boolean isOk;
    private LocalDateTime dateTime;
    private String rrn;
    private String authCode;
    private String message;

    private Response() {}

    public static Response parse(String src) {
        Response response = new Response();
        String respCode = RESPONSE_CODE.getFieldValue(src);
        response.responseCode = respCode;
        response.isOk = OK.equals(respCode);
        String dateTimeStr = DATE_TIME.getFieldValue(src);
        if (isNotBlank(dateTimeStr)) {
            response.dateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        }
        response.rrn = RRN.getFieldValue(src);
        response.authCode = AUTH_CODE.getFieldValue(src);
        response.message = MESSAGE.getFieldValue(src);
        return response;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public boolean isOk() {
        return isOk;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getRRN() {
        return rrn;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getMessage() {
        return message;
    }

    enum ResponseField {

        RESPONSE_CODE(26, 29),
        AUTH_CODE(29, 35),
        DATE_TIME(101, 111),
        RRN(132, 144),
        MESSAGE(237, 277);

        /**
         * Где в строке ответа начинается данное поле
         */
        private final int start;
        /**
         * Где в строке ответа заканчивается даннео поле
         */
        private final int end;

        ResponseField(int start, int end) {
            this.start = start;
            this.end = end;
        }

        String getFieldValue(String src) {
            return src.substring(start, end);
        }
    }
}
