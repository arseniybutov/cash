package ru.crystals.pos.bank.ucs.messages.requests;

import org.apache.commons.lang.StringUtils;

public class LoginRequest extends Request {
    private static final String REQUEST_STATE = "1";

    public LoginRequest(String terminalId) {
        super(RequestType.LOGIN, terminalId);
    }

    public LoginRequest() {
        super(RequestType.LOGIN);
    }

    @Override
    public String getDataToString() {
        return StringUtils.isEmpty(getTerminalId()) ? REQUEST_STATE : "";
    }

    @Override
    public String toString() {
        StringBuilder request = new StringBuilder(RequestType.LOGIN.getClassAndCode());
        request.append(StringUtils.leftPad(getTerminalId() != null ? getTerminalId() : "", 10, "0")).append(calculateLength());
        if (getTerminalId() == null) {
            request.append(StringUtils.trimToEmpty(getDataToString()));
        }
        return request.toString();
    }
}
