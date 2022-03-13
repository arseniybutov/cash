package ru.crystals.pos.bank.ucs.messages.requests;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.ucs.utils.LoggerUtil;

public abstract class Request {
    private RequestType type;
    private String terminalId;
    private LoggerUtil loggerUtil = new LoggerUtil(Request.class);

    Request(RequestType type) {
        this.type = type;
    }

    Request(RequestType type, String terminalId) {
        this(type);
        this.terminalId = terminalId;
    }

    protected String calculateLength() {
        return StringUtils.leftPad(Integer.toHexString(StringUtils.trimToEmpty(getDataToString()).length()).toUpperCase(), 2, "0");
    }

    @Override
    public String toString() {
        return type.getClassAndCode() + StringUtils.leftPad(terminalId != null ? terminalId : "", 10, "0") + calculateLength() +
                StringUtils.trimToEmpty(getDataToString());
    }

    protected abstract String getDataToString();

    @Override
    public boolean equals(Object o) {
        return !(o == null || getClass() != o.getClass()) && this.toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public RequestType getType() {
        return type;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }


    protected String getTerminalId() {
        return terminalId;
    }

    public String toLoggableString() {
        getLoggerUtil().add("type", type);
        getLoggerUtil().add("terminalId", (terminalId != null ? terminalId : "0000000000"));
        getLoggerUtil().add("length", calculateLength());
        setLoggableFields();
        return getLoggerUtil().toString();
    }

    protected void setLoggableFields() {

    }

    public LoggerUtil getLoggerUtil() {
        return loggerUtil;
    }
}
