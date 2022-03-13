package ru.crystals.pos.bank.ucs.messages.responses;

import ru.crystals.pos.bank.ucs.utils.LoggerUtil;

public class Response {
    private LoggerUtil loggerUtil = new LoggerUtil(Response.class);

    private String rawResponse;
    private String terminalId;
    private String data;
    private ResponseType type;
    private int length;

    Response(String response) {
        rawResponse = response;
        if (rawResponse.length() > 2) {
            type = ResponseType.getType(response.substring(0, 2));
            terminalId = response.substring(2, 12);
            length = Integer.parseInt(response.substring(12, 14), 16);
            if (length > 0) {
                data = response.substring(14);
            }
        }
    }

    public ResponseType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public String getTerminalId() {
        return terminalId;
    }

    @Override
    public String toString() {
        return rawResponse;
    }

    public String toLoggableString() {
        getLoggerUtil().add("type", type);
        getLoggerUtil().add("terminalId", terminalId);
        getLoggerUtil().add("length", length);
        setLoggableFields();
        return getLoggerUtil().toString();
    }

    protected void setLoggableFields() {

    }

    public LoggerUtil getLoggerUtil() {
        return loggerUtil;
    }

}
