package ru.crystals.pos.bank.ucs.messages.responses;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.ucs.ResBundleBankUcs;

public class InitialErrorResponse extends Response {
    private String errorCode;
    private String errorMessage;

    public InitialErrorResponse(String response) {
        super(response);
        if (StringUtils.trimToEmpty(getData()).length() >= 2) {
            this.errorCode = getData().substring(0, 2);
            if (getData().length() > 2) {
                this.errorMessage = getData().substring(2);
            }
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getCombinedMessage() {
        return ResBundleBankUcs.getString("TERMINAL_ANSWER") + ": " + errorMessage + " (" + errorCode + ")";
    }

    @Override
    public void setLoggableFields() {
        getLoggerUtil().add("errorCode", getErrorCode());
        getLoggerUtil().add("message", getErrorMessage());
    }
}
